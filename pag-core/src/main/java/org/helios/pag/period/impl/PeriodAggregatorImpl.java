/**
 * 
 */
package org.helios.pag.period.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.helios.pag.core.datapoints.Core.DataPoint;
import org.helios.pag.period.IPeriodAggregator;
import org.helios.pag.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: PeriodAggregatorImpl</p>
 * <p>Description: The PeriodAggregator node implementation</p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.pag.period.impl.PeriodAggregatorImpl</code></b>
 */

public class PeriodAggregatorImpl extends ReadOnlyPeriodAggregator {
	
	
	/** The offset of the aggregator lock */
	public final static byte XLOCK = 0;
	/** The offset of the raw enabled indicator */
	public final static byte RAW_ENABLED = UnsafeAdapter.LONG_SIZE;
	
	
	public static void log(Object msg) {
		System.out.println(msg);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#processDataPoint(org.helios.pag.core.datapoints.Core.DataPoint)
	 */
	public IPeriodAggregator processDataPoint(final DataPoint dataPoint) {
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
						UnsafeAdapter.putDouble(address + MEAN, (val + UnsafeAdapter.getDouble(address + MEAN)/2));
					}
				} else {
					long val = dataPoint.getLongValue();
					if(val < UnsafeAdapter.getLong(address + MIN)) UnsafeAdapter.putLong(address + MIN, val);
					if(val > UnsafeAdapter.getLong(address + MAX)) UnsafeAdapter.putLong(address + MAX, val);
					if(newCount==1) {
						UnsafeAdapter.putLong(address + MEAN, val);
					} else {
						UnsafeAdapter.putLong(address + MEAN, (val + UnsafeAdapter.getLong(address + MEAN)/2));
					}					
				}
			}
		}); 
		return this;
	}
	
	/**
	 * Creates a new PeriodAggregatorImpl
	 * @param isDouble true for a double, false for a long
	 */
	PeriodAggregatorImpl(boolean isDouble) {
		super(isDouble);
		reset();
	}
	
	/**
	 * Creates a new PeriodAggregatorImpl which is a direct copy of the passed impl
	 * @param youButACopy The aggregator to copy
	 */
	public ReadOnlyPeriodAggregator readOnly() {
		return new ReadOnlyPeriodAggregator(address);
	}
	
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
	
	public boolean isRawEnabled() {
		return UnsafeAdapter.getByte(address + RAW_ENABLED)==ZERO_BYTE;  // ZERO_BYTE = false, ONE_BYTE = true
	}
	
	public void setRawEnabled(final boolean enabled) {
		UnsafeAdapter.runInLock(address, new Runnable(){
			public void run() {
				UnsafeAdapter.putByte(address + RAW_ENABLED, enabled ? ONE_BYTE : ZERO_BYTE);			}
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
	
	

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PeriodAggregatorImpl [getId()=");
		builder.append(getId());
		builder.append(", getLastTime()=");
		builder.append(getLastTime());
		builder.append(", getCount()=");
		builder.append(getCount());
		builder.append(", isDouble()=");
		builder.append(isDouble());
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
