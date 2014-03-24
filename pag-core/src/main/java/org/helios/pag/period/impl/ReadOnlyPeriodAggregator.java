/**
 * 
 */
package org.helios.pag.period.impl;

import org.helios.pag.period.IPeriodAggregator;
import org.helios.pag.util.unsafe.DeAllocateMe;
import org.helios.pag.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: ReadOnlyPeriodAggregator</p>
 * <p>Description: A read only aggregated period, acquired from the internal active instance of the same id.</p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.pag.period.ReadOnlyPeriodAggregator</code></b>
 */

public class ReadOnlyPeriodAggregator  implements IPeriodAggregator, DeAllocateMe {
	/** The address of the store for this aggregator */
	protected final long address;

	/** The offset of the global id */
	public final static byte ID = UnsafeAdapter.LONG_SIZE + 1;		// 8
	/** The offset of the last time */
	public final static byte LAST_TIME = ID;
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
	
	/**
	 * Creates a new ReadOnlyPeriodAggregator which is a direct copy of the data stored in the passed address
	 * @param fromAddress The address from which to copy
	 */
	ReadOnlyPeriodAggregator(long fromAddress) {
		address = UnsafeAdapter.allocateMemory(UnsafeAdapter.findNextPositivePowerOfTwo(TOTAL));
		UnsafeAdapter.copyMemory(fromAddress, address, TOTAL);
	}
	
	/**
	 * Creates a new ReadOnlyPeriodAggregator
	 * @param isDouble true for a double, false for a long
	 */
	ReadOnlyPeriodAggregator(boolean isDouble) {
		address = UnsafeAdapter.allocateMemory(UnsafeAdapter.findNextPositivePowerOfTwo(TOTAL));
		UnsafeAdapter.registerForDeAlloc(this);
		UnsafeAdapter.setMemory(address, TOTAL, ZERO_BYTE);
		UnsafeAdapter.putLong(address, UnsafeAdapter.NO_LOCK);
		UnsafeAdapter.putByte(address + DOUBLE_OR_LONG, isDouble ? DOUBLE : LONG);
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
	 * @see org.helios.pag.period.IPeriodAggregator#getLongMedian()
	 */
	public long getLongMedian() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#getDoubleMedian()
	 */
	public double getDoubleMedian() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#getMedian()
	 */
	public Number getMedian() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.period.IPeriodAggregator#isRawEnabled()
	 */
	@Override
	public boolean isRawEnabled() {
		throw new UnsupportedOperationException();
	}

	
}
