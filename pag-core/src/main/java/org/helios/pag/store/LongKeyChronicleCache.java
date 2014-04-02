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

import gnu.trove.map.hash.TLongLongHashMap;

import java.util.Map;

import org.helios.pag.util.unsafe.UnsafeAdapter;
import org.helios.pag.util.unsafe.UnsafeAdapter.SpinLock;

/**
 * <p>Title: StringKeyChronicleCache</p>
 * <p>Description: A cache of chronicle keys (<b><code>long</code></b>s) keyed by the global ID</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.LongKeyChronicleCache</code></p>
 */

public class LongKeyChronicleCache  implements ILongKeyCache {
	
	/** The spin lock */
	protected final SpinLock lock = UnsafeAdapter.allocateSpinLock();

	/** The cache of Chronicle entry ids keyed by the long hash code of the name */
	private final TLongLongHashMap cache;
	
	/**
	 * Creates a new LongKeyChronicleCache
     * @param initialCapacity used to find a prime capacity for the table.
     * @param loadFactor used to calculate the threshold over which rehashing takes place.
	 */
	public LongKeyChronicleCache(int initialCapacity, float loadFactor) {
		cache = new TLongLongHashMap(initialCapacity, loadFactor, NO_ENTRY_VALUE, NO_ENTRY_VALUE);
	}
	
	/**
	 * LongKeyChronicleCache Copy Ctor
	 * @param otherCache the cache to copy
	 */
	public LongKeyChronicleCache(LongKeyChronicleCache otherCache) {
		cache = new TLongLongHashMap(otherCache.cache);
	}
	
	/**
	 * Creates a new LongKeyChronicleCache with the default load factor
     * @param initialCapacity used to find a prime capacity for the table.
	 */
	public LongKeyChronicleCache(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}
	
	

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#size()
	 */
	@Override
	public int size() {
		try {
			lock.xlock();
			return cache.size();
		} finally {
			lock.xunlock();
		}
	}


	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.ILongKeyCache#containsKey(long)
	 */
	@Override
	public boolean containsKey(long key) {
		try {			
			lock.xlock();
			if(cache.isEmpty()) return false;			
			return cache.containsKey(key);
		} finally {
			lock.xunlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#clear()
	 */
	@Override
	public void clear() {
		try {			
			lock.xlock();
			cache.clear();
		} finally {
			lock.xunlock();
		}		
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IKeyCache#purge()
	 */
	@Override
	public void purge() {
		try {			
			lock.xlock();
			cache.clear();
			cache.trimToSize();		
		} finally {
			lock.xunlock();
		}		
	}	
	

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.ILongKeyCache#get(long)
	 */
	@Override
	public long get(long key) {
		try {
			lock.xlock();
			if(cache.isEmpty()) return NO_ENTRY_VALUE;
			return cache.get(key);
		} finally {
			lock.xunlock();
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.ILongKeyCache#put(long, long)
	 */
	@Override
	public long put(long key, long value) {	
		try {
			lock.xlock();			
			return cache.put(key, value);
		} finally {
			lock.xunlock();
		}
	}
	
	/**
	 * Unguarded direct put for bulk puts
	 * @param key The key
	 * @param value The long value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 */
	protected long _put(long key, long value) {
		return cache.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.ILongKeyCache#putIfAbsent(long, long)
	 */
	@Override
	public long putIfAbsent(long key, long value) {
		try {
			lock.xlock();			
			return cache.putIfAbsent(key, value);
		} finally {
			lock.xunlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.ILongKeyCache#remove(long)
	 */
	@Override
	public long remove(long key) {
		try {			
			lock.xlock();			
			return cache.remove(key);
		} finally {
			lock.xunlock();
		}
	}

	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<Long, Long> map) {
		if(map==null) throw new IllegalArgumentException("The passed map was null");
		if(map.isEmpty()) return;
		try {			
			lock.xlock();
			for(Map.Entry<Long, Long> entry: map.entrySet()) {
				if(entry.getValue()==null) continue;
				_put(entry.getKey().longValue(), entry.getValue().longValue());
			}
		} finally {
			lock.xunlock();
		}
	}

	/**
	 * Adjusts the primitive value mapped to the key if the key is present in the map.
	 * @param key The key
	 * @param value The value
	 * @return true if a mapping was found and modified.
	 */
	public boolean adjustValue(long key, long value) {
		try {						
			lock.xlock();
			return cache.adjustValue(key, value);
		} finally {
			lock.xunlock();			
		}
	}

	/**
	 * Compresses the cache to the minimum prime size 
	 * @see gnu.trove.impl.hash.THash#trimToSize()
	 */
	public final void trimToSize() {
		try {
			lock.xlock();			
			cache.trimToSize();
		} finally {
			lock.xunlock();
		}
	}
	

    /**
     * The auto-compaction factor controls whether and when a table performs a
     * compaction automatically after a certain number of remove operations.
     * If the value is non-zero, the number of removes that need to occur for
     * auto-compaction is the size of table at the time of the previous compaction
     * (or the initial capacity) multiplied by this factor.
     * <p/>
     * Setting this value to zero will disable auto-compaction.
     * @param factor a <tt>float</tt> that indicates the auto-compaction factor
     * @see gnu.trove.impl.hash.THash#setAutoCompactionFactor(float)
     */

	public void setAutoCompactionFactor(float factor) {
		try {
			lock.xlock();			
			cache.setAutoCompactionFactor(factor);
		} finally {
			lock.xunlock();
		}
	}


	/**
	 * Returns the cache's auto compaction factor
	 * @return a <<tt>float</tt> that represents the auto-compaction factor.
	 * @see gnu.trove.impl.hash.THash#getAutoCompactionFactor()
	 */
	public float getAutoCompactionFactor() {
		try {
			lock.xlock();			
			return cache.getAutoCompactionFactor();
		} finally {
			lock.xunlock();
		}
	}


	/**
	 * Temporarily disables auto-compaction. MUST be followed by calling {@link #reenableAutoCompaction}.
	 * @see gnu.trove.impl.hash.THash#tempDisableAutoCompaction()
	 */
	public void tempDisableAutoCompaction() {
		try {
			lock.xlock();			
			cache.tempDisableAutoCompaction();
		} finally {
			lock.xunlock();
		}
	}

	/**
     * Re-enable auto-compaction after it was disabled via {@link #tempDisableAutoCompaction()}.     
     * @param check_for_compaction True if compaction should be performed if needed
     * before returning. If false, no compaction will be performed.
	 * @see gnu.trove.impl.hash.THash#reenableAutoCompaction(boolean)
	 */
	public void reenableAutoCompaction(boolean check_for_compaction) {
		try {
			lock.xlock();			
			cache.reenableAutoCompaction(check_for_compaction);
		} finally {
			lock.xunlock();
		}				
	}

}
