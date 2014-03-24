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
package org.helios.pag.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.ObjectName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.helios.pag.Constants;
import org.helios.pag.core.datapoints.Core.DataPoint;
import org.helios.pag.core.datapoints.Core.DataPoints;
import org.helios.pag.period.IPeriodAggregator;
import org.helios.pag.period.impl.PeriodAggregatorImpl;
import org.helios.pag.util.ConfigurationHelper;
import org.helios.pag.util.JMXHelper;
import org.helios.pag.util.StringHelper;
import org.helios.pag.util.SystemClock;
import org.helios.pag.util.SystemClock.ElapsedTime;
import org.helios.pag.util.unsafe.UnsafeAdapter;
import org.helios.pag.util.unsafe.collections.LongSlidingWindow;



/**
 * <p>Title: Registry</p>
 * <p>Description: The main registry for {@link IPeriodAggregator}s</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.control.Registry</code></p>
 */

public class Registry implements RegistryMXBean {
	/** The singleton instance */
	private static volatile Registry instance = null;
	/** The singleton instance ctor lock */
	private static final Object lock = new Object();
	/** The timestamp of the start of the current period */
	protected long startTime;
	/** The timestamp of the end of the current period */
	protected long endTime;
	/** Instance logger */
	protected Logger log = LogManager.getLogger(getClass());
	
	/** The map of aggregators keyed by the global metric ID */
	protected final NonBlockingHashMapLong<PeriodAggregatorImpl> aggregators;
	
	
	/** The registry's JMX ObjectName */
	public static final ObjectName OBJECT_NAME = JMXHelper.objectName(new StringBuilder(Registry.class.getPackage().getName()).append(":service=").append(Registry.class.getSimpleName()));
	
	/**
	 * Acquires the registry singleton instance
	 * @return the registry
	 */
	public static Registry getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new Registry();
					JMXHelper.registerMBean(instance, OBJECT_NAME);	
				}
			}
		}
		return instance;
	}
	
	public static void main(String[] args) {
		Registry reg = Registry.getInstance();
		Random R = new Random(System.currentTimeMillis());
		int loops = 100;
		long id = 23475;		
		LongSlidingWindow win = new LongSlidingWindow(loops);
//		//reg.setRawDataEnabled(id, false, true);
		IPeriodAggregator ipa = null;
		reg.log.info("Starting Warmup");
		for(int i = 0; i < 1500000; i++) {
			long v = Math.abs(R.nextInt(100));
			DataPoint dp = DataPoint.newBuilder()
					.setGlobalID(id)
					.setLongValue(v)
					.setTimestamp(System.currentTimeMillis())
					.setValueType(DataPoint.ValueType.LONG)
					.build();
			win.insert(v);
			ipa = reg.processDataPoint(dp);			
			//try { Thread.sleep(100); } catch (Exception x) {}			
		}
		reg.log.info(ipa);
		ipa = null;
		id++;
		int testLoops = 100000;
//
		reg.log.info("Starting Test");
		reg.setRawDataEnabled(id, false, true);
		win.clear();
		List<DataPoint> dps = new ArrayList<DataPoint>(testLoops);
		for(int i = 0; i < testLoops; i++) {
			long v = Math.abs(R.nextInt(100));
			dps.add(DataPoint.newBuilder()
			.setGlobalID(id)
			.setLongValue(v)
			.setTimestamp(System.currentTimeMillis())
			.setValueType(DataPoint.ValueType.LONG)
			.build());
		}
		reg.log.info("Total Samples: {}", dps.size());

		ElapsedTime et = SystemClock.startClock();
		for(DataPoint dp: dps) {
			
//			win.insert(dp.getLongValue());
			ipa = reg.processDataPoint(dp);			
			//try { Thread.sleep(100); } catch (Exception x) {}			
		}
		String etMsg = et.printAvg("Samples", testLoops); 
		reg.log.info("Elapsed: {}", etMsg);
		reg.log.info("DP Avg: {}, Win Avg: {}  DP Median: {}\n\tElapsed: {}", 
				ipa.getMean(), 
				win.avg(), 
				ipa.isRawEnabled() ? ipa.getMedian() :   -1,  
				etMsg);
//		((PeriodAggregatorImpl)ipa).setRawEnabled(false);
//		ipa = null;
//		//reg.aggregators.clear();
		System.gc();
		reg.log.info(ipa);
		try { Thread.sleep(3000); } catch (Exception x) {}
	}

	/**
	 * Creates a new Registry
	 */
	private Registry() {
		int size = UnsafeAdapter.findNextPositivePowerOfTwo(ConfigurationHelper.getIntSystemThenEnvProperty(Constants.REG_INIT_SIZE, Constants.DEFAULT_REG_INIT_SIZE));
		boolean space4speed = ConfigurationHelper.getBooleanSystemThenEnvProperty(Constants.REG_SPACE_FOR_SPEED, Constants.DEFAULT_REG_SPACE_FOR_SPEED);
		log.info("Registry Map Options:\n\tsize: {}\n\tspaceForspeed: {}", size, space4speed);
		aggregators = new NonBlockingHashMapLong<PeriodAggregatorImpl>(size, space4speed);		
		log.info(StringHelper.banner("Registry Started"));
	}
	
	/**
	 * Processes a collection of data points
	 * @param dataPoints the collection of data points to process
	 */
	public void processDataPoints(DataPoints dataPoints) {
		if(dataPoints==null || dataPoints.getDataPointsCount()<1) return;
		for(DataPoint dp: dataPoints.getDataPointsList()) {
			processDataPoint(dp);
		}
	}
	
	/**
	 * Process a single data point
	 * @param dataPoint the data point to process
	 * @return The processed aggregator
	 */
	public IPeriodAggregator processDataPoint(DataPoint dataPoint) {
		final long ID = dataPoint.getGlobalID();
		PeriodAggregatorImpl pai = null;
		if(aggregators.putIfAbsent(ID, PeriodAggregatorImpl.CONST)==null) {
			pai = new PeriodAggregatorImpl(dataPoint.hasDoubleValue());
			aggregators.replace(ID, pai);
		} else {
			pai = aggregators.get(ID);
		}
		return pai.processDataPoint(dataPoint);		
	}
	
	/**
	 * Sets the enabled state of raw data aggregation in the identified aggregator
	 * @param id The id of the aggregator
	 * @param enabled true to enable, false to disable
	 * @return the modified aggregator or null if the aggregator was not found
	 */
	public IPeriodAggregator setRawDataEnabled(long id, boolean enabled) {
		PeriodAggregatorImpl pai = aggregators.get(id);
		if(pai==null) return null;
		pai.setRawEnabled(enabled);
		return pai;
	}
	
	/**
	 * Sets the enabled state of raw data aggregation in the identified aggregator,
	 * creating a new aggregator if it does not exist
	 * @param id The id of the aggregator
	 * @param isDouble true for a double, false for a long
	 * @param enabled true to enable, false to disable
	 * @return the modified aggregator or null if the aggregator was not found
	 */
	public IPeriodAggregator setRawDataEnabled(long id, boolean isDouble, boolean enabled) {		
		PeriodAggregatorImpl pai = null;
		if(aggregators.putIfAbsent(id, PeriodAggregatorImpl.CONST)==null) {
			pai = new PeriodAggregatorImpl(isDouble);
			aggregators.replace(id, pai);
		} else {
			pai = aggregators.get(id);
		}
		pai.setRawEnabled(enabled);
		return pai;
	}
	
	
	/**
	 * Returns the timestamp of the start of the current period
	 * @return the timestamp of the start of the current period
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Returns the timestamp of the end of the current period
	 * @return the timestamp of the end of the current period
	 */
	public long getEndTime() {
		return endTime;
	}

	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.control.RegistryMXBean#getAggregatorCount()
	 */
	@Override
	public long getAggregatorCount() {
		return aggregators.size();
	}

}
