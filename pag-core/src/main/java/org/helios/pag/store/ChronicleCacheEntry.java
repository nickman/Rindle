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

import java.nio.charset.Charset;

import cern.colt.Arrays;

import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.ExcerptMarshallable;

/**
 * <p>Title: ChronicleCacheEntry</p>
 * <p>Description: A marshallable cache entry pojo representing a metric</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.ChronicleCacheEntry</code></p>
 */

public class ChronicleCacheEntry implements ExcerptMarshallable {
	/** The metric global ID */
	protected long globalId = -1L;
	/** The metric fully qualified name */
	protected String metricName = null;
	/** The metric opaque key */
	protected byte[] opaqueKey = null;
	/** The timestamp the metric was created */
	protected long timestamp = -1L;
	
	/** The chronicle cache instance */
	protected static final ChronicleCache cache = ChronicleCache.getInstance();
	
	/** The default charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	
	/** The cache no entry value, meaning a non-existent value not in the cache */
	public static final long NO_ENTRY_VALUE = -1L;


	
	/**
	 * Creates a new ChronicleCacheEntry and saves it to the chronicle cache, populating the new global id in the process
	 * @param metricName The optional metric name
	 * @param opaqueKey The optional opaque key
	 * @return the saved ChronicleCacheEntry
	 */
	public static ChronicleCacheEntry newEntry(String metricName, byte[] opaqueKey) {
		ChronicleCacheEntry entry = new ChronicleCacheEntry(-1L, metricName, opaqueKey);
		long _gid = NO_ENTRY_VALUE;
		if(metricName!=null) {
			_gid = cache.getNameCache().get(metricName);
		}
		if(opaqueKey!=null && _gid == NO_ENTRY_VALUE) {
			_gid = cache.getOpaqueCache().get(opaqueKey);
		}
		if(_gid != NO_ENTRY_VALUE) {
			
		}
		return cache.getWriter().writeEntry(entry);		
	}
	
	/**
	 * @param globalId
	 * @param exc
	 * @return
	 */
	public static ChronicleCacheEntry load(long globalId, Excerpt exc) {
		final boolean newExcerpt = exc==null;
		if(newExcerpt) exc = cache.newExcerpt();
		try {
			ChronicleCacheEntry entry = new ChronicleCacheEntry(globalId);
			entry.readMarshallable(exc);
			return entry;
		} finally {
			if(newExcerpt) try { exc.close(); } catch (Exception ex) {/* No Op */}
		}
	}
	
	/**
	 * Creates a new ChronicleCacheEntry and saves it to the chronicle cache, populating the new global id in the process
	 * @param metricName The optional metric name
	 * @return the saved ChronicleCacheEntry
	 */
	public static ChronicleCacheEntry newEntry(String metricName) {
		return newEntry(metricName, null);
	}
	
	/**
	 * Creates a new ID only ChronicleCacheEntry and saves it to the chronicle cache, populating the new global id in the process
	 * @param opaqueKey The optional opaqueKey
	 * @return the saved ChronicleCacheEntry
	 */
	public static ChronicleCacheEntry newEntry(byte[] opaqueKey) {
		return newEntry(null, opaqueKey);
	}
	
	
	
	/**
	 * Creates a new ChronicleCacheEntry and saves it to the chronicle cache, populating the new global id in the process
	 * @return the saved ChronicleCacheEntry
	 */
	public static ChronicleCacheEntry newEntry() {
		return newEntry(null, null);
	}
	
	/**
	 * Creates a new ChronicleCacheEntry
	 */
	private ChronicleCacheEntry(long gid, String metricName, byte[] opaqueKey) {
		globalId = gid;
		this.metricName = metricName;
		this.opaqueKey = opaqueKey;
	}
	
	private ChronicleCacheEntry(long globalId) {
		this.globalId = globalId;		
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.ExcerptMarshallable#readMarshallable(com.higherfrequencytrading.chronicle.Excerpt)
	 */
	@Override
	public void readMarshallable(Excerpt in) throws IllegalStateException {
		in.toStart();
		if(in.readByte()==0) {
			globalId = -1L;
			metricName = null;
			opaqueKey = null;
			timestamp = -1L;
		} else {
			globalId = in.index();
			timestamp = in.readLong();
			int stringSize = in.readInt();
			int byteSize = in.readInt();
			byte[] bytes = null;
			if(stringSize>0) {
				bytes = new byte[stringSize];
				in.read(bytes);
				metricName = new String(bytes);
				bytes = null;
 			} else { metricName = null; }
			if(byteSize>0) {
				opaqueKey = new byte[byteSize];
				in.read(opaqueKey);				
			} else { opaqueKey = null; }			
		}
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.ExcerptMarshallable#writeMarshallable(com.higherfrequencytrading.chronicle.Excerpt)
	 */
	@Override
	public void writeMarshallable(Excerpt out) {
		globalId = cache.getWriter().newMetricEntry(metricName, opaqueKey);
	}

	
	/**
	 * Indicates if this entry is null
	 * @return true if this entry is null, false otherwise
	 */
	public boolean isNull() {
		return globalId==-1L;
	}

	/**
	 * Returns the metric global ID
	 * @return the globalId
	 */
	public long getGlobalId() {
		return globalId;
	}


	/**
	 * Returns the metric name
	 * @return the metricName
	 */
	public String getMetricName() {
		return metricName;
	}


	/**
	 * Returns the opaque key
	 * @return the opaqueKey
	 */
	public byte[] getOpaqueKey() {
		return opaqueKey;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder b = new StringBuilder("Metric [gid:").append(globalId).append(",");
		if(metricName!=null) b.append("name:").append(metricName).append(",");
		if(opaqueKey!=null) b.append("okey:").append(Arrays.toString(opaqueKey)).append(",");
		return b.deleteCharAt(b.length()-1).append("]").toString();
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (globalId ^ (globalId >>> 32));
		result = prime * result
				+ ((metricName == null) ? 0 : metricName.hashCode());
		result = prime * result + java.util.Arrays.hashCode(opaqueKey);
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
		if (getClass() != obj.getClass())
			return false;
		ChronicleCacheEntry other = (ChronicleCacheEntry) obj;
		if (globalId != other.globalId)
			return false;
		if (metricName == null) {
			if (other.metricName != null)
				return false;
		} else if (!metricName.equals(other.metricName))
			return false;
		if (!java.util.Arrays.equals(opaqueKey, other.opaqueKey))
			return false;
		return true;
	}

}
