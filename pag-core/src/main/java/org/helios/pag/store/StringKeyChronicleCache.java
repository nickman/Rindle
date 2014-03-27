/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2014, Helios Development Group and individual contributors
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
package org.helios.pag.store;

import static org.helios.pag.util.unsafe.UnsafeAdapter.xlock;
import static org.helios.pag.util.unsafe.UnsafeAdapter.xunlock;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.helios.pag.util.unsafe.DeAllocateMe;
import org.helios.pag.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: StringKeyChronicleCache</p>
 * <p>Description: A cache of chronicle keys (<b><code>long</code></b>s) keyed by a {@link String} pointer</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.StringKeyChronicleCache</code></p>
 */

public class StringKeyChronicleCache  implements DeAllocateMe {
	/** The lock address */
	protected final long lockAddress;
	/** The default platform charset */
	public static final Charset CHARSET = Charset.defaultCharset();
    /** the load above which rehashing occurs. */
    public static final float DEFAULT_LOAD_FACTOR = 0.5f;
	
	/** A thread local containing the string matcher for the currently executing lookup */
	private static final ThreadLocal<StringPointer> CURRENT_STRING_MATCHER = new ThreadLocal<StringPointer>();
	/** The cache no entry value, meaning a non-existent value not in the cache */
	public static final long NO_ENTRY_VALUE = -1L;

	/** The cache of Chronicle entry ids keyed by the string pointer of the name */
	private final TObjectLongHashMap<OffHeapKey<?>> cache;
//	private final ConcurrentHashMap<OffHeapKey<?>, Long> cache; 
	
	/**
	 * Creates a new StringKeyChronicleCache
     * @param initialCapacity used to find a prime capacity for the table.
     * @param loadFactor used to calculate the threshold over which rehashing takes place.
	 */
	public StringKeyChronicleCache(int initialCapacity, float loadFactor) {
		cache = new TObjectLongHashMap<OffHeapKey<?>>(initialCapacity, loadFactor, NO_ENTRY_VALUE);
//		cache = new ConcurrentHashMap<OffHeapKey<?>, Long>(initialCapacity);
		lockAddress = UnsafeAdapter.allocateSpinLock();		
		UnsafeAdapter.registerForDeAlloc(this);
	}
	
	/**
	 * Creates a new StringKeyChronicleCache with the default load factor
     * @param initialCapacity used to find a prime capacity for the table.
	 */
	public StringKeyChronicleCache(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.util.unsafe.DeAllocateMe#getAddresses()
	 */
	@Override
	public long[] getAddresses() {
		return new long[]{lockAddress};
	}
	
	public static long initCnt = 0;
	
	/**
	 * <p>Title: StringPointer</p>
	 * <p>Description: Off heap representation of string to be used as a key in a {@link StringKeyChronicleCache}</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>org.helios.pag.store.StringKeyChronicleCacheStringPointer</code></p>
	 */
	public static class StringPointer implements DeAllocateMe, OffHeapKey<String> {
		/** The address of the StringPointer */
		protected final long address;
		
		/** The offset of the represented string's hashcode */
		public static final byte HASH_CODE = 0;
		/** The offset of the represented string's byte length */
		public static final byte LENGTH = HASH_CODE + UnsafeAdapter.INT_SIZE;
		/** The offset of the represented string's bytes */
		public static final byte BYTES = LENGTH + UnsafeAdapter.INT_SIZE;
		
		/** The size of the header in bytes */
		public static final byte HEADER_SIZE = UnsafeAdapter.INT_SIZE*2;
		
		
		/**
		 * Creates a new StringPointer
		 * @param s The stringy to pointerize
		 */
		public StringPointer(CharSequence s) {
			if(s==null) throw new IllegalArgumentException("The passed charsequence was null");
			initCnt++;
			String _s = s.toString();
			byte[] bytes = _s.getBytes(CHARSET);			
			address = UnsafeAdapter.allocateAlignedMemory(bytes.length + HEADER_SIZE);
			UnsafeAdapter.putInt(address + HASH_CODE, _s.hashCode());
			UnsafeAdapter.putInt(address + LENGTH, bytes.length);
			UnsafeAdapter.copyMemory(bytes, UnsafeAdapter.BYTE_ARRAY_OFFSET, null, address + BYTES, bytes.length);
			UnsafeAdapter.registerForDeAlloc(this);
		}
		
		
		
		/**
		 * {@inheritDoc}
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return UnsafeAdapter.getInt(address + HASH_CODE);
		}
		
		/**
		 * Determines if the passed StringPointer is equal to this one
		 * @param sp The StringPointer to compare to this one
		 * @return true if equal, false otherwise
		 */
		protected boolean equalsStringPointer(StringPointer sp) {
			if(sp==null) return false;
			int myLength = UnsafeAdapter.getInt(address + LENGTH);
			int otherLength = UnsafeAdapter.getInt(sp.address + LENGTH);
			if(myLength != otherLength) return false;
			return UnsafeAdapter.compareTo(address, myLength, sp.address, otherLength);			
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if(o==null) return false;
			byte[] bytes = null;
			if(o instanceof StringPointer) {
				return equalsStringPointer((StringPointer)o);
			}
			if(o instanceof CharSequence) {
				if(o instanceof String) {
					bytes = ((String)o).getBytes(CHARSET);
				} else {
					bytes = ((CharSequence)o).toString().getBytes(CHARSET);
				}
				return UnsafeAdapter.byteArraysEqual(getBytes(), bytes);
			}
			return false;
		}

		
		/**
		 * Returns the byte content of this pointer
		 * @return the byte content of this pointer
		 */
		public byte[] getBytes() {
			return UnsafeAdapter.getByteArray(address + BYTES, getLength());
		}
		
		/**
		 * Returns the number of bytes for the string pointer's content
		 * @return the number of bytes for the string pointer's content
		 */
		protected int getLength() {
			return UnsafeAdapter.getInt(address + LENGTH);
		}
		
		/**
		 * <p>Dereferences the pointer</p>
		 * {@inheritDoc}
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return new String(getBytes(), CHARSET);
		}
		
		
		/**
		 * {@inheritDoc}
		 * @see org.helios.pag.util.unsafe.DeAllocateMe#getAddresses()
		 */
		@Override
		public long[] getAddresses() {
			return new long[]{address};
		}
		
	}
	
	
	/**
	 * Sets the current StringPointer we're matching against in the current thread.
	 * This avoids having to recreate the StringPointer over-and-over.
	 * @param stringy The stringy to pointerize
	 * @return The created string pointer
	 */
	private static StringPointer initCurrent(CharSequence stringy) {		
		StringPointer sp = new StringPointer(stringy);
//		CURRENT_STRING_MATCHER.set(sp);
		return sp;
	}
	
	/**
	 * Removes the current thread's stringy StringPointer.
	 */
	private static void killCurrent() {
//		CURRENT_STRING_MATCHER.remove();
	}

	/**
	 * Returns the size of the cache
	 * @return the size of the cache
	 * @see gnu.trove.impl.hash.THash#size()
	 */
	public int size() {
		try {
			xlock(lockAddress);
			return cache.size();
		} finally {
			xunlock(lockAddress);
		}
	}

	/**
	 * Determines if the cache contains the passed stringy value
	 * @param key The stringy to check for
	 * @return true if found, false otherwise
	 * @see gnu.trove.map.hash.TObjectLongHashMap#containsKey(java.lang.Object)
	 */
	public boolean containsKey(CharSequence key) {
		if(key==null) return false;
		try {
			StringPointer sp = initCurrent(key);
			xlock(lockAddress);
			if(cache.isEmpty()) return false;
			return cache.containsKey(sp);
		} finally {
			xunlock(lockAddress);
			killCurrent();
		}
	}
	
	/**
	 * Clears the cache
	 */
	public void clear() {
		try {			
			xlock(lockAddress);
			cache.clear();
		} finally {
			xunlock(lockAddress);
			killCurrent();
		}		
	}
	
	private OffHeapKey<String> strOffHeap(CharSequence key) {
		final String s = key.toString();
		return new OffHeapKey<String>() {
			/**
			 * {@inheritDoc}
			 * @see java.lang.Object#hashCode()
			 */
			@Override
			public int hashCode() {
				return s.hashCode();
			}
			
			/**
			 * {@inheritDoc}
			 * @see org.helios.pag.store.OffHeapKey#getBytes()
			 */
			@Override
			public byte[] getBytes() {				
				return s.getBytes(CHARSET);
			}
			
			/**
			 * {@inheritDoc}
			 * @see java.lang.Object#equals(java.lang.Object)
			 */
			@Override
			public boolean equals(Object obj) {
				if(obj==null || !(obj instanceof OffHeapKey)) return false;
				return UnsafeAdapter.byteArraysEqual(getBytes(), ((OffHeapKey)obj).getBytes());
			}
		};
	}

	/**
	 * Retrieves the long keyed by the passed stringy
	 * @param key The stringy key
	 * @return the located long or {@link #NO_ENTRY_VALUE}  if not found
	 * @see gnu.trove.map.hash.TObjectLongHashMap#get(java.lang.Object)
	 */
	public long get(CharSequence key) {
		if(key==null) return NO_ENTRY_VALUE;
		try {
			//StringPointer sp = initCurrent(key);
			OffHeapKey<String> sp = strOffHeap(key);
			xlock(lockAddress);
			if(cache.isEmpty()) return NO_ENTRY_VALUE;
			Long l = cache.get(sp);
			if(l==null) return NO_ENTRY_VALUE;
			return l.longValue();
		} finally {
			xunlock(lockAddress);
			killCurrent();
		}
	}

	/**
	 * Inserts the passed key/value into the cache
	 * @param key The stringy key
	 * @param value The value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#put(java.lang.Object, long)
	 */
	public void put(CharSequence key, long value) {
		if(key==null) throw new IllegalArgumentException("The passed key was null");
		try {
//			StringPointer sp = initCurrent(key);
			OffHeapKey<String> sp = strOffHeap(key);
			xlock(lockAddress);			
			cache.put(sp, value);
		} finally {
			xunlock(lockAddress);
			killCurrent();
		}
	}
	
	/**
	 * Unguarded direct put for bulk puts
	 * @param sp The StringPointer key
	 * @param value The long value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 */
	protected long _put(StringPointer sp, long value) {
		return cache.put(sp, value);
	}

	/**
	 * Inserts the passed key/value into the cache if the key is not already bound
	 * @param key The stringy key
	 * @param value The value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#putIfAbsent(java.lang.Object, long)
	 */
	public long putIfAbsent(CharSequence key, long value) {
		if(key==null) throw new IllegalArgumentException("The passed key was null");
		try {
			StringPointer sp = initCurrent(key);
			xlock(lockAddress);			
			return cache.putIfAbsent(sp, value);
		} finally {
			xunlock(lockAddress);
			killCurrent();
		}
	}
	
	/**
	 * Removes the mapping for a key from this map if it is present 
	 * @param key The stringy key
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#remove(java.lang.Object)
	 */
	public long remove(CharSequence key) {
		if(key==null) throw new IllegalArgumentException("The passed key was null");
		try {
			StringPointer sp = initCurrent(key);
			xlock(lockAddress);			
			return cache.remove(sp);
		} finally {
			xunlock(lockAddress);
			killCurrent();
		}
	}

	
	/**
	 * Inserts the passed map of values into the cache
	 * @param map a map of stringy keys and long values
	 * @see gnu.trove.map.hash.TObjectLongHashMap#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends CharSequence, ? extends Long> map) {
		if(map==null) throw new IllegalArgumentException("The passed map was null");
		if(map.isEmpty()) return;		
		try {			
			xlock(lockAddress);
			for(Map.Entry<? extends CharSequence, ? extends Long> entry: map.entrySet()) {
				StringPointer sp = initCurrent(entry.getKey());
				_put(sp, entry.getValue().longValue());
			}
		} finally {
			xunlock(lockAddress);
			killCurrent();
		}
	}

//	/**
//	 * Adjusts the primitive value mapped to the key if the key is present in the map.
//	 * @param key The stringy key
//	 * @param value The value
//	 * @return true if a mapping was found and modified.
//	 * @see gnu.trove.map.hash.TObjectLongHashMap#adjustValue(java.lang.Object, long)
//	 */
//	public boolean adjustValue(CharSequence key, long value) {
//		if(key==null) throw new IllegalArgumentException("The passed key was null");		
//		try {			
//			StringPointer sp = initCurrent(key);
//			xlock(lockAddress);
//			return cache.adjustValue(sp, value);
//		} finally {
//			xunlock(lockAddress);
//			killCurrent();
//		}
//	}

//	/**
//	 * Compresses the cache to the minimum prime size 
//	 * @see gnu.trove.impl.hash.THash#trimToSize()
//	 */
//	public final void trimToSize() {
//		try {
//			xlock(lockAddress);			
//			cache.trimToSize();
//		} finally {
//			xunlock(lockAddress);
//		}
//	}
//	
//
//    /**
//     * The auto-compaction factor controls whether and when a table performs a
//     * {@link THash#compact} automatically after a certain number of remove operations.
//     * If the value is non-zero, the number of removes that need to occur for
//     * auto-compaction is the size of table at the time of the previous compaction
//     * (or the initial capacity) multiplied by this factor.
//     * <p/>
//     * Setting this value to zero will disable auto-compaction.
//     * @param factor a <tt>float</tt> that indicates the auto-compaction factor
//     * @see gnu.trove.impl.hash.THash#setAutoCompactionFactor(float)
//     */
//
//	public void setAutoCompactionFactor(float factor) {
//		try {
//			xlock(lockAddress);			
//			cache.setAutoCompactionFactor(factor);
//		} finally {
//			xunlock(lockAddress);
//		}
//	}
//
//
//	/**
//	 * Returns the cache's auto compaction factor
//	 * @return a <<tt>float</tt> that represents the auto-compaction factor.
//	 * @see gnu.trove.impl.hash.THash#getAutoCompactionFactor()
//	 */
//	public float getAutoCompactionFactor() {
//		try {
//			xlock(lockAddress);			
//			return cache.getAutoCompactionFactor();
//		} finally {
//			xunlock(lockAddress);
//		}
//	}
//
//
//	/**
//	 * Temporarily disables auto-compaction. MUST be followed by calling {@link #reenableAutoCompaction}.
//	 * @see gnu.trove.impl.hash.THash#tempDisableAutoCompaction()
//	 */
//	public void tempDisableAutoCompaction() {
//		try {
//			xlock(lockAddress);			
//			cache.tempDisableAutoCompaction();
//		} finally {
//			xunlock(lockAddress);
//		}
//	}
//
//	/**
//     * Re-enable auto-compaction after it was disabled via {@link #tempDisableAutoCompaction()}.     
//     * @param check_for_compaction True if compaction should be performed if needed
//     * before returning. If false, no compaction will be performed.
//	 * @see gnu.trove.impl.hash.THash#reenableAutoCompaction(boolean)
//	 */
//	public void reenableAutoCompaction(boolean check_for_compaction) {
//		try {
//			xlock(lockAddress);			
//			cache.reenableAutoCompaction(check_for_compaction);
//		} finally {
//			xunlock(lockAddress);
//		}				
//	}

}
