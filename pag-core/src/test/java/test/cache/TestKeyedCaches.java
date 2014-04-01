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
package test.cache;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.helios.pag.store.ByteArrayKeyChronicleCache;
import org.helios.pag.store.CharBufferStringKeyCache;
import org.helios.pag.store.IByteArrayKeyCache;
import org.helios.pag.store.IStringKeyCache;
import org.helios.pag.store.StringKeyChronicleCache;
import org.helios.pag.util.SystemClock;
import org.helios.pag.util.SystemClock.ElapsedTime;
import org.junit.Assert;
import org.junit.Test;

import test.base.BaseTest;

/**
 * <p>Title: TestKeyedCaches</p>
 * <p>Description: Test cases for string byte array keyed long value classes</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.cache.TestKeyedCaches</code></p>
 */
public class TestKeyedCaches extends BaseTest {
	/** The cache no entry value, meaning a non-existent value not in the cache */
	public static final long NO_ENTRY_VALUE = -1L;

	/** The sample size */
	static final int SAMPLE_SIZE = 100000;
	/** The sample index array */
	static final long[] sampleIndex = new long[SAMPLE_SIZE];
	/** The sample index list */
	static final List<Long> sampleList;
	
	/** The number of warmup loops */
	static final int warmupLoops = 10;
	
	/** Static test samples */
	static final SortedMap<Long, String> uuidSamples;
	/** Static test samples key-reversed */
	static final SortedMap<String, Long> uuidSamplesRev;
	
	/** The memory mx bean to capture heap allocation */
	static final MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
	
	/** Some random characters */
	public static final char[] RANDOM_CHARS = new char[]{'$','(',')','*','+','-','.','?','[','\\',']','^','{','|','}' };
	/** The max random char index */
	public static final int RANDOM_CHAR_MAX = RANDOM_CHARS.length-1;
	
	static {
		Set<String> tmpSet = new HashSet<String>(SAMPLE_SIZE);
		for(long i = 0; i < SAMPLE_SIZE; i++) {
			tmpSet.add(randomSample());
		}
		log("Initial Sample Size: %s", tmpSet.size());
		if(tmpSet.size()<SAMPLE_SIZE) {
			while(tmpSet.size()<SAMPLE_SIZE) {
				tmpSet.add(randomSample());
			}
		}
		log("Final Sample Size: %s", tmpSet.size());
		SortedMap<Long, String> tmp = new TreeMap<Long, String>();
		SortedMap<String, Long> tmpRev = new TreeMap<String, Long>();
		List<Long> tmpList = new ArrayList<Long>(SAMPLE_SIZE);
		long index = 0;
		for(String sample: tmpSet) {
			tmp.put(index, sample);
			tmpRev.put(sample, index);
			sampleIndex[(int) index] = index;
			tmpList.add(index);
			index++;
		}
		uuidSamples = Collections.unmodifiableSortedMap(tmp);
		uuidSamplesRev = Collections.unmodifiableSortedMap(tmpRev);
		sampleList = tmpList;
		log("Samples: %s", uuidSamples.size());
		log("Samples Rev: %s", uuidSamplesRev.size());
		Collections.shuffle(sampleList, RANDOM);
		tmpSet.clear();
	}

	protected static String randomSample() {
		StringBuilder b = new StringBuilder();
		int fragments = nextPosInt(10);
		for(int x = 0; x < fragments; x++) {
			b.append(getRandomFragment()).append(RANDOM_CHARS[nextPosInt(RANDOM_CHAR_MAX)]);
		}
		return b.toString();
	}
	/**
	 * Tests the {@link StringKeyChronicleCache}
	 */
	@Test
	public void testLongHashedStringKeyChronicleCache() {
		final IStringKeyCache nameCache = new StringKeyChronicleCache(SAMPLE_SIZE, 0.75f);		
		for(Map.Entry<Long, String> entry: uuidSamples.entrySet()) {
			nameCache.put(entry.getValue(), entry.getKey());			
		}
		Assert.assertEquals("Unexpected cache size", SAMPLE_SIZE,  nameCache.size());
		log("Cache Size: %s", nameCache.size());
		for(long index: sampleList) {
			long key = nameCache.get(uuidSamples.get(index));
			Assert.assertNotEquals("Key was not found", NO_ENTRY_VALUE, key);
			Assert.assertEquals("Key was not expected ", index, key);
		}
	}
	
	/**
	 * Tests the OffHeap {@link CharBufferStringKeyCache}
	 */
	@Test
	public void testOffHeapCharBufferStringKeyChronicleCache() {
		final IStringKeyCache nameCache = new CharBufferStringKeyCache(SAMPLE_SIZE, 0.75f, true);		
		for(Map.Entry<Long, String> entry: uuidSamples.entrySet()) {
			nameCache.put(entry.getValue(), entry.getKey());			
		}
		Assert.assertEquals("Unexpected cache size", SAMPLE_SIZE,  nameCache.size());
		log("Cache Size: %s", nameCache.size());
		for(long index: sampleList) {
			long key = nameCache.get(uuidSamples.get(index));
			Assert.assertNotEquals("Key was not found", NO_ENTRY_VALUE, key);
			Assert.assertEquals("Key was not expected ", index, key);
		}
	}
	
	/**
	 * Tests the OnHeap {@link CharBufferStringKeyCache}
	 */
	@Test
	public void testOnHeapCharBufferStringKeyChronicleCache() {
		final IStringKeyCache nameCache = new CharBufferStringKeyCache(SAMPLE_SIZE, 0.75f, false);		
		for(Map.Entry<Long, String> entry: uuidSamples.entrySet()) {
			nameCache.put(entry.getValue(), entry.getKey());			
		}
		Assert.assertEquals("Unexpected cache size", SAMPLE_SIZE,  nameCache.size());
		log("Cache Size: %s", nameCache.size());
		for(long index: sampleList) {
			long key = nameCache.get(uuidSamples.get(index));
			Assert.assertNotEquals("Key was not found", NO_ENTRY_VALUE, key);
			Assert.assertEquals("Key was not expected ", index, key);
		}
	}
	
	
	/**
	 * Compares IStringKeyCache implementation performance after warmup
	 */
	@Test
	public void compareCaches() {
		List<String> results = new ArrayList<String>();
		// =============================================================================================
		final IStringKeyCache offHeapCBCache = new CharBufferStringKeyCache(SAMPLE_SIZE, 0.75f, true);
		for(int i = 0; i < warmupLoops; i++) { testCache(offHeapCBCache); offHeapCBCache.clear(); }
		ElapsedTime et = SystemClock.startClock();
		testCache(offHeapCBCache);
		StringBuilder result = new StringBuilder(et.printAvg("OffHeap CharBufferStringKeyCache Samples", SAMPLE_SIZE));
		long before = beforeClear();
		offHeapCBCache.clear(); 
		long diff = afterClear(before);
		results.add(result.append("\n\t\tHeap Usage: ").append(diff/1024).append(" KB").toString());
		log("OffHeap CharBufferStringKeyCache Complete");
		// =============================================================================================
		final IStringKeyCache onHeapCBCache = new CharBufferStringKeyCache(SAMPLE_SIZE, 0.75f, false);
		for(int i = 0; i < warmupLoops; i++) { testCache(onHeapCBCache); onHeapCBCache.clear(); }
		et = SystemClock.startClock();
		testCache(onHeapCBCache); 
		result = new StringBuilder(et.printAvg("OnHeap CharBufferStringKeyCache Samples", SAMPLE_SIZE));
		before = beforeClear();
		onHeapCBCache.clear();
		diff = afterClear(before);
		results.add(result.append("\n\t\tHeap Usage: ").append(diff/1024).append(" KB").toString());		
		log("OnHeap CharBufferStringKeyCache Complete");
		// =============================================================================================		
		final IStringKeyCache longHashNameCache = new StringKeyChronicleCache(SAMPLE_SIZE, 0.75f);
		for(int i = 0; i < warmupLoops; i++) { testCache(longHashNameCache); longHashNameCache.clear(); }
		et = SystemClock.startClock();
		testCache(longHashNameCache); 
		result = new StringBuilder(et.printAvg("OffHeap CharBufferStringKeyCache Samples", SAMPLE_SIZE));
		before = beforeClear();		
		log("LongHash Cache Size: %s", longHashNameCache.size());
		longHashNameCache.clear();
		longHashNameCache.trimToSize();
		diff = afterClear(before);
		results.add(result.append("\n\t\tHeap Usage: ").append(diff/1024).append(" KB").toString());				
		log("StringKeyChronicleCache Complete");
		// =============================================================================================
		log("\n\t==================================\n\tResults\n\t==================================");
		for(String s: results) {
			log("\n\t%s", s);
		}
		
		log("\n\t==================================\n");
		

	}
	
	public void testCache(IStringKeyCache nameCache) {
		for(Map.Entry<Long, String> entry: uuidSamples.entrySet()) {
			nameCache.put(entry.getValue(), entry.getKey());			
		}
		Assert.assertEquals("Unexpected cache size", SAMPLE_SIZE,  nameCache.size());
		for(long index: sampleList) {
			long key = nameCache.get(uuidSamples.get(index));
			Assert.assertNotEquals("Key was not found", NO_ENTRY_VALUE, key);
			Assert.assertEquals("Key was not expected ", index, key);
		}
	}
	
	/**
	 * Tests the {@link ByteArrayKeyChronicleCache}
	 */
	@Test
	public void testByteArrayKeyChronicleCache() {
		final IByteArrayKeyCache bCache = new ByteArrayKeyChronicleCache(SAMPLE_SIZE, 0.75f);		
		for(Map.Entry<Long, String> entry: uuidSamples.entrySet()) {
			bCache.put(entry.getValue().getBytes(), entry.getKey());			
		}
		Assert.assertEquals("Unexpected cache size", SAMPLE_SIZE,  bCache.size());
		log("Cache Size: %s", bCache.size());
		for(long index: sampleList) {
			long key = bCache.get(uuidSamples.get(index).getBytes());
			Assert.assertNotEquals("Key was not found", NO_ENTRY_VALUE, key);
			Assert.assertEquals("Key was not expected ", index, key);
		}
	}
	
	
	protected long beforeClear() {
		memBean.gc(); memBean.gc(); 
		return memBean.getHeapMemoryUsage().getUsed();
	}
	
	protected long afterClear(long beforeClear) {
		memBean.gc(); memBean.gc();
		return beforeClear - memBean.getHeapMemoryUsage().getUsed();
	}

}
