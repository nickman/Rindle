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
package org.helios.rindle.store.chronicle;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.rindle.metric.IMetricDefinition;
import org.helios.rindle.util.StringHelper;
import org.helios.rindle.util.SystemClock;
import org.helios.rindle.util.SystemClock.ElapsedTime;
import org.helios.rindle.util.unsafe.UnsafeAdapter;
import org.helios.rindle.util.unsafe.UnsafeAdapter.SpinLock;

import test.cache.TestKeyedCaches;

import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.impl.IndexedChronicle;
import com.higherfrequencytrading.chronicle.tools.ChronicleTools;

/**
 * <p>Title: ChronicleCache</p>
 * <p>Description: Chronicle based persistent store for tri-cache</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.ChronicleCache</code></p>
 */

public class ChronicleCache {
	/** The singleton instance */
	private static volatile ChronicleCache instance = null;
	/** The singleton instance ctor lock */
	private static final Object lock = new Object();
	/** The main chronicle name */
	public static final String CACHE_NAME = "MetricCache";
	/** The defrag chronicle name */
	public static final String DEFRAG_NAME = "Defrag";
	/** The read only chronicle name */
	public static final String RO_NAME = "ReadOnly";
	
	/** Factory for global ids */
	protected final AtomicLong idFactory = new AtomicLong();
	
	
	/** Read only clone of the main, used by calling threads for reads while the main is defragged */
	protected ChronicleCache readOnlyCache = null;
	
	/** The write guarding spin lock */
	protected final SpinLock writeSpinLock = UnsafeAdapter.allocateSpinLock();
	/** The read only redirect spin lock */
	protected final SpinLock readOnlySpinLock = UnsafeAdapter.allocateSpinLock();

	/** The cache name */
	protected final String cacheName;
	
	
	/** The general config for chronicles */
	protected final ChronicleConfiguration config = new ChronicleConfiguration();
	
	
	/** The cache mapping the global id of a metric to the underlying chronicle index it is stored in */
	protected final ILongKeyCache idCache;
	/** The string key cache to keep metric names synchronized with */
	protected final IStringKeyCache nameCache;
	/** The opaque key cache to keep metric names synchronized with */
	protected final IByteArrayKeyCache opaqueCache;
	
	
	/** The store name index chronicle */
	protected IndexedChronicle indexedChronicle;
	/** This instance's chronicle writer */
	protected ChronicleCacheWriter writer; 
	
	/** Instance logger */
	protected final Logger log;
	
	/** The default charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	
	
	/**
	 * Acquires the ChronicleCache instance
	 * @return the singleton ChronicleCache instance
	 */
	public static ChronicleCache getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new ChronicleCache(CACHE_NAME);
				}
			}
		}		
		return instance.readOnlySpinLock.isLocked() ? instance.readOnlyCache : instance;
	}
	
	/**
	 * Executes a write task within a spin lock acquire/release
	 * @param task The task to execute
	 * @return the return value of the task
	 */
	public <T> T executeWriteTask(WriteTask<T> task) {
		return writer.executeWriteTask(task);
	}
	
	
	/**
	 * Returns a new assigned global id
	 * @return a new assigned global id
	 */
	long nextGlobalID() {
		return idFactory.incrementAndGet();
	}
	
	/**
	 * Initalizes a new metric entry
	 * @param chronicleIndex The chronicle index where the metric was written
	 * @return the assigned global id
	 */
	long newMetric(long chronicleIndex) {
		long gid = nextGlobalID();
		idCache.put(gid, chronicleIndex);
		return gid;
	}
	
	/**
	 * Cleans the caches of values for the passed deleted metric.
	 * @param deletedMetric the deleted metric
	 */
	public void processDeleteCacheClean(IMetricDefinition deletedMetric) {
		if(deletedMetric!=null) {
			idCache.remove(deletedMetric.getId());
			String name = deletedMetric.getName();
			byte[] opaqueKey = deletedMetric.getOpaqueKey();
			if(name!=null) nameCache.remove(name);
			if(opaqueKey!=null) opaqueCache.remove(opaqueKey);
		}
	}
	
	/**
	 * Adds the passed global id to the name and opaque caches if the keys are not null
	 * @param gid The global id
	 * @param name The metric name
	 * @param opaqueKey The metric opaque key
	 */
	public void addNewMetricToCache(long gid, String name, byte[] opaqueKey) {
		if(name!=null) nameCache.put(name, gid);
		if(opaqueKey!=null) opaqueCache.put(opaqueKey, gid);
	}
	
	/**
	 * Returns the chronicle index for the passed global ID
	 * @param globalId The metric global ID
	 * @return the chronicle index the metric resides at or the no_value token
	 */
	public long getChronicleIndex(long globalId) {
		return idCache.get(globalId);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ChronicleCache clone() {
		return new ChronicleCache(this, RO_NAME);
	}
	
	private static final String[] EXTENSIONS = new String[] {".data", ".index"};
	
	static void closeDeleteReinit(ChronicleCache cache, boolean reinit) {
		cache.purge();
		try { cache.writer.writer.close(); } catch (Exception ex) {/* No Op */}
		try { cache.indexedChronicle.close(); } catch (Exception ex) {/* No Op */}
		ChronicleConfiguration config = new ChronicleConfiguration();
		for(String ext: EXTENSIONS) {
			File f = new File(config.dataDir, String.format("%s%s", cache.cacheName, ext));
			boolean deleted = f.delete();
			cache.log.info("Deleted Chronicle File [{}]:{}", f.getAbsoluteFile(), deleted);
		}
		if(reinit) {
			try {
				String fileName = config.dataDir.getAbsolutePath() + File.separator + cache.cacheName;
				final boolean isMain = CACHE_NAME.equals(cache.cacheName);
				cache.indexedChronicle = new IndexedChronicle(fileName, config.dataBitSizeHint, ByteOrder.nativeOrder(), true, false);
				cache.indexedChronicle.useUnsafe(config.unsafe);
				cache.indexedChronicle.multiThreaded(isMain);
				cache.indexedChronicle.setEnumeratedMarshaller(UnsafeMetricDefinitionMarshaller.INSTANCE);
				cache.writer = new ChronicleCacheWriter(cache.indexedChronicle, cache.writeSpinLock, cache.nameCache, cache.opaqueCache);
				cache.log.info(StringHelper.banner("Reinited ChronicleCache [%s]", cache.cacheName));
				if(isMain) cache.load();
				
			} catch (Exception ex) {
				cache.log.fatal("Failed to reinit Cache [{}]", cache.cacheName, ex);
				try { Thread.sleep(2000 ); Runtime.getRuntime().halt(-1); } catch (Exception x) {
					cache.log.fatal("Was going to sleep before killing the JVM, but that failed too", x);
					Runtime.getRuntime().halt(-1);
				}
			}
		}
	}
	
	/**
	 * Defragments the main chronicle cache
	 */
	synchronized void defragChronicle() {
		log.info("Defrag Starting....");
		ChronicleCache defrag = null;
		Excerpt defragEx = null;
		try {
			readOnlyCache = clone();
			readOnlySpinLock.xlock(true);
			defrag = new ChronicleCache(DEFRAG_NAME);
			defrag.purge();						
			ElapsedTime et = SystemClock.startClock();
			final long preSize = indexedChronicle.size();
			long defragCount = 0, deletedCount = 0, retainedCount = 0;
			writer.spinLock.xlock(true);
			Excerpt from = writer.writer;
			Excerpt to = defrag.newExcerpt();
			defragEx = to;
			from.toStart();
			UnsafeMetricDefinition metricCursor = null;
			while(from.hasNextIndex()) {
				if(!from.nextIndex()) break;
				defragCount++;
				from.position(0);
				if(from.readByte()==1) {
					deletedCount++;
					continue;
				}
				final long index = from.index();
				if(index==-1) continue;
				if(from.capacity() < IMetricDefinition.BASE_SIZE) {
					log.info("Corrupt Entry: {}, Size:{}", index, from.capacity());
					deletedCount++;
					continue;
				}
				if(metricCursor==null) {
					metricCursor = new UnsafeMetricDefinition(from);
				} else {
					metricCursor.readMarshallable(from);
				}
				final long globalId = metricCursor.getId();
				to.startExcerpt(metricCursor.getByteSize());
				metricCursor.writeMarshallable(to);
				retainedCount++;
			}
			log.info("Defrag Phase 1 Complete in {} ms. Read {} Records. Deleted: {}, Retained: {}", 
					et.elapsedMs(), defragCount, deletedCount, retainedCount);
			from.close();
			purge();
			closeDeleteReinit(this, true);
			from = to;
			to = writer.writer;
			from.toStart();
			to.toStart();
			long writeBacks = 0;
			while(from.hasNextIndex()) {
				writeBacks++;
				if(!from.nextIndex()) break;
				from.position(0);
				final long index = from.index();
				if(index==-1) continue;
				if(metricCursor==null) {
					metricCursor = new UnsafeMetricDefinition(from);
				} else {
					metricCursor.readMarshallable(from);
				}
				final long globalId = metricCursor.getId();
				to.startExcerpt(metricCursor.getByteSize());
				metricCursor.writeMarshallable(to);
			}
			long postSize = indexedChronicle.size();
			log.info("Defrag Phase 2 Complete in {} ms. Writebacks: {}. Defragged from {} records to {} records", 
					et.elapsedMs(), writeBacks, preSize, postSize);
			clearCache();
			load();
			log.info("Defrag Phase 3 Complete in {} ms.\n\tID Cache: {}\n\tName Cache: {}\n\tOpaque Cache: {}", 
					et.elapsedMs(), idCache.size(), nameCache.size(), opaqueCache.size());
			//  === CLEANUP ===
			if(defragEx != null) try { defragEx.close(); } catch (Exception x) { /* No Op */}
			defragEx = null;
			if(defrag != null) try { defrag.indexedChronicle.clear(); } catch (Exception x) { /* No Op */} 
			if(defrag != null) try { defrag.indexedChronicle.close(); } catch (Exception x) { /* No Op */}
			defrag = null;
			
			System.gc();
		} finally {
			readOnlySpinLock.xunlock();
			writer.spinLock.xunlock();

			if(defragEx != null) try { defragEx.close(); } catch (Exception x) { /* No Op */}
			defragEx = null;
			if(defrag != null) try { defrag.indexedChronicle.clear(); } catch (Exception x) { /* No Op */} 
			if(defrag != null) try { defrag.indexedChronicle.close(); } catch (Exception x) { /* No Op */}
			defrag = null;
			if(readOnlyCache != null) try { readOnlyCache.purge(); } catch (Exception x) { /* No Op */} 
			if(readOnlyCache != null) try { readOnlyCache.indexedChronicle.close(); } catch (Exception x) { /* No Op */}
			readOnlyCache = null;
			System.gc();
			ChronicleTools.deleteOnExit(config.dataDir.getAbsolutePath() + File.separator + DEFRAG_NAME);
			ChronicleTools.deleteOnExit(config.dataDir.getAbsolutePath() + File.separator + RO_NAME);
		}
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		ChronicleCache cc = null;
		int insertLoops = 10;
		UnsafeMetricDefinitionMarshaller marshaller = UnsafeMetricDefinitionMarshaller.INSTANCE; 
		
		try {
			cc = ChronicleCache.getInstance();
			cc.purge();
//			int cnt = 0;
//			for(String uuid: TestKeyedCaches.uuidSamples.values()) {
//				marshaller.createOrUpdate(uuid);
//				marshaller.createOrUpdate(uuid.getBytes());
//				cnt++; if(cnt==10) break;
//			}
			
//			if(cc.indexedChronicle.size()<1) {
			ElapsedTime et = SystemClock.startClock();
			for(int i = 0; i < insertLoops; i++) {
				for(String uuid: TestKeyedCaches.uuidSamples.values()) {
					marshaller.createOrUpdate(uuid);
					marshaller.createOrUpdate(uuid.getBytes());
				}
			}
			cc.log.info("Inital Load Test Loop: {}", et.printAvg("\n\tCreateOrUpdates", TestKeyedCaches.uuidSamples.size() * insertLoops * 2));
//			}
			cc.log.info("ID Cache Size: {}", cc.idCache.size());
			cc.log.info("Name Cache Size: {}", cc.nameCache.size());
			cc.log.info("Opaque Cache Size: {}", cc.opaqueCache.size());
			cc.log.info("Chronicle Cache Size: {}", cc.indexedChronicle.size());
			
			cc.log.info("================== Initial Inserts Complete =================="); 

			for(String uuid: TestKeyedCaches.uuidSamples.values()) {
				marshaller.createOrUpdate(uuid, uuid.getBytes());
			}
			
			et = SystemClock.startClock();
			for(int i = 0; i < insertLoops; i++) {
				for(String uuid: TestKeyedCaches.uuidSamples.values()) {
					marshaller.createOrUpdate(uuid);
					marshaller.createOrUpdate(uuid.getBytes());
					marshaller.createOrUpdate(uuid, uuid.getBytes());
				}
			}
			cc.log.info("Test Merge Overlay: {}", et.printAvg("\n\tCreateOrUpdates", TestKeyedCaches.uuidSamples.size() * insertLoops * 3));
			//			
			cc.log.info("ID Cache Size: {}", cc.idCache.size());
			cc.log.info("Name Cache Size: {}", cc.nameCache.size());
			cc.log.info("Opaque Cache Size: {}", cc.opaqueCache.size());
			cc.log.info("Chronicle Cache Size: {}", cc.indexedChronicle.size());
			cc.log.info("Chronicle Deleted Entries: {}", cc.writer.deletedEntries);
//			
			cc.log.info("================== Combined Inserts Complete ==================");
//			
			et = SystemClock.startClock();
			cc.defragChronicle();
			cc.log.info("Defrag: {}", et.printAvg("\n\tDefrag", 1));
			cc.log.info("ID Cache Size: {}", cc.idCache.size());
			cc.log.info("Name Cache Size: {}", cc.nameCache.size());
			cc.log.info("Opaque Cache Size: {}", cc.opaqueCache.size());
			cc.log.info("Chronicle Cache Size: {}", cc.indexedChronicle.size());

//			et = SystemClock.startClock();
//			for(int i = 0; i < insertLoops; i++) {
//				for(String uuid: TestKeyedCaches.uuidSamples.values()) {
//					marshaller.createOrUpdate(uuid);
//					marshaller.createOrUpdate(uuid.getBytes());
//					marshaller.createOrUpdate(uuid, uuid.getBytes());
//				}
//			}
//			cc.log.info("Post Defrag Test Merge Overlay: {}", et.printAvg("\n\tCreateOrUpdates", TestKeyedCaches.uuidSamples.size() * insertLoops * 3));
			
			
//			cc.indexedChronicle.clear();
//			cc.clearCache();
//			
//			cc.log.info("UUID Sample Size: {}", TestKeyedCaches.uuidSamples.size());
//			for(String uuid: TestKeyedCaches.uuidSamples.values()) {
//				cc.writer.newMetricEntry(uuid);
//				cc.writer.newMetricEntry(uuid.getBytes());
//			}
//			cc.clearCache();
//			cc.load();			
//			ElapsedTime et = SystemClock.startClock();
//			for(int i = 0; i < 10; i++) {
//				for(String uuid: TestKeyedCaches.uuidSamples.values()) {
//					cc.writer.newMetricEntry(uuid);
//					cc.writer.newMetricEntry(uuid.getBytes());
//				}
//			}
//			cc.log.info("Loaded Test Data: {}", et.printAvg("Entries", TestKeyedCaches.uuidSamples.size() * 10));
//			cc.log.info("Name Cache Size: {}", cc.nameCache.size());
//			cc.log.info("Opaque Cache Size: {}", cc.opaqueCache.size());
		} catch (Throwable t) {
			if(cc!=null) {
				cc.log.info("Name Cache Size: {}", cc.nameCache.size());
				cc.log.info("Opaque Cache Size: {}", cc.opaqueCache.size());
			}
			t.printStackTrace(System.err);
		} finally {
//			ChronicleTools.deleteOnExit(new ChronicleConfiguration().dataDir.getAbsolutePath());
			if(cc!=null) try { cc.indexedChronicle.close(); } catch (Exception x) {/* No Op */}
		}		
	}
	
	/**
	 * Clears the chronicle, name and opaque caches
	 */
	private void purge() {
		indexedChronicle.clear();
		if(nameCache!=null) {
			nameCache.clear();
			nameCache.trimToSize();
		}
		if(opaqueCache!=null) {
			opaqueCache.clear();
			opaqueCache.trimToSize();
		}
		if(idCache!=null) {
			idCache.clear();
			idCache.trimToSize();
		}
		
	}
	
	/**
	 * Clears the name and opaque caches
	 */
	private void clearCache() {
		if(nameCache!=null) nameCache.clear();
		if(opaqueCache!=null) opaqueCache.clear();
		if(idCache!=null) idCache.clear();
	}
	

	private ChronicleCache(ChronicleCache otherCache, String cacheName) {
		this.cacheName = cacheName;
		final boolean isMain = CACHE_NAME.equals(cacheName);
		log = LogManager.getLogger(getClass().getName() + "." + cacheName);
		String fileName = null;
		try {
			fileName = config.dataDir.getAbsolutePath() + File.separator + cacheName;
			indexedChronicle = new IndexedChronicle(fileName, config.dataBitSizeHint, ByteOrder.nativeOrder(), true, false);
			indexedChronicle.useUnsafe(config.unsafe);
			indexedChronicle.multiThreaded(isMain);
			indexedChronicle.setEnumeratedMarshaller(new ChronicleCacheEntryMarshaller(writeSpinLock));
			idCache = new LongKeyChronicleCache((LongKeyChronicleCache)otherCache.idCache);
			nameCache = new StringKeyChronicleCache((StringKeyChronicleCache)otherCache.nameCache);
			opaqueCache = new ByteArrayKeyChronicleCache((ByteArrayKeyChronicleCache)otherCache.opaqueCache);
			writer = new ChronicleCacheWriter(indexedChronicle, writeSpinLock, nameCache, opaqueCache);
			log.info(StringHelper.banner("Created ChronicleCache [%s]", cacheName));
			if(isMain) load();
		} catch (IOException e) {
			String msg = "Failed to create IndexedChronicle in [" + fileName + "]";
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
		
	}
	
	/**
	 * Creates a new ChronicleCache
	 * @param cacheName The logical name of this chronicle store
	 */
	private ChronicleCache(String cacheName) {
		this.cacheName = cacheName;
		final boolean isMain = CACHE_NAME.equals(cacheName);
		log = LogManager.getLogger(getClass().getName() + "." + cacheName);
		String fileName = null;
		try {
			fileName = config.dataDir.getAbsolutePath() + File.separator + cacheName;
			indexedChronicle = new IndexedChronicle(fileName, config.dataBitSizeHint, ByteOrder.nativeOrder(), true, false);
			indexedChronicle.useUnsafe(config.unsafe);
			indexedChronicle.multiThreaded(isMain);
			indexedChronicle.setEnumeratedMarshaller(new ChronicleCacheEntryMarshaller(writeSpinLock));
			idCache = new LongKeyChronicleCache(config.idCacheInitialCapacity, config.idCacheLoadFactor);
			if(isMain) {
				nameCache = new StringKeyChronicleCache(config.nameCacheInitialCapacity, config.nameCacheLoadFactor);
				opaqueCache = new ByteArrayKeyChronicleCache(config.opaqueCacheInitialCapacity, config.opaqueCacheLoadFactor);
			} else {
				nameCache = null;
				opaqueCache = null;
			}
			writer = new ChronicleCacheWriter(indexedChronicle, writeSpinLock, nameCache, opaqueCache);
			log.info(StringHelper.banner("Created ChronicleCache [%s]", cacheName));
			if(isMain) load();
		} catch (IOException e) {
			String msg = "Failed to create IndexedChronicle in [" + fileName + "]";
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}
	
	/**
	 * Creates a new excerpt for this chronicle 
	 * @return the created excerpt
	 */
	protected Excerpt newExcerpt() {
		return indexedChronicle.createExcerpt();
	}
	
	/**
	 * Loads the chronicle into cache
	 */
	protected void load() {
		Excerpt exc = null; 
		long loadCount = 0, nameCacheEntries = 0, opaqueCacheEntries = 0;
		UnsafeMetricDefinition metricCursor = null;
		try {
			exc = indexedChronicle.createExcerpt();			
			for(long index = 0; index < indexedChronicle.size(); index ++) {
//			while(exc.hasNextIndex()) {
				if(!exc.index(index)) break;
				if(exc.capacity()<1) {
					log.warn("Index {} had zero capacity", exc.index());
					continue;
				}
				exc.position(0);
				if(exc.readByte()==1) {
					continue;
				}
//				final long index = exc.index();
				if(index==-1) continue;
				if(exc.capacity() < IMetricDefinition.BASE_SIZE) {
					log.info("Corrupt Entry: {}, Size:{}", index, exc.capacity());
					continue;
				}
				if(metricCursor==null) {
					metricCursor = new UnsafeMetricDefinition(exc);
				} else {
					metricCursor.readMarshallable(exc);
				}
				final long globalId = metricCursor.getId();
				idCache.put(globalId, exc.index());
				loadCount++;
				
				final String name = metricCursor.getName();
				final byte[] opaqueKey = metricCursor.getNameBytes();
				
				if(name!=null) {
					nameCache.put(name, globalId);
					nameCacheEntries++;
				}
				if(opaqueKey!=null) {
					opaqueCache.put(opaqueKey, globalId);
					opaqueCacheEntries++;
				}
			}
			System.gc();
			log.info(StringHelper.banner("Loaded %s Cache Records\n\tName Cache: %s\n\tOpaque Cache: %s" , loadCount, nameCacheEntries, opaqueCacheEntries));
		} finally {
			if(exc!=null) try { exc.close(); } catch (Exception x) {/* No Op */}
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
