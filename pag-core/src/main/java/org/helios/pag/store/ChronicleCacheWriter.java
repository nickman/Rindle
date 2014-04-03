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
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.helios.pag.util.SystemClock;
import org.helios.pag.util.unsafe.UnsafeAdapter;
import org.helios.pag.util.unsafe.UnsafeAdapter.SpinLock;

import com.higherfrequencytrading.chronicle.Chronicle;
import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.impl.IndexedChronicle;

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
	protected final SpinLock spinLock;
	
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
	
	/** Empty byte array constant */
	public static final byte[] EMPTY_BYTE_ARR = {};
	
	/** The default charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	
	
	
	/**
	 * Creates a new ChronicleCacheWriter
	 * @param chronicle The chronicle this writer is for
	 * @param spinLock The write guarding spin lock
	 * @param nameCache The metric name cache the writer will keep synchronized
	 * @param opaqueKeyCache The metric opaque key cache the writer will keep synchronized
	 */
	ChronicleCacheWriter(IndexedChronicle chronicle, SpinLock spinLock, IStringKeyCache nameCache, IByteArrayKeyCache opaqueKeyCache) {
		this.spinLock = spinLock;
		this.chronicle = chronicle;
		this.nameCache = nameCache;
		this.opaqueCache = opaqueKeyCache;
		this.writer = chronicle.createExcerpt();
	}
	
	/**
	 * Executes a write task within a spin lock acquire/release
	 * @param task The task to execute
	 * @return the return value of the task
	 */
	public <T> T executeWriteTask(WriteTask<T> task) {
		spinLock.xlock();
		try {
			try {
				return task.call(writer);
			} catch (Exception ex) {
				throw new RuntimeException("Write task failed", ex);
			}
		} finally {
			spinLock.xunlock();
		}
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
		spinLock.xlock();
		try {
			writer.writeEnum(entry);
			return writer.readEnum(ChronicleCacheEntry.class);
		} finally {
			spinLock.xunlock();
		}
	}
	
	
	/**
	 * Writes a new metric entry and returns the global id
	 * @param name The optional name of the metric. Ignored if null.
	 * @param opaqueKey The optional opaque key of the metric. Ignored if null.
	 * @return the new global id
	 */
	public long newMetricEntry(String name, byte[] opaqueKey) {
		
		long ngid = name!=null ? nameCache.get(name) : IKeyCache.NO_ENTRY_VALUE;
		long bgid = opaqueKey!=null ? opaqueCache.get(opaqueKey) : IKeyCache.NO_ENTRY_VALUE;
		if(ngid!=IKeyCache.NO_ENTRY_VALUE || bgid!=IKeyCache.NO_ENTRY_VALUE) {
			if(ngid!=IKeyCache.NO_ENTRY_VALUE && bgid!=IKeyCache.NO_ENTRY_VALUE) {
				if(ngid != bgid) {
					// merge the two entries into one new one
					return mergeEntries(name, ngid, opaqueKey, bgid);
				}
				// retrieve stored and compare. if same, end, otherwise, delete and re-insert
				return loadAndCompare(ngid, name, opaqueKey);
			}
			if(ngid!=IKeyCache.NO_ENTRY_VALUE) {
				// retrieve stored and compare. if same, end, otherwise, delete and re-insert
				return loadAndCompare(ngid, name, opaqueKey);
			} 
			// retrieve stored and compare. if same, end, otherwise, delete and re-insert
			return loadAndCompare(bgid, name, opaqueKey);
		} // no cache match. continue
		
		long gid = IKeyCache.NO_ENTRY_VALUE;
		try {
			spinLock.xlock();
			int capacityEstimate = NAME_OFFSET + (name==null ? 0 : name.length()*2) + (opaqueKey==null ? 0 : opaqueKey.length); 
			writer.startExcerpt(capacityEstimate);
			writer.writeByte(1);
			writer.writeLong(SystemClock.time());
			byte[] stringBytes = getBytes(name);
			byte[] opaqueBytes = getBytes(opaqueKey);
			writer.writeInt(stringBytes.length);
			writer.writeInt(opaqueBytes.length);
			writer.write(stringBytes);
			writer.write(opaqueBytes);
			writer.finish();
			insertCount.incrementAndGet();
			gid = writer.index();
			return gid;
		} finally {
			spinLock.xunlock();
			if(name!=null && gid != -1L) {
				nameCache.put(name, gid);
			}
			if(opaqueKey!=null) {
				opaqueCache.put(opaqueKey, gid);
			}					
		}	
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
	 * Writes a new metric entry and returns the global id without acquiring a lock.
	 * @param name The optional name of the metric. Ignored if null.
	 * @param opaqueKey The optional opaque key of the metric. Ignored if null.
	 * @return the new global id
	 */
	protected long _newMetricEntry(String name, byte[] opaqueKey) {
		int capacityEstimate = NAME_OFFSET + (name==null ? 0 : name.length()*2) + (opaqueKey==null ? 0 : opaqueKey.length); 
		writer.startExcerpt(capacityEstimate);
		writer.writeByte(1);
		writer.writeLong(SystemClock.time());
		if(name!=null) {
			byte[] bytes = name.getBytes(CHARSET);
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
		writer.finish();			
		insertCount.incrementAndGet();
		return writer.index();
	}
	
	/**
	 * Merges two different chronicle entries, one with a name and one with an opaqe key into one common entry.
	 * Updates the name and opaque cache.
	 * @param name The metric name
	 * @param ngid The metric name cache id
	 * @param opaqueKey The opaque key
	 * @param bgid The opaque key id
	 * @return the new global id
	 */
	protected long mergeEntries(String name, long ngid, byte[] opaqueKey, long bgid) {
		long gid = IKeyCache.NO_ENTRY_VALUE;
		try {
			spinLock.xlock();
			_markEntryDeleted(ngid); 
			_markEntryDeleted(bgid);
			gid = _newMetricEntry(name, opaqueKey);			
		} finally {
			spinLock.xunlock();
		}
		nameCache.put(name, gid);
		opaqueCache.put(opaqueKey, gid);
		return gid;
	}
	
	/**
	 * Creates a new stub entry with the passed name and opaque key, then loads the chronicle entry with the passed gid.
	 * The two are compared. If different, the loaded entry is deleted and the stub entry is saved. Updates the name and opaque cache.
	 * Otherwise nothing. 
	 * @param gid The global id of the chronicle entry to load
	 * @param name The metric name
	 * @param opaqueKey The metric opaque key
	 * @return the winning global id
	 */
	protected long loadAndCompare(long gid, String name, byte[] opaqueKey) {
		ChronicleCacheEntry newEntry = ChronicleCacheEntry.stub(name, opaqueKey);
		ChronicleCacheEntry storedEntry = ChronicleCacheEntry.load(gid, null);
		if(newEntry.equals(storedEntry)) return gid; // Nothing to do. Everything is unchanged.
		// Delete the old one and write a new one
		long newGlobalId = IKeyCache.NO_ENTRY_VALUE;
		try {
			spinLock.xlock();
			_markEntryDeleted(gid);
			newGlobalId = _newMetricEntry(name, opaqueKey);
		} finally {
			spinLock.xunlock();
		}
		if(newGlobalId != IKeyCache.NO_ENTRY_VALUE) {
			if(name!=null) {
				nameCache.put(name, newGlobalId);
			}
			if(opaqueKey!=null) {
				opaqueCache.put(opaqueKey, newGlobalId);
			}			
		}
		return newGlobalId;
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
		spinLock.xlock();
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
			spinLock.xunlock();
		}		
		if(metricName!=null) {
			nameCache.remove(metricName);
		}
		if(opaqueKey!=null) {
			opaqueCache.remove(opaqueKey);
		}		
	}
	
	/**
	 * Marks an entry as deleted without messing with the lock and without cache updates.
	 * Assumes the lock has been acquired before now.
	 * @param gid The id of the entry to delete
	 */
	protected void _markEntryDeleted(long gid) {
		writer.index(gid);
		writer.position(DELETE_OFFSET).writeByte(0);
		writer.writeLong(System.currentTimeMillis());		
		writer.finish();	
		deletedEntries.incrementAndGet();
		deletedSize.addAndGet(writer.size());
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
