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
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.helios.pag.util.StringHelper;
import org.helios.pag.util.unsafe.DeAllocateMe;
import org.helios.pag.util.unsafe.UnsafeAdapter;
import org.helios.pag.util.unsafe.UnsafeAdapter.SpinLock;

import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.impl.IndexedChronicle;

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

	/**
	 * Creates a new ChronicleCache
	 * @param cacheName The logical name of this chronicle store
	 */
	private ChronicleCache() {
		log = LogManager.getLogger(getClass());
		String fileName = null;
		try {
			fileName = config.dataDir.getAbsolutePath() + File.separator + "MetricCache";
			indexedChronicle = new IndexedChronicle(fileName, 1, ByteOrder.nativeOrder(), true, false);
			indexedChronicle.useUnsafe(config.unsafe);
			indexedChronicle.multiThreaded(true);
			indexedChronicle.setEnumeratedMarshaller(new ChronicleCacheEntryMarshaller(writeSpinLock));
			nameCache = new StringKeyChronicleCache(config.nameCacheInitialCapacity, config.nameCacheLoadFactor);
			opaqueCache = new ByteArrayKeyChronicleCache(config.opaqueCacheInitialCapacity, config.opaqueCacheLoadFactor);
			writer = new ChronicleCacheWriter(indexedChronicle, writeSpinLock, nameCache, opaqueCache);
			log.info(StringHelper.banner("Created ChronicleCache"));
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
		long loadCount = 0;
		while(exc.hasNextIndex()) {
			if(!exc.nextIndex()) break;
			
		}
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
