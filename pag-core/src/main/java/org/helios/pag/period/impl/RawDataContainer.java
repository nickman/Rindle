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

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.pag.Stats;
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

public class RawDataContainer implements DeAllocateMe {
	private static final Logger LOG = LogManager.getLogger(RawDataContainer.class);
//	/**
//	 * <p>Title: SwappableRawDataContainer</p>
//	 * <p>Description: The internal swappable container</p>
//	 * <p>Company: Helios Development Group LLC</p>
//	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
//	 * <p><b><code>org.helios.pag.period.impl.RawDataContainer.SwappableRawDataContainer</code></b>
//	 */
//	private class SwappableRawDataContainer implements DeAllocateMe {		
//		/** The address of the swappable container */
//		private final long[] _address = new long[1];
//
//		public SwappableRawDataContainer(long address) {
//			this._address[0] = address;
//			UnsafeAdapter.registerForDeAlloc(this);
//		}
//		
//		@Override
//		public long[] getAddresses() {
//			return _address;
//		}		
//		
//		/**
//		 * Reallocates the memory allocation for this container to enlarge or shrink.
//		 * @param newSize The new size of the allocation in bytes
//		 * @return the new address
//		 */
//		private long reallocate(long newSize) {
//			long newAddress = UnsafeAdapter.reallocateAlignedMemory(_address[0], newSize);
//			_address[0] = newAddress;			
//			return newAddress;
//		}
//		
//		
//	}
	
	
	
	/** The address of the swappable container */
	private long[] address = new long[1];
//	/** The swappable container */
//	private SwappableRawDataContainer internal = null;
	

	@Override
	public long[][] getAddresses() {
		return new long[][] {address};
	}		
	
	/**
	 * Reallocates the memory allocation for this container to enlarge or shrink.
	 * @param newSize The new size of the allocation in bytes
	 * @return the new address
	 */
	private long reallocate(long newSize) {
		long newAddress = UnsafeAdapter.reallocateAlignedMemory(address[0], newSize);
		address[0] = newAddress;			
		return newAddress;
	}
	
	/**
	 * Creates a new raw data container
	 * @return a new raw data container
	 */
	public static RawDataContainer newInstance() {
		return new RawDataContainer(INIT_ALLOC);  // TODO: Swap out for an iface
	}
	
	public String toString() {
		int size = size();
		return String.format("RDC: cap:%s, size:%s, data:%s", capacity(), size, size <= 128 ? Arrays.toString(getLongs()) : "[elements:]" + size);
	}
	

	/**
	 * Creates a new RawDataContainer of the specified number of elements minus one, meaning it will hold the capacity, current size and <b><code>totalSize-1</code></b> raw data elements.
	 * @param totalSize The size of the container to create specified as the number of elements it can hold
	 */
	private RawDataContainer(int totalSize) {
		final int totalBytes = totalSize << 3;
		address[0] = UnsafeAdapter.allocateAlignedMemory(totalBytes);
		UnsafeAdapter.putInt(address[0] + CAPACITY, totalSize -1);
		UnsafeAdapter.putInt(address[0] + SIZE, 0);
//		UnsafeAdapter.setMemory(address[0] + DATA, totalBytes - DATA, ZERO_BYTE);
		LOG.info("New Cap: {}, Size: {}", capacity(), size());
//		internal = new SwappableRawDataContainer(address);	
		UnsafeAdapter.registerForDeAlloc(this);
	}
	
	/**
	 * Reallocates the swappable raw data container
	 * @param currentCapacity The current capacity
	 * @param currentSize The current size
	 */
	protected void upgrade(int currentCapacity, int currentSize) {		
		final int totalBytes = (currentCapacity + 1 + ALLOC_SIZE) << 3;
//		if(LOG.isDebugEnabled()) LOG.debug("Resizing Raw Container @ {}--({}) from {} to {}", address, UnsafeAdapter.sizeOf(address[0]), currentCapacity, currentCapacity + ALLOC_SIZE);
		reallocate(totalBytes);
//		LOG.debug("Resized....");
		UnsafeAdapter.putInt(address[0] + CAPACITY , currentCapacity + ALLOC_SIZE);
//		if(LOG.isDebugEnabled()) LOG.debug("Resized Raw Container @ {}--({}) to {}", address, UnsafeAdapter.sizeOf(address[0]), capacity());		
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
	
	
	
	
	/**
	 * Returns the capacity of this raw data container
	 * @return the capacity of this raw data container
	 */
	public int capacity() {
		return UnsafeAdapter.getInt(address[0] + CAPACITY);
	}
	
	/**
	 * Returns the size of this raw data container, i.e. the number of allocated data slot
	 * @return the size of this raw data container
	 */
	public int size() {
		return UnsafeAdapter.getInt(address[0] + SIZE);
	}
	
	/**
	 * Increments the number of allocated slots in this container
	 * @param count the number of slots to increment by
	 * @return the new size
	 */
	protected int incrementSize(int count) {
		int now = UnsafeAdapter.getInt(address[0] + SIZE) + count; 
		UnsafeAdapter.putInt(address[0] + SIZE, now);
		return now;
	}
	
	/**
	 * Returns the long at the specified index 
	 * @param index The index from which to retrieve the long value
	 * @return the long at the specified index
	 */
	public long getLong(int index) {
		return UnsafeAdapter.getLong(address[0] + DATA + (index << 3));
	}

	/**
	 * Returns the raw data buffer as a long array
	 * @return a long array
	 */
	public long[] getLongs() {
		return UnsafeAdapter.getLongArray(address[0] + DATA, size());
	}
	
	/**
	 * Returns the median of the raw data
	 * @return the median of the raw data
	 */
	public double getDoubleMedian() {
		return Stats.mediand(address[0] + DATA, size());
	}
	
	/**
	 * Returns the median of the raw data
	 * @return the median of the raw data
	 */
	public long getLongMedian() {
		return Stats.medianl(address[0] + DATA, size());
	}
	

	/**
	 * Returns the double at the specified index 
	 * @param index The index from which to retrieve the double value
	 * @return the double at the specified index
	 */
	public double getDouble(int index) {
		return UnsafeAdapter.getDouble(address[0] + DATA + (index << 3));
	}

	/**
	 * Returns the raw data buffer as a double array
	 * @return a double array
	 */
	public double[] getDoubles() {
		return UnsafeAdapter.getDoubleArray(address[0] + DATA, size());
	}

	/**
	 * Appends a value to this raw data container
	 * @param value The value to append
	 */
	public void append(long value) {
		int size;
		if(size() == capacity()) {
			size = checkCap();
			UnsafeAdapter.putInt(address[0] + SIZE, size);
		} else { 
			size = incrementSize(1);
		}		
		UnsafeAdapter.putLong(address[0] + DATA + ((size-1)<< 3), value);
	}
	
	/**
	 * Appends a value to this raw data container
	 * @param value The value to append
	 */
	public void append(double value) {
		int size;
		if(size() == capacity()) {
			size = checkCap();
			UnsafeAdapter.putInt(address[0] + SIZE, size);
		} else { 
			size = incrementSize(1);
		}				
		UnsafeAdapter.putDouble(address[0] + DATA + ((size-1)<< 3), value);
	}
	
	
	/**
	 * Checks the capacity of the swappable container and returns the new size.
	 * If the current container is full, it will be extended before this call returns.
	 * If the current container is full and at maximum capacity, the container will be put into roll mode.
	 * @return the new size of the container (i.e. the number of occupied data slots)
	 */
	protected int checkCap() {
		int cap = capacity();
		int size = size();
		if(size() == cap) {
			if(cap>=MAX_SIZE) {
				roll(cap);
			} else {
				if(shouldStartRolling(size)) {
					roll(cap);
				} else {
					upgrade(cap, size);
					size++;
				}
			}
		} else {
			size++;			
		}
		return size;
	}
	
	/**
	 * Rolls the raw data container, discarding the oldest value and making room for a new one
	 * @param capacity The current capacity of the container
	 */
	protected void roll(int capacity) {
		UnsafeAdapter.copyMemory(address[0] + DATA + UnsafeAdapter.LONG_SIZE, address[0] + DATA , (capacity << 3) - UnsafeAdapter.LONG_SIZE);
	}
	
	
	
	
	/**
	 * Figures out if the container can be upgraded, or if it needs to go into roll mode
	 * @param currentSize The current size (and assumed capacity) of the container
	 * @return true if the container should go into roll mode, false if it can be upgraded
	 */
	protected boolean shouldStartRolling(int currentSize) {
		int allocationIncrement = (currentSize + ALLOC_SIZE > MAX_SIZE) ? MAX_SIZE - currentSize : ALLOC_SIZE;
		if(allocationIncrement==0) {		
			LOG.info("\n\t---->Container is rolling");
			return true;  // now we're rolling
		}		
		return false;  // swap out for an upgrade
	}
}
