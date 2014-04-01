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
import java.util.concurrent.atomic.AtomicLong;

import org.helios.pag.util.SystemClock;
import org.helios.pag.util.unsafe.UnsafeAdapter;
import org.helios.pag.util.unsafe.UnsafeAdapter.SpinLock;

import com.higherfrequencytrading.chronicle.Chronicle;
import com.higherfrequencytrading.chronicle.Excerpt;

/**
 * <p>Title: ChronicleCacheWriter</p>
 * <p>Description: The chronicle writing excerpt wrapper to guard from multi-threaded concurrent access</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.ChronicleCacheWriter</code></p>
 */

public class ChronicleCacheWriter implements IChronicleCacheEntry {
	/** The chronicle this writer is for */
	protected final Chronicle chronicle;
	/** The guarded writing excerpt */
	protected final Excerpt writer;
	/** The write guarding spin lock */
	protected final SpinLock writeSpinLock;
	
	/** The string key cache to keep metric names synchronized with */
	protected final IStringKeyCache nameCache;
	/** The opaque key cache to keep metric names synchronized with */
	protected final IByteArrayKeyCache opaqueCache;
	/** The cummulative count of deleted entries since the last defragmentation */
	protected final AtomicLong deletedEntries = new AtomicLong(0L);
	/** The cummulative size in bytes of deleted entries since the last defragmentation */
	protected final AtomicLong deletedSize = new AtomicLong(0L);
	/** The number of inserts */
	protected final AtomicLong insertCount = new AtomicLong(0L);
	
	
//	public static int ID_OFFSET = UnsafeAdapter.LONG_SIZE + 1;
	
	/**
	 * Creates a new ChronicleCacheWriter
	 * @param chronicle The chronicle this writer is for
	 * @param spinLock The write guarding spin lock
	 * @param nameCache The metric name cache the writer will keep synchronized
	 * @param opaqueKeyCache The metric opaque key cache the writer will keep synchronized
	 */
	ChronicleCacheWriter(Chronicle chronicle, SpinLock spinLock, IStringKeyCache nameCache, IByteArrayKeyCache opaqueKeyCache) {
		writeSpinLock = spinLock;
		this.chronicle = chronicle;
		this.nameCache = nameCache;
		this.opaqueCache = opaqueKeyCache;
		this.writer = chronicle.createExcerpt();
	}
	
	/**
	 * Returns the cummulative count of deleted entries since the last defragmentation
	 * @return the cummulative count of deleted entries since the last defragmentation
	 */
	public long getDeletedEntryCount() {
		return deletedEntries.get();
	}
	
	/**
	 * Returns the cummulative size in bytes of deleted entries since the last defragmentation
	 * @return the cummulative size in bytes of deleted entries since the last defragmentation
	 */
	public long getDeletedSize() {
		return deletedSize.get();
	}
	
	/**
	 * Returns the insert count
	 * @return the insert count
	 */
	public long getInsertCount() {
		return insertCount.get();
	}
	
	
	public ChronicleCacheEntry writeEntry(ChronicleCacheEntry entry) {
		writeSpinLock.xlock();
		try {
			writer.writeEnum(entry);
			return writer.readEnum(ChronicleCacheEntry.class);
		} finally {
			writeSpinLock.xunlock();
		}
	}
	
	
	/**
	 * Writes a new metric entry and returns the global id
	 * @param name The optional name of the metric. Ignored if null.
	 * @param opaqueKey The optional opaque key of the metric. Ignored if null.
	 * @return the new global id
	 */
	public long newMetricEntry(String name, byte[] opaqueKey) {
		writeSpinLock.xlock();
		long gid = -1L;
		try {
			int capacityEstimate = NAME_OFFSET + (name==null ? 0 : name.length()*2) + (opaqueKey==null ? 0 : opaqueKey.length); 
			writer.startExcerpt(capacityEstimate);
			writer.writeByte(1);
			writer.writeLong(SystemClock.time());
			if(name!=null) {
				byte[] bytes = name.getBytes();
				writer.writeInt(bytes.length);
				writer.skipBytes(UnsafeAdapter.INT_SIZE);
				writer.write(bytes);
			} else {
				writer.writeInt(0);
			}
			writer.position(BYTES_LENGTH_OFFSET);
			if(opaqueKey!=null) {
				writer.writeInt(opaqueKey.length);
				writer.write(opaqueKey);				
			} else {
				writer.writeInt(0);
			}
//			writer.toEnd();
			writer.finish();			
			insertCount.incrementAndGet();
			gid = writer.index();
			return gid;
		} finally {
			writeSpinLock.xunlock();
			if(name!=null && gid != -1L) {
				nameCache.put(name, gid);
			}
			if(opaqueKey!=null) {
				opaqueCache.put(opaqueKey, gid);
			}					
		}	
	}
	
	/**
	 * Writes a new metric entry and returns the global id
	 * @param name The name of the metric. Ignored if null.
	 * @return the new global id
	 */
	public long newMetricEntry(String name) {
		if(name==null) throw new IllegalArgumentException("The passed name was null");
		return newMetricEntry(name, null);		
	}
	
	/**
	 * Writes a new metric entry and returns the global id
	 * @param opaqueKey The opaque key of the metric. Ignored if null.
	 * @return the new global id
	 */
	public long newMetricEntry(byte[] opaqueKey) {
		if(opaqueKey==null) throw new IllegalArgumentException("The passed opaque key was null");
		return newMetricEntry(null, opaqueKey);		
	}
	
	/**
	 * Creates a new entry with only a timestamp, for use when we want to create an ID but have no metric name or byte[] to associate to it.
	 * @return the ID (global ID, or GID) of the new entry
	 */
	public long newMetricEntry() {
		return newMetricEntry(null, null);
	}
	
	/**
	 * Marks an entry as deleted
	 * @param gid The id of the entry to delete
	 */
	public void markEntryDeleted(long gid) {
		writeSpinLock.xlock();
		String metricName = null;
		byte[] opaqueKey = null;
		try {
			writer.index(gid);
			writer.position(DELETE_OFFSET).writeByte(0);
			metricName = _readMetricName();
			opaqueKey = _readOpaqueKey();
			writer.toEnd();
			writer.finish();	
			deletedEntries.incrementAndGet();
			deletedSize.addAndGet(writer.size());
		} finally {
			writeSpinLock.xunlock();
		}		
		if(metricName!=null) {
			nameCache.remove(metricName);
		}
		if(opaqueKey!=null) {
			opaqueCache.remove(opaqueKey);
		}		
	}
	
	
	/**
	 * Reads the metric name from the current excerpt index
	 * @return the metric name
	 */
	protected String _readMetricName() {
		final int length = writer.readInt(NAME_LENGTH_OFFSET);
		if(length==0) return null;
		byte[] bytes = new byte[length];
		writer.read(bytes);
		return new String(bytes, CHARSET);
	}
	
	/**
	 * Reads the opaque key from the current excerpt index
	 * @return the opaque key
	 */
	protected byte[] _readOpaqueKey() {
		final int blength = writer.readInt(BYTES_LENGTH_OFFSET);		
		if(blength==0) return null;
		final int slength = writer.readInt(NAME_LENGTH_OFFSET);
		writer.position(BYTES_LENGTH_OFFSET + slength);
		byte[] bytes = new byte[blength];
		writer.read(bytes);
		return bytes;
	}
	

}
