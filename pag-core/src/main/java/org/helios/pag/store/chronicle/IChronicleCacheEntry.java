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
package org.helios.pag.store.chronicle;

import java.nio.charset.Charset;

import org.helios.pag.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: IChronicleCacheEntry</p>
 * <p>Description: Defines a ChronicleCacheEntry representation of a metric and associated cacke keys</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.IChronicleCacheEntry</code></p>
 */

public interface IChronicleCacheEntry {
	/** The default charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	
	/** The excerpt total size of an ID only entry */
	public static int ENTRY_ID_ONLY_SIZE = UnsafeAdapter.LONG_SIZE + 1;
	
	/** The excerpt offset of the entry delete indicator */
	public static int DELETE_OFFSET = 0;
	/** The excerpt offset of the entry creation timestamp */
	public static int TIMESTAMP_OFFSET = 1 + DELETE_OFFSET;
	/** The excerpt offset of the length of the metric name */
	public static int NAME_LENGTH_OFFSET = UnsafeAdapter.LONG_SIZE + TIMESTAMP_OFFSET;
	/** The excerpt offset of the length of the metric byte array */
	public static int BYTES_LENGTH_OFFSET = UnsafeAdapter.INT_SIZE + NAME_LENGTH_OFFSET;
	/** The excerpt offset of the start of the metric name */
	public static int NAME_OFFSET = UnsafeAdapter.INT_SIZE + BYTES_LENGTH_OFFSET;

}
