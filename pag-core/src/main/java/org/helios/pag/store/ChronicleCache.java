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

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.pag.util.StringHelper;
import org.helios.pag.util.SystemClock;
import org.helios.pag.util.SystemClock.ElapsedTime;
import org.helios.pag.util.unsafe.UnsafeAdapter;
import org.helios.pag.util.unsafe.UnsafeAdapter.SpinLock;

import test.cache.TestKeyedCaches;

import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.impl.IndexedChronicle;
import com.higherfrequencytrading.chronicle.tools.ChronicleTools;

/**
 * <p>Title: ChronicleCache</p>
 * <p>Description: Chronicle based persistent store for tri-cache</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.ChronicleCache</code></p>
 */

public class ChronicleCache {
	/** The singleton instance */
	private static volatile ChronicleCache instance = null;
	/** The singleton instance ctor lock */
	private static final Object lock = new Object();
	
	
	/** The write guarding spin lock */
	protected final SpinLock writeSpinLock = UnsafeAdapter.allocateSpinLock();

	
	
	/** The general config for chronicles */
	protected final ChronicleConfiguration config = new ChronicleConfiguration();
	
	/** The string key cache to keep metric names synchronized with */
	protected final IStringKeyCache nameCache;
	/** The opaque key cache to keep metric names synchronized with */
	protected final IByteArrayKeyCache opaqueCache;
	
	
	/** The store name index chronicle */
	protected final IndexedChronicle indexedChronicle;
	/** This instance's chronicle writer */
	protected final ChronicleCacheWriter writer; 
	
	/** Instance logger */
	protected final Logger log;
	
	/**
	 * Acquires the ChronicleCache instance
	 * @return the singleton ChronicleCache instance
	 */
	public static ChronicleCache getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new ChronicleCache();
				}
			}
		}
		return instance;
	}
	
	public static void main(String[] args) {
		ChronicleCache cc = null;
		try {
			cc = ChronicleCache.getInstance();
			cc.indexedChronicle.clear();
			cc.clearCache();
			
			cc.log.info("UUID Sample Size: {}", TestKeyedCaches.uuidSamples.size());
			for(String uuid: TestKeyedCaches.uuidSamples.values()) {
				ChronicleCacheEntry.newEntry(uuid);
				ChronicleCacheEntry.newEntry(uuid.getBytes());
			}
			cc.clearCache();
			cc.load();			
			ElapsedTime et = SystemClock.startClock();
			for(int i = 0; i < 10; i++) {
				for(String uuid: TestKeyedCaches.uuidSamples.values()) {
					ChronicleCacheEntry.newEntry(uuid);
					ChronicleCacheEntry.newEntry(uuid.getBytes());
				}
			}
			cc.log.info("Loaded Test Data: {}", et.printAvg("Entries", TestKeyedCaches.uuidSamples.size() * 10));
			cc.log.info("Name Cache Size: {}", cc.nameCache.size());
			cc.log.info("Opaque Cache Size: {}", cc.opaqueCache.size());
		} catch (Throwable t) {
			cc.log.info("Name Cache Size: {}", cc.nameCache.size());
			cc.log.info("Opaque Cache Size: {}", cc.opaqueCache.size());
			cc.log.error("Test Fail", t);
		} finally {
			ChronicleTools.deleteOnExit(new ChronicleConfiguration().dataDir.getAbsolutePath());
		}		
	}
	
	/**
	 * Clears the chronicle, name and opaque caches
	 */
	private void purge() {
		indexedChronicle.clear();
		nameCache.clear();
		opaqueCache.clear();
	}
	
	/**
	 * Clears the name and opaque caches
	 */
	private void clearCache() {
		nameCache.clear();
		opaqueCache.clear();
	}
	

	/**
	 * Creates a new ChronicleCache
	 * @param cacheName The logical name of this chronicle store
	 */
	private ChronicleCache() {
		log = LogManager.getLogger(getClass());
		String fileName = null;
		try {
			fileName = config.dataDir.getAbsolutePath() + File.separator + "MetricCache";
			indexedChronicle = new IndexedChronicle(fileName, 8, ByteOrder.nativeOrder(), true, false);
			indexedChronicle.useUnsafe(config.unsafe);
			indexedChronicle.multiThreaded(true);
			indexedChronicle.setEnumeratedMarshaller(new ChronicleCacheEntryMarshaller(writeSpinLock));
			nameCache = new StringKeyChronicleCache(config.nameCacheInitialCapacity, config.nameCacheLoadFactor);
			opaqueCache = new ByteArrayKeyChronicleCache(config.opaqueCacheInitialCapacity, config.opaqueCacheLoadFactor);
			writer = new ChronicleCacheWriter(indexedChronicle, writeSpinLock, nameCache, opaqueCache);
			log.info(StringHelper.banner("Created ChronicleCache"));
			load();
		} catch (IOException e) {
			String msg = "Failed to create IndexedChronicle in [" + fileName + "]";
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}
	
	Excerpt newExcerpt() {
		return indexedChronicle.createExcerpt();
	}
	
	protected void load() {
		Excerpt exc = indexedChronicle.createExcerpt();
		long loadCount = 0, nameCacheEntries = 0, opaqueCacheEntries = 0;
		
		while(exc.hasNextIndex()) {
			if(!exc.nextIndex()) break;
			
			if(exc.readByte()==0) {
				continue;
			}
			long key = exc.index();
			if(exc.capacity() < IChronicleCacheEntry.BYTES_LENGTH_OFFSET) {
				log.info("Corrupt Entry: {}, Size:{}", key, exc.capacity());
				exc.position(0);
				exc.writeByte(0);
				exc.toEnd();
				exc.finish();
				exc.flush();
				
			}
			exc.position(IChronicleCacheEntry.NAME_LENGTH_OFFSET);
			int s_size = exc.readInt();
			int b_size = exc.readInt();
			if(s_size>0) {
				byte[] bytes = new byte[s_size];
				exc.read(bytes);
				nameCache.put(new String(bytes), key);
				nameCacheEntries++;
			}
			if(b_size>0) {
				byte[] bytes = new byte[b_size];
				exc.read(bytes);
				opaqueCache.put(bytes, key);
				opaqueCacheEntries++;
			}				
			loadCount++;
		}
		log.info(StringHelper.banner("Loaded %s Cache Records\n\tName Cache: %s\n\tOpaque Cache: %s" , loadCount, nameCacheEntries, opaqueCacheEntries));
	}

	/**
	 * Returns the 
	 * @return the writer
	 */
	public ChronicleCacheWriter getWriter() {
		return writer;
	}

	/**
	 * Returns the 
	 * @return the nameCache
	 */
	public IStringKeyCache getNameCache() {
		return nameCache;
	}

	/**
	 * Returns the 
	 * @return the opaqueCache
	 */
	public IByteArrayKeyCache getOpaqueCache() {
		return opaqueCache;
	}

	
	
	/*
	 * TriCache Store:
	 * ===============
	 * In mem cache:  ??? which ones ?   byte[] --> id,  name --> id
	 * Load at start, Skip deleted.
	 * Define TriCacheEntry implements ExcerptMarshallable:  direct mem.
	 * Write entry
	 * 		Empty (for id only)
	 * 		Name only
	 * 		byte[] only
	 * 		Name and byte[]
	 * Read entry
	 * Delete entry
	 * Update entry  (delete and write)
	 * Defragment  (copy, clear, import)
	 * 
	 */
	
	

}
