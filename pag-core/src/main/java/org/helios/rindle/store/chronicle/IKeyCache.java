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
 * <p>Title: IKeyCache</p>
 * <p>Description: Common base interface for all key caches</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.IKeyCache</code></p>
 */

public interface IKeyCache {
    /** the load above which rehashing occurs. */
    public static final float DEFAULT_LOAD_FACTOR = 0.5f;
	
	/** The cache no entry value, meaning a non-existent value not in the cache */
	public static final long NO_ENTRY_VALUE = -1L;
	

	/**
	 * Returns the size of the cache
	 * @return the size of the cache
	 * @see gnu.trove.impl.hash.THash#size()
	 */
	public int size();


	/**
	 * Clears the cache, but does not trim the cache size.
	 */
	public void clear();

	/**
	 * Clears the cache, and trims the cache size to zero.
	 */
	public void purge();
	
	/**
	 * Removes all entries where the value is {@link #NO_ENTRY_VALUE}
	 */
	public void trimToSize();

}
