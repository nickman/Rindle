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
package org.helios.rindle.store.chronicle;

import java.util.Map;

/**
 * <p>Title: ILongKeyCache</p>
 * <p>Description: Defines the long key cache used to associate the Chronicle index (long) to the global id (long).</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.ILongKeyCache</code></p>
 */

public interface ILongKeyCache extends IKeyCache {
	/**
	 * Determines if the cache contains the passed key
	 * @param key The key to check for
	 * @return true if found, false otherwise
	 */
	public boolean containsKey(long key);


	/**
	 * Retrieves the long keyed by the passed key
	 * @param key The stringy key
	 * @return the located long or {@link #NO_ENTRY_VALUE}  if not found
	 */
	public long get(long key);

	/**
	 * Inserts the passed key/value into the cache
	 * @param key The key
	 * @param value The value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 */
	public long put(long key, long value);

	/**
	 * Inserts the passed key/value into the cache if the key is not already bound
	 * @param key The key
	 * @param value The value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 */
	public long putIfAbsent(long key, long value);

	/**
	 * Removes the mapping for a key from this map if it is present 
	 * @param key The key
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 */
	public long remove(long key);

	/**
	 * Inserts the passed map of values into the cache
	 * @param map a map of long keys and long values
	 */
	public void putAll(Map<Long, Long> map);
	
	/**
	 * Adjusts the primitive value mapped to the key if the key is present in the map.
	 * @param key The key
	 * @param value The value
	 * @return true if a mapping was found and modified.
	 */
	public boolean adjustValue(long key, long value);

}
