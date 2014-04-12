/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package org.helios.rindle.control;

import gnu.trove.set.hash.TIntHashSet;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.cliffc.high_scale_lib.NonBlockingHashSet;
import org.helios.rindle.Constants;
import org.helios.rindle.util.ConfigurationHelper;
import org.helios.rindle.util.StringHelper;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;

/**
 * <p>Title: FlushScheduler</p>
 * <p>Description: Schedules flushes for each aggregator in accordance with the subscribers' granularity requests.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.control.FlushScheduler</code></p>
 */

public class FlushScheduler implements ThreadFactory, UncaughtExceptionHandler {
	/** The singleton instance */
	private static volatile FlushScheduler instance = null;
	/** The singleton instance ctror lock */
	private static final Object lock = new Object();
	
	/** Serial number factory for thread factory */
	private static final AtomicInteger threadSerial = new AtomicInteger();
	/** Thread group that the timer's threads run in */
	private static final ThreadGroup threadGroup = new ThreadGroup(FlushScheduler.class.getSimpleName());
	
	/** Instance logger */
	protected final Logger log = LogManager.getLogger(getClass());
	/** The timer instance */
	protected final HashedWheelTimer timer;
	/** The minimum period granularity and the tick duration of the timer in seconds */
	protected final int minTick;
	/** The tick size of the timer */
	protected final int tickSize;
	/** The max period allowable in seconds. */
	protected final int maxPeriod;
	/** The maximum number of distinct periods */
	protected final int maxPeriodCount;
	/** An array of all the distinct periods */
	protected final int[] distinctPeriods;
	/** A map of pre-created timeout tasks, one for each period */
	protected final NonBlockingHashMapLong<TimerTask> timerTasks;
	/** A map of the number of aggregators with subscribers of a period, keyed by the period */
	protected final NonBlockingHashMapLong<AtomicInteger> activePeriods;
	/** A set of flush listeners to be notified on period events keyed by the period */ 
	protected final NonBlockingHashMapLong<NonBlockingHashSet<IFlushPeriodListener>> listeners;
	/** A set of state aware flush listeners to be notified on period events keyed by the period */ 
	protected final NonBlockingHashMapLong<NonBlockingHashSet<IStateAwareFlushPeriodListener>> stateAwareListeners;
	
	/** An executor to handle firing listener callbacks against registered listeners */
	protected final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
		private final AtomicInteger serial = new AtomicInteger(0);
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "FlushPeriodExecutorThread#" + serial.incrementAndGet());
			t.setDaemon(true);
			return t;
		}
	});
	
	/** An empty int array constant */
	private static final int[] EMPTY_INT_ARR = new int[0];
	
	/**Acquires the flush scheduler singleton instance
	 * @return the flush scheduler singleton instance
	 */
	public static FlushScheduler getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new FlushScheduler();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Creates a new FlushScheduler
	 */
	private FlushScheduler() {
		minTick = ConfigurationHelper.getIntSystemThenEnvProperty(Constants.PERIOD_MIN_GRANULARITY, Constants.DEFAULT_PERIOD_MIN_GRANULARITY);
		tickSize = ConfigurationHelper.getIntSystemThenEnvProperty(Constants.PERIOD_TIMER_TICK_SIZE, Constants.DEFAULT_PERIOD_TIMER_TICK_SIZE);
		maxPeriod = ConfigurationHelper.getIntSystemThenEnvProperty(Constants.PERIOD_MAX, Constants.DEFAULT_PERIOD_MAX);
		if(minTick > maxPeriod) throw new IllegalArgumentException("Invalid period configuration. Granularity [" + minTick + "] cannot be higher than the max period [" + maxPeriod + "]", new Throwable());
		maxPeriodCount = (int) Math.ceil((double)maxPeriod / (double)minTick);
		log.debug("Min Granularity: {}", minTick);
		log.debug("Max Period: {}", maxPeriod);
		log.debug("Max Period Count: {}", maxPeriodCount);
		try { ((ThreadPoolExecutor)threadPool).prestartCoreThread(); } catch (Exception x) {/* No Op */}
		activePeriods = new NonBlockingHashMapLong<AtomicInteger>(maxPeriodCount, true);
		timerTasks = new NonBlockingHashMapLong<TimerTask>(maxPeriodCount, true);
		listeners = new NonBlockingHashMapLong<NonBlockingHashSet<IFlushPeriodListener>>(maxPeriodCount, false);
		stateAwareListeners = new NonBlockingHashMapLong<NonBlockingHashSet<IStateAwareFlushPeriodListener>>(maxPeriodCount, false);
		distinctPeriods = new int[maxPeriodCount];
		int _dpx = 0;
		for(int x = minTick; x <= maxPeriod; x += minTick) {
			activePeriods.put(x, new AtomicInteger(0));
			timerTasks.put(x, newPeriodTimerTask(x));
			listeners.put(x, new NonBlockingHashSet<IFlushPeriodListener>());
			stateAwareListeners.put(x, new NonBlockingHashSet<IStateAwareFlushPeriodListener>());
			distinctPeriods[_dpx] = x;
			_dpx++;
		}
		log.debug("Added {} Counters to Active Periods", activePeriods.size());
		timer = new HashedWheelTimer(this, minTick, TimeUnit.SECONDS, tickSize);
		timer.start();
		log.info(StringHelper.banner("Started Flush Scheduler"));
	}
	
	/**
	 * Registers a flush period listener
	 * @param listener The listener to register
	 * @return An array of the adjusted periods the listener was subscribed to
	 */
	public int[] registerListener(IFlushPeriodListener listener) {
		if(listener!=null) {
			final IStateAwareFlushPeriodListener stateAware = (listener instanceof IStateAwareFlushPeriodListener) ? (IStateAwareFlushPeriodListener)listener : null;
			int[] periods = listener.getPeriods();
			if(periods.length==0) return EMPTY_INT_ARR;
			if(periods.length==1 && periods[0]==-1) {
				for(NonBlockingHashSet<IFlushPeriodListener> set: listeners.values()) {
					set.add(listener);					
				}
				if(stateAware!=null) {
					for(NonBlockingHashSet<IStateAwareFlushPeriodListener> set: stateAwareListeners.values()) {
						set.add(stateAware);					
					}					
				}
				listener.setAdjustedPeriods(distinctPeriods);
				return distinctPeriods;
			}
			TIntHashSet adjustedPeriods = new TIntHashSet();
			for(int p : periods) {
				adjustedPeriods.add(adjustPeriod(p));
			}
			int[] finalPeriods = adjustedPeriods.toArray();
			for(int p: finalPeriods) {
				listeners.get(p).add(listener);
				listener.setAdjustedPeriods(finalPeriods);
				activatePeriod(p);
			}
			if(stateAware!=null) {
				for(int p: finalPeriods) {
					stateAwareListeners.get(p).add(stateAware);
				}
			}
			return finalPeriods;
		}
		return EMPTY_INT_ARR;
	}
	
	/**
	 * Removes a listener from all subscribed periods
	 * @param listener The listener to remove
	 */
	public void removeListener(IFlushPeriodListener listener) {
		if(listener!=null) {
			final IStateAwareFlushPeriodListener stateAware = (listener instanceof IStateAwareFlushPeriodListener) ? (IStateAwareFlushPeriodListener)listener : null;
			for(NonBlockingHashSet<IFlushPeriodListener> set: listeners.values()) {
				set.remove(listener);
			}
			if(stateAware!=null) {
				for(NonBlockingHashSet<IStateAwareFlushPeriodListener> set: stateAwareListeners.values()) {
					set.remove(stateAware);
				}				
			}
		}
	}
	
	/**
	 * Removes a listener from the specified periods
	 * @param listener The listener to remove
	 * @param periods The periods to remove the passed listener from
	 */
	public void removeListener(IFlushPeriodListener listener, int...periods) {
		if(listener!=null && periods!=null && periods.length>0) {
			final IStateAwareFlushPeriodListener stateAware = (listener instanceof IStateAwareFlushPeriodListener) ? (IStateAwareFlushPeriodListener)listener : null;
			TIntHashSet adjustedPeriods = new TIntHashSet();
			for(int p : periods) {
				adjustedPeriods.add(adjustPeriod(p));
			}
			for(int p: adjustedPeriods.toArray()) {
				listeners.get(p).remove(listener);
			}
			if(stateAware!=null) {
				for(int p: adjustedPeriods.toArray()) {
					stateAwareListeners.get(p).remove(stateAware);
				}
			}			
		}
	}
	
	/**
	 * Creates a new timer task for the given period
	 * @param period The timer task period
	 * @return the timer task
	 */
	protected TimerTask newPeriodTimerTask(final int period) {
		return new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				if(activePeriods.get(period).get()>0) {
					timer.newTimeout(this, period, TimeUnit.SECONDS);
					log.trace("Re-scheduled period timer for period {}", period);
					threadPool.execute(new Runnable() {
						public void run() {
							for(IFlushPeriodListener listener: listeners.get(period)) {
								listener.onPeriodFlush(period);
							}							
						}
					});
				} else {
					log.debug("Expired period timer for period {}", period);
					timeout.cancel();
					threadPool.execute(new Runnable() {
						public void run() {
							for(IStateAwareFlushPeriodListener listener: stateAwareListeners.get(period)) {
								listener.onPeriodDeactivate(period);
							}
						}
					});					
				}
			}
		};
	}
	
	public static void main(String[] args) {		
		System.setProperty(Constants.PERIOD_MIN_GRANULARITY, "1");
		FlushScheduler f = new FlushScheduler();
		f.log.info("Test FlushScheduler");
		f.log.info("Activating Period 2");
		f.activatePeriod(2);
		try { Thread.sleep(10000); } catch (Exception x) {}
		f.log.info("Deactivating Period 2");
		f.deactivatePeriod(2);
		f.log.info("=========================================");
		try { Thread.sleep(5000); } catch (Exception x) {}
		f.log.info("Activating Period 2");
		f.activatePeriod(2);
		try { Thread.sleep(10000); } catch (Exception x) {}
		f.log.info("Deactivating Period 2");
		f.deactivatePeriod(2);
		try { Thread.sleep(5000); } catch (Exception x) {}
		
		f.log.info("Bye");
	}
	
	/**
	 * Adds an activation to the specified period
	 * @param period The period to add an activation to
	 * @return the adjusted period, modified, if necessary, to fit into the configured granularity.
	 */
	int activatePeriod(final int period) {
		final int _period = adjustPeriod(period);
		if(activePeriods.get(_period).incrementAndGet()==1) {
			timer.newTimeout(timerTasks.get(_period), _period, TimeUnit.SECONDS);
			threadPool.execute(new Runnable() {
				public void run() {
					for(IStateAwareFlushPeriodListener listener: stateAwareListeners.get(_period)) {
						listener.onPeriodActivate(_period);
					}
				}
			});
		}
		return _period;
	}
	
	/**
	 * Removes an activation from the specified period
	 * @param period The period to remove an activation from
	 * @return the adjusted period, modified, if necessary, to fit into the configured granularity.
	 */
	int deactivatePeriod(int period) {
		activePeriods.get(adjustPeriod(period)).decrementAndGet();
		return period;
	}
	
	/**
	 * Adjusts the passed period, if necessary, to fit into the configured granularity.
	 * @param period The period to adjust
	 * @return the adjusted period
	 */
	int adjustPeriod(int period) {
		if(period > maxPeriod) {
			throw new IllegalArgumentException("Invalid period [" + period + "]  >  Max Period [" + maxPeriod + "]", new Throwable());
		}		
		if(period < minTick) period = minTick;
		else {
			int mod = period%minTick;
			if(mod!=0) {
				period += mod;
			}
		}		
		return period;
	}
	/**
	 * Returns the number of activations in each period keyed by the period
	 * @return the number of activations in each period keyed by the period
	 */
	public Map<Long, Integer> getActivePeriods() {
		Map<Long, Integer> map = new TreeMap<Long, Integer>();
		for(Map.Entry<Long, AtomicInteger> entry: activePeriods.entrySet()) {
			map.put(entry.getKey(), entry.getValue().get());
		}
		return map;
	}

	/**
	 * {@inheritDoc}
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(threadGroup, r, getClass().getSimpleName() + "Thread#" + threadSerial.incrementAndGet());
		t.setPriority(Thread.MAX_PRIORITY);
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(this);
		return t;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("Uncaught timer task exception on [{}]", t, e);
		
	}
}
