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

import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.impl.IndexedChronicle;

/**
 * <p>Title: ChronicleCache</p>
 * <p>Description: Chronicle based persistent store for tri-cache</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.ChronicleCache</code></p>
 */

public class ChronicleCache implements DeAllocateMe {
	/** The singleton instance */
	private static final Map<String, ChronicleCache> chronicles = new NonBlockingHashMap<String, ChronicleCache>(32);

	/** The general config for chronicles */
	protected final ChronicleConfiguration config = new ChronicleConfiguration();
	
	/** The store name */
	protected final String name;
	/** The store name index chronicle */
	protected final IndexedChronicle indexedChronicle;
	/** The store chronicle writer excerpt */
	protected final Excerpt chronicleWriter;
	/** The address of the global lock for this instance's chronicle writer */
	protected final long[] globalLockAddress = new long[1];
	
	/** Instance logger */
	protected final Logger log;
	
	/**
	 * Acquires the named ChronicleCache instance
	 * @param cacheName The name of the cache to retrieve
	 * @return the singleton ChronicleCache instance
	 */
	public static ChronicleCache getInstance(String cacheName) {
		ChronicleCache cache = chronicles.get(cacheName);		
		if(cache==null) {
			synchronized(chronicles) {
				if(cache==null) {
					cache = new ChronicleCache(cacheName);
					chronicles.put(cacheName, cache);
				}
			}
		}
		return cache;
	}

	/**
	 * Creates a new ChronicleCache
	 * @param cacheName The logical name of this chronicle store
	 */
	private ChronicleCache(String cacheName) {
		this.name = cacheName;
		log = LogManager.getLogger(getClass().getName() + "." + cacheName);
		String fileName = null;
		try {
			fileName = config.dataDir.getAbsolutePath() + File.separator + name;
			indexedChronicle = new IndexedChronicle(fileName, 1, ByteOrder.nativeOrder(), true, false);
			indexedChronicle.useUnsafe(config.unsafe);
			indexedChronicle.multiThreaded(true);
			chronicleWriter = indexedChronicle.createExcerpt();
			globalLockAddress[0] = UnsafeAdapter.allocateAlignedMemory(UnsafeAdapter.LONG_SIZE);
			UnsafeAdapter.registerForDeAlloc(this);
			log.info(StringHelper.banner("Created ChronicleCache [%s]", name));
		} catch (IOException e) {
			String msg = "Failed to create IndexedChronicle [" + cacheName + "] in [" + fileName + "]";
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.util.unsafe.DeAllocateMe#getAddresses()
	 */
	@Override
	public long[][] getAddresses() {
		return new long[][]{globalLockAddress};
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
