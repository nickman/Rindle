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

import java.util.Map;

/**
 * <p>Title: IByteArrayKeyCache</p>
 * <p>Description: Defines a chronicle global id cache keyed by the opaque metric id (a byte array)</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.IByteArrayKeyCache</code></p>
 */

public interface IByteArrayKeyCache extends IKeyCache {
	
    /** the load above which rehashing occurs. */
    public static final float DEFAULT_LOAD_FACTOR = 0.5f;
	
	/** The cache no entry value, meaning a non-existent value not in the cache */
	public static final long NO_ENTRY_VALUE = -1L;
	

	/**
	 * Determines if the cache contains the passed byte array value
	 * @param key The byte array to check for
	 * @return true if found, false otherwise
	 * @see gnu.trove.map.hash.TObjectLongHashMap#containsKey(java.lang.Object)
	 */
	public boolean containsKey(byte[] key);


	/**
	 * Retrieves the long keyed by the passed byte array
	 * @param key The byte array key
	 * @return the located long or {@link #NO_ENTRY_VALUE}  if not found
	 * @see gnu.trove.map.hash.TObjectLongHashMap#get(java.lang.Object)
	 */
	public long get(byte[] key);

	/**
	 * Inserts the passed key/value into the cache
	 * @param key The byte array key
	 * @param value The value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#put(java.lang.Object, long)
	 */
	public long put(byte[] key, long value);

	/**
	 * Inserts the passed key/value into the cache if the key is not already bound
	 * @param key The byte array key
	 * @param value The value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#putIfAbsent(java.lang.Object, long)
	 */
	public long putIfAbsent(byte[] key, long value);

	/**
	 * Removes the mapping for a key from this map if it is present 
	 * @param key The byte array key
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#remove(java.lang.Object)
	 */
	public long remove(byte[] key);

	/**
	 * Inserts the passed map of values into the cache
	 * @param map a map of stringy keys and long values
	 * @see gnu.trove.map.hash.TObjectLongHashMap#putAll(java.util.Map)
	 */
	public void putAll(Map<byte[], Long> map);
	
	/**
	 * Adjusts the primitive value mapped to the key if the key is present in the map.
	 * @param key The byte array key
	 * @param value The value
	 * @return true if a mapping was found and modified.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#adjustValue(java.lang.Object, long)
	 */
	public boolean adjustValue(byte[] key, long value);
	

}
