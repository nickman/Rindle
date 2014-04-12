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

import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cern.colt.Arrays;

import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.ExcerptMarshallable;
import com.higherfrequencytrading.chronicle.impl.IndexedChronicle;
import com.higherfrequencytrading.chronicle.tools.ChronicleTools;

/**
 * <p>Title: ChronicleCacheEntry</p>
 * <p>Description: A marshallable cache entry pojo representing a metric</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.ChronicleCacheEntry</code></p>
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
	
	/** Static class logger */
	protected static final Logger LOG = LogManager.getLogger(ChronicleCacheEntry.class);
	
	/** The chronicle cache instance */
	protected static final ChronicleCache cache = ChronicleCache.getInstance();
	
	/** The default charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	
	/** The cache no entry value, meaning a non-existent value not in the cache */
	public static final long NO_ENTRY_VALUE = -1L;
	/** A const null entry */
	private static final ChronicleCacheEntry NULL_ENTRY = new ChronicleCacheEntry(NO_ENTRY_VALUE);

	
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
			return ChronicleCacheEntry.load(_gid, null);
		}
		return cache.getWriter().writeEntry(entry);		
	}
	
	/**
	 * Loads a ChronicleCacheEntry
	 * @param globalId the global id
	 * @param exc The optional chronicle excerpt. If null, a new excerpt will be created and closed on load completion.
	 * @return the loaded ChronicleCacheEntry
	 */
	public static ChronicleCacheEntry load(long globalId, Excerpt exc) {
		if(globalId==NO_ENTRY_VALUE) return NULL_ENTRY;
		final boolean newExcerpt = exc==null;
		if(newExcerpt) exc = cache.newExcerpt();
		try {
			ChronicleCacheEntry entry = new ChronicleCacheEntry(globalId);
			exc.index(globalId);
			entry.readMarshallable(exc);
			return entry;
		} finally {
			if(newExcerpt) try { exc.close(); } catch (Exception ex) {/* No Op */}
		}
	}
	
	/**
	 * Loads a ChronicleCacheEntry
	 * @param metricName The fully qualified metric name
	 * @param exc The optional chronicle excerpt. If null, a new excerpt will be created and closed on load completion.
	 * @return the loaded ChronicleCacheEntry
	 */
	public static ChronicleCacheEntry load(String metricName, Excerpt exc) {
		if(metricName==null) throw new IllegalArgumentException("The passed metric name was null");
		long _gid = cache.getNameCache().get(metricName);
		return load(_gid, exc);
	}
	
	/**
	 * Loads a ChronicleCacheEntry
	 * @param opaqueKey The metric's opaque key
	 * @param exc The optional chronicle excerpt. If null, a new excerpt will be created and closed on load completion.
	 * @return the loaded ChronicleCacheEntry
	 */
	public static ChronicleCacheEntry load(byte[] opaqueKey, Excerpt exc) {
		if(opaqueKey==null) throw new IllegalArgumentException("The passed metric opaque key was null");
		long _gid = cache.getOpaqueCache().get(opaqueKey);
		return load(_gid, exc);
	}
	
	/**
	 * Creates a new ChronicleCacheEntry and saves it to the chronicle cache, populating the new global id in the process
	 * @param metricName The optional metric name
	 * @return the saved ChronicleCacheEntry global id
	 */
	public static long newEntry(String metricName) {
		return cache.getWriter().newMetricEntry(metricName);		
	}
	
	/**
	 * Creates a new ID only ChronicleCacheEntry and saves it to the chronicle cache, populating the new global id in the process
	 * @param opaqueKey The optional opaqueKey
	 * @return the saved ChronicleCacheEntry global id
	 */
	public static long newEntry(byte[] opaqueKey) {
		return cache.getWriter().newMetricEntry(opaqueKey);
	}
	
	
	
	/**
	 * Creates a new ChronicleCacheEntry and saves it to the chronicle cache, populating the new global id in the process
	 * @return the saved ChronicleCacheEntry global id
	 */
	public static long newEntry() {
		return cache.getWriter().newMetricEntry();
	}
	
	/**
	 * Creates an unsaved stub with a no-entry value global id
	 * @param metricName The optional metric name
	 * @param opaqueKey The optional metric opaque key
	 * @return the ChronicleCacheEntry stub
	 */
	public static ChronicleCacheEntry stub(String metricName, byte[] opaqueKey) {
		return new ChronicleCacheEntry(IKeyCache.NO_ENTRY_VALUE, metricName, opaqueKey);
	}
	/**
	 * Creates an unsaved stub
	 * @param globalId The global id
	 * @param metricName The optional metric name
	 * @param opaqueKey The optional metric opaque key
	 * @return the ChronicleCacheEntry stub
	 */
	public static ChronicleCacheEntry stub(long globalId, String metricName, byte[] opaqueKey) {
		return new ChronicleCacheEntry(globalId, metricName, opaqueKey);
	}
	
	
	/**
	 * Creates a new ChronicleCacheEntry
	 * @param gid The assigned global id
	 * @param metricName The optional metric name
	 * @param opaqueKey The optional metric opaque key
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
		in.position(0);
		try {
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
		} catch (Exception ex) {
			try { LOG.error("Failed to read from excerpt. id: {}, size: {}, cap: {}", in.index(), in.size(), in.capacity()); } catch (Exception x) {/* No Op */}
			throw new RuntimeException("Failed to read from excerpt", ex);
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
		if(globalId != IKeyCache.NO_ENTRY_VALUE && other.globalId != IKeyCache.NO_ENTRY_VALUE) {
			if (globalId != other.globalId)
				return false;
		}
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
