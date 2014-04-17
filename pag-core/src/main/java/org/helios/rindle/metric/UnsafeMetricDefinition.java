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
package org.helios.rindle.metric;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.rindle.util.unsafe.DeAllocateMe;
import org.helios.rindle.util.unsafe.UnsafeAdapter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * <p>Title: UnsafeMetricDefinition</p>
 * <p>Description: An {@link IMetricDefinition} with all values stored off heap</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.UnsafeMetricDefinition</code></p>
 */
@JsonSerialize(using=MetricSerialization.UnsafeMetricDefinitionSerializer.class)
@JsonDeserialize(using=MetricSerialization.UnsafeMetricDefinitionDeserializer.class)
public class UnsafeMetricDefinition implements IMetricDefinition, DeAllocateMe {
	/** The native address of the metric definition */
	protected final long[] address = new long[]{-1L};
	
	/** Static class logger */
	protected static final Logger LOG = LogManager.getLogger(UnsafeMetricDefinition.class);
	
	
	/**
	 * Creates a new UnsafeMetricDefinition
	 * @param globalId The global id of the metric
	 * @param timestamp The creation timestamp of this metric, or -1L if the metric is new, in which case the currnt time will be substituted
	 * @param name The metric name
	 * @param opaqueKey The opaque key
	 */
	public UnsafeMetricDefinition(long globalId, long timestamp, String name, byte[] opaqueKey) {		
		byte[] nameBytes = getBytes(name);
		byte[] opaqueBytes = getBytes(opaqueKey);
		int size = BASE_SIZE + nameBytes.length + opaqueBytes.length;
		address[0] = UnsafeAdapter.allocateAlignedMemory(size);
		UnsafeAdapter.registerForDeAlloc(this);		
		UnsafeAdapter.putByte(address[0], DELETE_FLAG);
		UnsafeAdapter.putInt(address[0] + SIZE, size);
		UnsafeAdapter.putLong(address[0] + ID, globalId);
		UnsafeAdapter.putLong(address[0] + TIMESTAMP, timestamp!=-1L ? timestamp : System.currentTimeMillis());
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
	 * Creates a new UnsafeMetricDefinition with a current timestamp
	 * @param globalId The global id of the metric
	 * @param name The metric name
	 * @param opaqueKey The opaque key
	 */
	public UnsafeMetricDefinition(long globalId, String name, byte[] opaqueKey) {		
		this(globalId, -1L, name, opaqueKey);
	}
	
	/**
	 * Returns the size of the name
	 * @return the size of the name
	 */
	protected int getNameSize() {
		if(address[0]==-1L) return -1;
		return UnsafeAdapter.getInt(address[0] + NAME_SIZE);
	}
	
	/**
	 * Returns the size of the opaque key
	 * @return the size of the opaque key
	 */
	protected int getOpaqueSize() {
		if(address[0]==-1L) return -1;
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
		if(address[0]==-1L) return -1;
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

	
//	protected void setName(String name) {
//		int currentSize = getNameSize();
//		byte[] nameBytes = getBytes(name);
//		int diff = currentSize - nameBytes.length;
//		if(diff != 0) {
//			// something changed
//			if(name==null) {
//				// name has been cleared, size is reduced by nameBytes.length
//				int newSize = getByteSize() - nameBytes.length;
//				long newAddress = UnsafeAdapter.allocateAlignedMemory(newSize);
//				UnsafeAdapter.copyMemory(address[0], newAddress, BASE_SIZE);
//				
//				if(getOpaqueSize()>0) {
//					UnsafeAdapter.copyMemory(address[0] + currentSize, newAddress, BASE_SIZE);
//				}
//			} else {
//				// name has changed
//				if(diff < 0) {
//					// name was null, now it's not
//				} else {
//					// name changed from non-null to a new non-null
//				}
//			}
//		} else {
//			if(name!=null) {				
//				if(name.equals(getName())) {
//					// no change
//				} else {
//					// name changed from non-zero to another non-zero (of the same size)
//				}
//				
//			} else {
//				// no change. Was zero, and still is zero.
//			}
//		}		
//	}
	

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.util.unsafe.DeAllocateMe#getAddresses()
	 */
	@Override
	public long[][] getAddresses() {
		return new long[][] {address};
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.metric.IMetricDefinition#getId()
	 */
	@Override
	public long getId() {
		if(address[0]==-1L) return IMetricDefinition.NO_ENTRY_VALUE;
		return UnsafeAdapter.getLong(address[0] + ID);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.metric.IMetricDefinition#getCreatedTimestamp()
	 */
	@Override
	public long getCreatedTimestamp() {		
		if(address[0]==-1L) return IMetricDefinition.NO_ENTRY_VALUE;
		return UnsafeAdapter.getLong(address[0] + TIMESTAMP);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.metric.IMetricDefinition#getName()
	 */
	@Override
	public String getName() {
		if(address[0]==-1L) return null;
		byte[] bytes = getNameBytes();
		if(bytes.length==0) return null;
		return new String(bytes, CHARSET);
	}
	
	/**
	 * Returns the bytes for the name
	 * @return the bytes for the name
	 */
	public byte[] getNameBytes() {
		if(address[0]==-1L) return null;
		int size = getNameSize();
		if(size==0) return EMPTY_BYTE_ARR;
		byte[] bytes = new byte[size];
		UnsafeAdapter.copyMemory(null, address[0] + NAME_BYTES, bytes, UnsafeAdapter.BYTE_ARRAY_OFFSET, size);
		return bytes;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.metric.IMetricDefinition#getOpaqueKey()
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
			builder.append("opaqueKey:[");
			builder.append(MetricSerialization.base64EncodeToString(ok));
			builder.append("]");
		}
		if(ok==null && name!=null) {
			builder.deleteCharAt(builder.length()-1);
			builder.deleteCharAt(builder.length()-1);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (getId() ^ (getId() >>> 32));
		result = prime * result
				+ ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + java.util.Arrays.hashCode(getOpaqueKey());
		return result;
	}	
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IMetricDefinition))
			return false;
		IMetricDefinition other = (IMetricDefinition) obj;
		if(getId() != NO_ENTRY_VALUE && other.getId() != NO_ENTRY_VALUE) {
			if (getId() != other.getId())
				return false;
		}
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		
		if (getOpaqueKey() == null) {
			if (other.getOpaqueKey() != null)
				return false;
		} else if (!java.util.Arrays.equals(getOpaqueKey(), other.getOpaqueKey()))
			return false;
		
		return true;
	}

}
