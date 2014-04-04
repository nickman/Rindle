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
package org.helios.pag.metric;

import java.nio.charset.Charset;

import org.helios.pag.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: IMetricDefinition</p>
 * <p>Description: Defines a metric with a unique global ID, an optional name and an optional opaque key.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.IMetricDefinition</code></p>
 */

public interface IMetricDefinition {
	
	/** The offset of the deletion flag */
	public static final byte DELETE_FLAG = 0;
	/** The offset of the size field */
	public static final byte SIZE = DELETE_FLAG + 1;
	/** The offset of the id field */
	public static final byte ID = SIZE + UnsafeAdapter.INT_SIZE;
	/** The offset of the timestamp field */
	public static final byte TIMESTAMP = ID + UnsafeAdapter.LONG_SIZE;
	/** The offset of the metric name string size field */
	public static final byte NAME_SIZE = TIMESTAMP + UnsafeAdapter.LONG_SIZE;
	/** The offset of the metric opaque key byte size field */
	public static final byte OPAQUE_SIZE = NAME_SIZE + UnsafeAdapter.INT_SIZE;
	/** The offset of the metric name bytes field */
	public static final byte NAME_BYTES = OPAQUE_SIZE + UnsafeAdapter.INT_SIZE;
	/** The minimum size of a metric definition */
	public static final int BASE_SIZE = NAME_BYTES; 
	
	/** Empty byte array constant */
	public static final byte[] EMPTY_BYTE_ARR = {};
	/** The default charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	/** The cache no entry value, meaning a non-existent value not in the cache */
	public static final long NO_ENTRY_VALUE = -1L;
	
	
	/**
	 * Returns the metric global id
	 * @return the metric global id
	 */
	public long getId();
	/**
	 * Returns the metric created timestamp
	 * @return the metric created timestamp
	 */
	public long getCreatedTimestamp();
	/**
	 * Returns the metric name
	 * @return the metric name
	 */
	public String getName();
	/**
	 * Returns the metric opaque key
	 * @return the metric opaque key
	 */
	public byte[] getOpaqueKey();
}
