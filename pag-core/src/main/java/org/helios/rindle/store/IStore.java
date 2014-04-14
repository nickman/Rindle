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
package org.helios.rindle.store;

import org.helios.rindle.RindleService;

/**
 * <p>Title: IStore</p>
 * <p>Description: Defines a Rindle metric dictionary store</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.IStore</code></p>
 */

public interface IStore extends RindleService {
	/**
	 * Purges the store
	 */
	public void purge();
	
	/**
	 * Returns the global metric id for the passed name and opaque key
	 * @param name The metric name
	 * @param opaqueKey The metric opaque key
	 * @return The global id or -1 if both the name and opaque key were null
	 */
	public long[] getGlobalId(String name, byte[] opaqueKey);
	
	/**
	 * Returns the global metric id for the passed name
	 * @param name The metric name
	 * @return The global id or -1 if the name was null
	 */
	public long getGlobalId(String name);

	/**
	 * Returns the global metric id for the passed opaque key
	 * @param opaqueKey The metric opaque key
	 * @return The global id or -1 if the opaque key was null
	 */
	public long getGlobalId(byte[] opaqueKey);
	
}
