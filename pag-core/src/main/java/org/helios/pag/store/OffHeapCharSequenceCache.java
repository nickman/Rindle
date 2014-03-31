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
package org.helios.pag.store;

import java.util.Map;
import java.util.Map.Entry;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

/**
 * <p>Title: OffHeapCharSequenceCache</p>
 * <p>Description: An {@link IStringKeyCache} implemented with an OffHeap CharSequence</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.OffHeapCharSequenceCache</code></p>
 */

public class OffHeapCharSequenceCache implements IStringKeyCache {
	/** The internal cache impl */
	protected final NonBlockingHashMap<OffHeapCharSequenceKey, Long> cache;
	
	
	/**
	 * Creates a new OffHeapCharSequenceCache
	 * @param initialCapacity The initial capacity of the cache
	 * @param factor The packing factor
	 */
	public OffHeapCharSequenceCache(int initialCapacity, float factor) {
		cache = new NonBlockingHashMap<OffHeapCharSequenceKey, Long>(initialCapacity);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#size()
	 */
	@Override
	public int size() {
		return cache.size();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#containsKey(java.lang.CharSequence)
	 */
	@Override
	public boolean containsKey(CharSequence key) {
		if(key==null) throw new IllegalArgumentException("The passed key was null");
		return cache.contains(key);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#clear()
	 */
	@Override
	public void clear() {
		cache.clear();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#get(java.lang.CharSequence)
	 */
	@Override
	public long get(CharSequence key) {
		if(key==null) throw new IllegalArgumentException("The passed key was null");
		Long value = cache.get(key);
		if(value==null) return NO_ENTRY_VALUE;
		return value.longValue();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#put(java.lang.CharSequence, long)
	 */
	@Override
	public long put(CharSequence key, long value) {
		if(key==null) throw new IllegalArgumentException("The passed key was null");
		Long val = cache.put(new OffHeapCharSequenceKey(key), value);
		return val==null ? NO_ENTRY_VALUE : val.longValue();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#putIfAbsent(java.lang.CharSequence, long)
	 */
	@Override
	public long putIfAbsent(CharSequence key, long value) {
		if(key==null) throw new IllegalArgumentException("The passed key was null");
		Long val = cache.putIfAbsent(new OffHeapCharSequenceKey(key), value);
		return val==null ? NO_ENTRY_VALUE : val.longValue();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#remove(java.lang.CharSequence)
	 */
	@Override
	public long remove(CharSequence key) {
		if(key==null) throw new IllegalArgumentException("The passed key was null");
		Long val = cache.remove(key);
		return val==null ? NO_ENTRY_VALUE : val.longValue();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends CharSequence, ? extends Long> map) {
		if(map==null) throw new IllegalArgumentException("The passed map was null");
		for(Entry<? extends CharSequence, ? extends Long> entry: map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IStringKeyCache#adjustValue(java.lang.CharSequence, long)
	 */
	@Override
	public boolean adjustValue(CharSequence key, long value) {
		if(key==null) throw new IllegalArgumentException("The passed key was null");
		return cache.put(new OffHeapCharSequenceKey(key), value) != NO_ENTRY_VALUE;		
	}

}
