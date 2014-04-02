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

import java.nio.charset.Charset;
import java.util.Arrays;

import org.helios.pag.util.unsafe.DeAllocateMe;
import org.helios.pag.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: UnsafeMetricDefinition</p>
 * <p>Description: An {@link IMetricDefinition} with all values stored off heap</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.UnsafeMetricDefinition</code></p>
 */

public class UnsafeMetricDefinition implements IMetricDefinition, DeAllocateMe {
	/** The native address of the metric definition */
	protected final long[] address = new long[1];
	
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

	
	/**
	 * Creates a new UnsafeMetricDefinition
	 * @param id The global id of the metric
	 * @param name The metric name
	 * @param opaqueKey The opaque key
	 */
	protected UnsafeMetricDefinition(long id, String name, byte[] opaqueKey) {		
		byte[] nameBytes = getBytes(name);
		byte[] opaqueBytes = getBytes(opaqueKey);
		int size = BASE_SIZE + nameBytes.length + opaqueBytes.length;
		address[0] = UnsafeAdapter.allocateAlignedMemory(size);
		UnsafeAdapter.registerForDeAlloc(this);
		UnsafeAdapter.putByte(address[0], DELETE_FLAG);
		UnsafeAdapter.putInt(address[0] + SIZE, size);
		UnsafeAdapter.putLong(address[0] + ID, id);
		UnsafeAdapter.putLong(address[0] + TIMESTAMP, System.currentTimeMillis());
		UnsafeAdapter.putInt(address[0] + NAME_SIZE, nameBytes.length);
		UnsafeAdapter.putInt(address[0] + OPAQUE_SIZE, opaqueBytes.length);
		if(nameBytes.length>0) {
			UnsafeAdapter.copyMemory(nameBytes, UnsafeAdapter.BYTE_ARRAY_OFFSET, null, address[0] + NAME_BYTES, nameBytes.length);
		}
		if(opaqueBytes.length>0) {
			UnsafeAdapter.copyMemory(opaqueBytes, UnsafeAdapter.BYTE_ARRAY_OFFSET, null, address[0] + NAME_BYTES + nameBytes.length, opaqueBytes.length);
		}		
	}
	
	/**
	 * Returns the size of the name
	 * @return the size of the name
	 */
	protected int getNameSize() {
		return UnsafeAdapter.getInt(address[0] + NAME_SIZE);
	}
	
	/**
	 * Returns the size of the opaque key
	 * @return the size of the opaque key
	 */
	protected int getOpaqueSize() {
		return UnsafeAdapter.getInt(address[0] + OPAQUE_SIZE);
	}
	
	/**
	 * Updates the id of this metric
	 * @param id the new id
	 */
	protected void setId(long id) {
		UnsafeAdapter.putLong(address[0] + ID, id);
	}
	
	/**
	 * Touches the metric created timestamp
	 */
	protected void touch() {
		UnsafeAdapter.putLong(address[0] + TIMESTAMP, System.currentTimeMillis());
	}
	
	/**
	 * Returns the byte size of this metric
	 * @return the byte size of this metric
	 */
	protected int getByteSize() {
		return UnsafeAdapter.getInt(address[0] + SIZE);
	}
	
	/**
	 * Updates the byte size of this metric
	 * @return the new byte size of this metric
	 */
	protected int updateByteSize() {				
		int size = BASE_SIZE + getNameSize() + getOpaqueSize();
		UnsafeAdapter.putInt(address[0] + SIZE, size);
		return size;
	}

	
	protected void setName(String name) {
		int currentSize = getNameSize();
		byte[] nameBytes = getBytes(name);
		if(currentSize!=nameBytes.length) {
			if(name==null) {
				// name has been cleared
			} else {
				// name has changed
			}
		} else {
			if(name!=null) {
				// name chaned from non-zero to another non-zero
			} else {
				// no change. Was zero, and still is zero.
			}
		}		
	}
	

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.util.unsafe.DeAllocateMe#getAddresses()
	 */
	@Override
	public long[][] getAddresses() {
		return new long[][] {address};
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IMetricDefinition#getId()
	 */
	@Override
	public long getId() {
		return UnsafeAdapter.getLong(address[0] + ID);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IMetricDefinition#getCreatedTimestamp()
	 */
	@Override
	public long getCreatedTimestamp() {		
		return UnsafeAdapter.getLong(address[0] + TIMESTAMP);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IMetricDefinition#getName()
	 */
	@Override
	public String getName() {
		int size = getNameSize();
		if(size==0) return null;
		byte[] bytes = new byte[size];
		UnsafeAdapter.copyMemory(null, address[0] + NAME_BYTES, bytes, UnsafeAdapter.BYTE_ARRAY_OFFSET, size);
		return new String(bytes, CHARSET);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.IMetricDefinition#getOpaqueKey()
	 */
	@Override
	public byte[] getOpaqueKey() {
		int size = getOpaqueSize();
		if(size==0) return null;
		byte[] bytes = new byte[size];
		UnsafeAdapter.copyMemory(null, address[0] + NAME_BYTES + getNameSize(), bytes, UnsafeAdapter.BYTE_ARRAY_OFFSET, size);
		return bytes;
	}
	
	/**
	 * Returns the bytes of the passed string
	 * @param s The string to extract from
	 * @return a byte array
	 */
	public static byte[] getBytes(String s) {
		if(s==null || s.trim().isEmpty()) return EMPTY_BYTE_ARR;
		return s.getBytes();
	}
	
	/**
	 * Returns the bytes of the passed byte array
	 * @param arr The array to extract from
	 * @return a byte array
	 */
	public static byte[] getBytes(byte[] arr) {
		if(arr==null || arr.length==0) return EMPTY_BYTE_ARR;
		return arr;
	}



	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("Metric [Id:");
		builder.append(getId());
		builder.append(", ts:");
		builder.append(getCreatedTimestamp());
		builder.append(", ");
		String name = getName();
		if (name != null) {
			builder.append("name:");
			builder.append(name);
			builder.append(", ");
		}
		byte[] ok = getOpaqueKey();
		if (ok != null) {
			builder.append("opaqueKey:");
			builder.append(ok.length);
		}
		if(ok==null && name!=null) {
			builder.deleteCharAt(builder.length()-1);
			builder.deleteCharAt(builder.length()-1);
		}
		builder.append("]");
		return builder.toString();
	}
	

}
