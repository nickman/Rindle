/**
 * 
 */
package org.helios.pag.period.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.helios.pag.core.datapoints.Core.DataPoint;
import org.helios.pag.period.IPeriodAggregator;
import org.helios.pag.util.unsafe.DeAllocateMe;
import org.helios.pag.util.unsafe.UnsafeAdapter;
import org.helios.pag.util.unsafe.collections.LongSlidingWindow;

/**
 * <p>Title: PeriodAggregatorImpl</p>
 * <p>Description: The PeriodAggregator node implementation</p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.pag.period.impl.PeriodAggregatorImpl</code></b>
 */

public class PeriodAggregatorImpl implements IPeriodAggregator, DeAllocateMe {
	/** The address of the store for this aggregator */
	protected final long address;
	
	/** The raw data container used when a subscriber has requested an aggregation that requires all raw data for the period */
	protected RawDataContainer rawData = null;
	
	/** The offset of the aggregator lock */
	public final static byte XLOCK = 0;							// 8
	/** The offset of the raw enabled indicator */
	public final static byte RAW_ENABLED = UnsafeAdapter.LONG_SIZE;  // 1
	/** The offset of the global id */
	public final static byte ID = RAW_ENABLED + 1;		// 8
	/** The offset of the last time */
	public final static byte LAST_TIME = ID + UnsafeAdapter.LONG_SIZE;
	/** The offset of the count */
	public final static byte COUNT = LAST_TIME + UnsafeAdapter.LONG_SIZE;
	/** The offset of the min value */
	public final static byte MIN = COUNT + UnsafeAdapter.LONG_SIZE;
	/** The offset of the max value */
	public final static byte MAX = MIN + UnsafeAdapter.LONG_SIZE;
	/** The offset of the average or driver value */
	public final static byte MEAN = MAX + UnsafeAdapter.LONG_SIZE;
	/** The offset of the long/double indicator (double is 0, long is 1) */
	public final static byte DOUBLE_OR_LONG = MEAN + UnsafeAdapter.LONG_SIZE;	
	/** The total memory allocation  */
	public final static byte TOTAL = DOUBLE_OR_LONG + UnsafeAdapter.LONG_SIZE;
	
	
	
	/** A zero byte value */
	public static final byte ZERO_BYTE = 0;
	/** A one byte value */
	public static final byte ONE_BYTE = 1;
	/** Double indicator */
	public static final byte DOUBLE = 0;
	/** Long indicator */
	public static final byte LONG = 1;

	
	public long[] getAddresses() {
		return new long[]{address};
	}
	
	
	
	public static void log(Object msg) {
		System.out.println(msg);
	}

	/**
	 * Processes a new data point into this aggregator
	 * @param dataPoint The data point to process
	 * @return this aggregator
	 */
	public IPeriodAggregator processDataPoint(final DataPoint dataPoint) {
		final long startTime = System.nanoTime();
		UnsafeAdapter.runInLock(address, new Runnable(){
			public void run() {
				final long newCount = increment();
				if(dataPoint.hasDoubleValue()) {
					double val = dataPoint.getDoubleValue();
					if(val < UnsafeAdapter.getDouble(address + MIN)) UnsafeAdapter.putDouble(address + MIN, val);
					if(val > UnsafeAdapter.getDouble(address + MAX)) UnsafeAdapter.putDouble(address + MAX, val);
					if(newCount==1) {
						UnsafeAdapter.putDouble(address + MEAN, val);
					} else {
						UnsafeAdapter.putDouble(address + MEAN, avgd(UnsafeAdapter.getDouble(address + MEAN), newCount-1, val));
					}
					if(isRawEnabled()) rawData.append(val);
				} else {
					long val = dataPoint.getLongValue();
					if(val < UnsafeAdapter.getLong(address + MIN)) UnsafeAdapter.putLong(address + MIN, val);
					if(val > UnsafeAdapter.getLong(address + MAX)) UnsafeAdapter.putLong(address + MAX, val);
					if(newCount==1) {
						UnsafeAdapter.putDouble(address + MEAN, val);
					} else {
						UnsafeAdapter.putDouble(address + MEAN, avgd(UnsafeAdapter.getDouble(address + MEAN), newCount-1, val));
					}
					if(isRawEnabled()) {
						rawData.append(val);
					}
				}
			}
		}); 
		return this;
	}
	
	
	/**
	 * Calcs a double average incorporating a new value
	 * using <b><code>(prev_avg*cnt + newval)/(cnt+1)</code></b>
	 * @param prev_avg The pre-average
	 * @param cnt The pre-count
	 * @param newval The new value
	 * @return the average
	 */
	public static double avgd(double prev_avg, double cnt, double newval) {		
		return (prev_avg*cnt + newval)/(cnt+1);
	}
	
	
	/** A constant place-holder, not to be used. */
	public static final PeriodAggregatorImpl CONST = new PeriodAggregatorImpl();
	
	
	/**
	 * Creates a new PeriodAggregatorImpl for the const.
	 */
	private PeriodAggregatorImpl() {
		address = -1L;
	}
	
	/**
	 * Creates a new PeriodAggregatorImpl
	 * @param isDouble true for a double, false for a long
	 */
	public PeriodAggregatorImpl(boolean isDouble) {
		address = UnsafeAdapter.allocateAlignedMemory(TOTAL);
		UnsafeAdapter.registerForDeAlloc(this);
		UnsafeAdapter.setMemory(address, TOTAL, ZERO_BYTE);
		UnsafeAdapter.putLong(address, UnsafeAdapter.NO_LOCK);
		UnsafeAdapter.putByte(address + DOUBLE_OR_LONG, isDouble ? DOUBLE : LONG);
		reset();
	}
	
//	/**
//	 * Creates a new PeriodAggregatorImpl which is a direct copy of the passed impl
//	 * @param youButACopy The aggregator to copy
//	 */
//	public ReadOnlyPeriodAggregator readOnly() {
//		return new ReadOnlyPeriodAggregator(address);
//	}
	
	/**
	 * Reset procedure after the flush procedure and init of a new aggregator
	 */
	protected void reset() {
		if(isLong()) {
			UnsafeAdapter.putLong(address + MIN, Long.MAX_VALUE);
			UnsafeAdapter.putLong(address + MAX, Long.MIN_VALUE);
		} else {
			UnsafeAdapter.putDouble(address + MIN, Double.MAX_VALUE);
			UnsafeAdapter.putDouble(address + MAX, Double.MIN_VALUE);			
		}
		UnsafeAdapter.putLong(address + COUNT, 0L);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#isRawEnabled()
	 */
	@Override
	public boolean isRawEnabled() {
		return UnsafeAdapter.getByte(address + RAW_ENABLED)!=ZERO_BYTE;  // ZERO_BYTE = false, ONE_BYTE = true
	}
	
	/**
	 * Sets the enabled state of raw data aggregation
	 * @param enabled true to enable, false to disable
	 */
	public void setRawEnabled(final boolean enabled) {
		UnsafeAdapter.runInLock(address, new Runnable(){
			public void run() {				
				UnsafeAdapter.putByte(address + RAW_ENABLED, enabled ? ONE_BYTE : ZERO_BYTE);
				if(enabled && rawData==null) {
					rawData = RawDataContainer.newInstance();
				} else if(!enabled && rawData!=null) {
					rawData = null;
				}
			}
		}); 
	}


	public long increment() {
		return increment(1L);
	}

	public long increment(long value) {
		long newval = UnsafeAdapter.getLong(address + COUNT) + value;
		UnsafeAdapter.putLong(address + COUNT, newval);
		UnsafeAdapter.putLong(address + LAST_TIME, System.currentTimeMillis());
		return newval;
	}

	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.impl.ReadOnlyPeriodAggregator#getLongMedian()
	 */
	public long getLongMedian() {
		if(!isRawEnabled()) throw new IllegalStateException("The aggregator does not have raw data enabled", new Throwable());
		return rawData.getLongMedian();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.impl.ReadOnlyPeriodAggregator#getDoubleMedian()
	 */
	public double getDoubleMedian() {
		if(!isRawEnabled()) throw new IllegalStateException("The aggregator does not have raw data enabled", new Throwable());
		return rawData.getDoubleMedian();
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.impl.ReadOnlyPeriodAggregator#getMedian()
	 */
	public Number getMedian() {
		if(!isRawEnabled()) throw new IllegalStateException("The aggregator does not have raw data enabled", new Throwable());
		return isDouble() ? getDoubleMedian() : getLongMedian();
	}
	

	
	public static void main(String[] args) {
		log("SIZE PER:" + TOTAL + "  NEXT POW:" + UnsafeAdapter.findNextPositivePowerOfTwo(TOTAL));
		final Random R = new Random(System.currentTimeMillis());
		for(int x = 0; x < 1000000; x++) {
			List<PeriodAggregatorImpl> p = new ArrayList<PeriodAggregatorImpl>(100000);
			int loops = R.nextInt(100000);
			for(int i = 0; i < loops; i++) {
				p.add(new PeriodAggregatorImpl(i%2==0));
			}
//			log("Created Aggregators");
			try { Thread.sleep(5000); } catch (Exception ex) {}
			p.clear();
			System.gc();
//			log("Cleared Aggregators");
			System.gc();
			try { Thread.sleep(5000); } catch (Exception ex) {}
		}		
	}
	
	@Override
	public double[] getDoubles() {
		if(!isRawEnabled()) throw new IllegalStateException("The aggregator does not have raw data enabled", new Throwable());
		if(isDouble()) return rawData.getDoubles();
		long[] ls = rawData.getLongs();
		double[] ds = new double[ls.length];
		for(int x = 0; x < ds.length; x++) ds[x] = ls[x];
		return ds;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#getLongs()
	 */
	@Override
	public long[] getLongs() {
		if(!isRawEnabled()) throw new IllegalStateException("The aggregator does not have raw data enabled", new Throwable());
		if(isLong()) return rawData.getLongs();
		double[] ds = rawData.getDoubles();
		long[] ls = new long[ds.length];
		for(int x = 0; x < ds.length; x++) ls[x] = (long)ds[x];
		return ls;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#getId()
	 */
	@Override
	public long getId() {
		return UnsafeAdapter.getLong(address + ID);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#getLastTime()
	 */
	@Override
	public long getLastTime() {
		return UnsafeAdapter.getLong(address + LAST_TIME);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#getCount()
	 */
	@Override
	public long getCount() {
		return UnsafeAdapter.getLong(address + COUNT);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#isLong()
	 */
	@Override
	public boolean isLong() {
		return UnsafeAdapter.getByte(address + DOUBLE_OR_LONG)==LONG;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#isDouble()
	 */
	@Override
	public boolean isDouble() {
		return UnsafeAdapter.getByte(address + DOUBLE_OR_LONG)==DOUBLE;
	}

	@Override
	public double getDoubleMean() {
		return UnsafeAdapter.getDouble(address + MEAN);
	}

	@Override
	public double getDoubleMin() {
		return UnsafeAdapter.getDouble(address + MIN);
	}

	@Override
	public double getDoubleMax() {
		return UnsafeAdapter.getDouble(address + MAX);
	}

	@Override
	public long getLongMean() {
		return (long)UnsafeAdapter.getDouble(address + MEAN);
	}

	@Override
	public long getLongMin() {
		return UnsafeAdapter.getLong(address + MIN);
	}

	@Override
	public long getLongMax() {
		return UnsafeAdapter.getLong(address + MAX);
	}

	@Override
	public Number getMean() {
		if(isDouble()) return getDoubleMean();
		return getLongMean();
	}

	@Override
	public Number getMin() {
		if(isDouble()) return getDoubleMin();
		return getLongMin();
	}

	@Override
	public Number getMax() {
		if(isDouble()) return getDoubleMax();
		return getLongMax();
	}
	
	

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Period [id=");
		builder.append(getId());
		builder.append(", LastTime=");
		builder.append(getLastTime());
		final long cnt = getCount();
		builder.append(", Count=");
		builder.append(cnt);
		builder.append(", nType=");
		final boolean isd = isDouble();		
		builder.append(isd ? "d" : "l");
		
		if(cnt>0) {
			if(isd) {
				builder.append(", min=").append(getDoubleMin())
				.append(", max=").append(getDoubleMax())
				.append(", mean=").append(getDoubleMean());
			} else {
				builder.append(", min=").append(getLongMin())
				.append(", max=").append(getLongMax())
				.append(", mean=").append(getLongMean());				
			}
			if(isRawEnabled() && rawData!=null) {
				builder.append("\n\traw=");
				if(isd) {
					if(rawData.size()>128) {
						builder.append(rawData.size()).append(" doubles");
					} else {
						builder.append(Arrays.toString(rawData.getDoubles()));
					}
				} else {
					if(rawData.size()>128) {
						builder.append(rawData.size()).append(" longs");
					} else {
						builder.append(Arrays.toString(rawData.getLongs()));
					}
				}
				builder.append("\n");
			}
		} 		
		builder.append("]");
		return builder.toString();
	}


	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (address ^ (address >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PeriodAggregatorImpl other = (PeriodAggregatorImpl) obj;
		if (address != other.address)
			return false;
		return true;
	}
	
}
