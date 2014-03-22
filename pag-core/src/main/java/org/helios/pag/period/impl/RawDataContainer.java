/**
 * 
 */
package org.helios.pag.period.impl;

import static org.helios.pag.Constants.DEFAULT_INIT_SLOTS_ALLOC;
import static org.helios.pag.Constants.DEFAULT_MAX_SLOTS_ALLOC;
import static org.helios.pag.Constants.DEFAULT_RESIZE_SLOTS_ALLOC;
import static org.helios.pag.Constants.INIT_SLOTS_ALLOC;
import static org.helios.pag.Constants.MAX_SLOTS_ALLOC;
import static org.helios.pag.Constants.RESIZE_SLOTS_ALLOC;

import org.helios.pag.util.ConfigurationHelper;
import org.helios.pag.util.unsafe.DeAllocateMe;
import org.helios.pag.util.unsafe.UnsafeAdapter;
/**
 * <p>Title: RawDataContainer</p>
 * <p>Description: Manages an off-heap array of raw data  </p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.pag.period.impl.RawDataContainer</code></b>
 */

public class RawDataContainer  {
	// capacity
	// size
	// <8> ....
	
	/**
	 * <p>Title: SwappableRawDataContainer</p>
	 * <p>Description: The internal swappable container</p>
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><b><code>org.helios.pag.period.impl.RawDataContainer.SwappableRawDataContainer</code></b>
	 */
	private class SwappableRawDataContainer implements DeAllocateMe {
		/** The address of the swappable container */
		private final long address;

		public SwappableRawDataContainer(long address) {
			this(address, -1L, -1L);
		}
		
		public SwappableRawDataContainer(long address, long dataSegmentAddress, long dataSegmentSize) {
			this.address = address;
			if(dataSegmentSize!=-1L) {
				UnsafeAdapter.copyMemory(dataSegmentAddress, address, dataSegmentSize);
			}
			UnsafeAdapter.registerForDeAlloc(this);
		}
		
		
		@Override
		public long[] getAddresses() {
			return new long[]{address};
		}		
		
	}
	
	
	
	/** The address of the swappable container */
	private long address;
	/** The swappable container */
	private SwappableRawDataContainer internal = null;
	
	
	private RawDataContainer() {
		long dataSegment = INIT_ALLOC << 3;
		address = UnsafeAdapter.allocateMemory(DATA + dataSegment);
		UnsafeAdapter.putInt(address + CAPACITY, INIT_ALLOC);
		UnsafeAdapter.putInt(address + SIZE, 0);
		UnsafeAdapter.setMemory(address + DATA, dataSegment, ZERO_BYTE);
		internal = new SwappableRawDataContainer(address);		
	}
	

	
	
	/** The offset of the capacity */
	public final static byte CAPACITY = 0;
	/** The offset of the size */
	public final static byte SIZE = UnsafeAdapter.INT_SIZE;
	/** The offset of the start of the data array */
	public final static byte DATA = SIZE + UnsafeAdapter.INT_SIZE;
	
	/** A zero byte value */
	public static final byte ZERO_BYTE = 0;

	
	
	
	/** The number of slots added in a resize */
	public final static int ALLOC_SIZE = ConfigurationHelper.getIntSystemThenEnvProperty(RESIZE_SLOTS_ALLOC, DEFAULT_RESIZE_SLOTS_ALLOC);
	/** The number of slots initially allocated */
	public final static int INIT_ALLOC = ConfigurationHelper.getIntSystemThenEnvProperty(INIT_SLOTS_ALLOC, DEFAULT_INIT_SLOTS_ALLOC);
	/** The maximum number of slots that can be allocated */
	public final static int MAX_SIZE = ConfigurationHelper.getIntSystemThenEnvProperty(MAX_SLOTS_ALLOC, DEFAULT_MAX_SLOTS_ALLOC);
	
	
	
	
	public int capacity() {
		return UnsafeAdapter.getInt(address + CAPACITY);
	}
	
	public int size() {
		return UnsafeAdapter.getInt(address + SIZE);
	}
	
	public long getLong(int index) {
		return UnsafeAdapter.getLong(address + DATA + (index << 3));
	}

	public long[] getLongs() {
		return UnsafeAdapter.getLongArray(address + DATA, size());
	}
	

	public double getDouble(int index) {
		return UnsafeAdapter.getDouble(address + DATA + (index << 3));
	}

	public double[] getDoubles() {
		return UnsafeAdapter.getDoubleArray(address + DATA, size());
	}

	public void append(long value) {
		int size = checkCap();
		UnsafeAdapter.putLong(address + DATA + (size << 3), value);
	}
	
	public void append(double value) {
		int size = checkCap();
		UnsafeAdapter.putDouble(address + DATA + (size << 3), value);
	}
	
	
	protected int checkCap() {
		int cap = capacity();
		int size = size();
		if(size() == cap) {
			if(cap>=MAX_SIZE) {
				roll(cap);
			} else {
				if(resize(size)) {
					roll(cap);
				} else {
					internal = upgrade(cap, size);
					address = internal.address;
					size++;
				}
			}
		} else {
			size++;			
		}
		return size;
	}
	
	protected void roll(int capacity) {
		UnsafeAdapter.copyMemory(address + DATA + UnsafeAdapter.LONG_SIZE, address + DATA , (capacity << 3) - UnsafeAdapter.LONG_SIZE);
	}
	
	
	
	protected SwappableRawDataContainer upgrade(int currentCapacity, int currentSize) {
		int newCap = currentCapacity + ALLOC_SIZE;
		long dataSegment = newCap << 3;
		long newAddress = UnsafeAdapter.allocateMemory(DATA + dataSegment);
		UnsafeAdapter.putInt(newAddress + CAPACITY, newCap);
		UnsafeAdapter.putInt(newAddress + SIZE, currentSize);		
		internal = new SwappableRawDataContainer(newAddress-(DATA), address, currentSize << 3 );
		return internal;
	}
	
	protected boolean resize(int currentSize) {
		int allocationIncrement = (currentSize + ALLOC_SIZE > MAX_SIZE) ? MAX_SIZE - currentSize : ALLOC_SIZE;
		if(allocationIncrement==0) {			
			return true;  // now we're rolling
		}		
		return false;  // swap out for an upgrade
	}
}
