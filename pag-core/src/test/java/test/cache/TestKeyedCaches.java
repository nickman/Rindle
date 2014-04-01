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
import org.helios.pag.store.ByteBufferKeyChronicleCache;
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
	public static final SortedMap<Long, String> uuidSamples; 
	/** Static test samples key-reversed */
	public static final SortedMap<String, Long> uuidSamplesRev;
	
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

	/**
	 * Creates a random string by appending random numbers of UUID fragments interlaced with random characters
	 * @return a random string
	 */
	protected static String randomSample() {
		StringBuilder b = new StringBuilder();
		int fragments = nextPosInt(10);
		if(fragments<1) fragments = 1;
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
		testCache(new StringKeyChronicleCache(SAMPLE_SIZE, 0.75f));		
	}
	
	/**
	 * Tests the OffHeap {@link CharBufferStringKeyCache}
	 */
	@Test
	public void testOffHeapCharBufferStringKeyChronicleCache() {
		testCache(new CharBufferStringKeyCache(SAMPLE_SIZE, 0.75f, true));		
	}
	
	/**
	 * Tests the OnHeap {@link CharBufferStringKeyCache}
	 */
	@Test
	public void testOnHeapCharBufferStringKeyChronicleCache() {
		testCache(new CharBufferStringKeyCache(SAMPLE_SIZE, 0.75f, false));		
	}

	/**
	 * Tests the {@link ByteArrayKeyChronicleCache}
	 */
	@Test
	public void testByteArrayKeyChronicleCache() {
		testCache(new ByteArrayKeyChronicleCache(SAMPLE_SIZE, 0.75f));		
	}
	
	/**
	 * Tests the Off Heap {@link ByteBufferKeyChronicleCache}
	 */
	@Test
	public void testOffHeapByteBufferKeyChronicleCache() {
		testCache(new ByteBufferKeyChronicleCache(SAMPLE_SIZE, 0.75f, true));		
	}
	
	/**
	 * Tests the On Heap {@link ByteBufferKeyChronicleCache}
	 */
	@Test
	public void testOnHeapByteBufferKeyChronicleCache() {
		testCache(new ByteBufferKeyChronicleCache(SAMPLE_SIZE, 0.75f, false));		
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
		offHeapCBCache.purge();
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
		onHeapCBCache.purge();
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
		longHashNameCache.purge();
		diff = afterClear(before);
		results.add(result.append("\n\t\tHeap Usage: ").append(diff/1024).append(" KB").toString());				
		log("StringKeyChronicleCache Complete");
		// =============================================================================================		
		final IByteArrayKeyCache longHashByteCache = new ByteArrayKeyChronicleCache(SAMPLE_SIZE, 0.75f);
		for(int i = 0; i < warmupLoops; i++) { testCache(longHashByteCache); longHashByteCache.clear(); }
		et = SystemClock.startClock();
		testCache(longHashByteCache); 
		result = new StringBuilder(et.printAvg("ByteArrayKeyChronicleCache Samples", SAMPLE_SIZE));
		before = beforeClear();		
		longHashByteCache.purge();
		diff = afterClear(before);
		results.add(result.append("\n\t\tHeap Usage: ").append(diff/1024).append(" KB").toString());				
		log("ByteArrayKeyChronicleCache Complete");
		// =============================================================================================		
		final IByteArrayKeyCache onHeapByteCache = new ByteBufferKeyChronicleCache(SAMPLE_SIZE, 0.75f, false);
		for(int i = 0; i < warmupLoops; i++) { testCache(onHeapByteCache); onHeapByteCache.clear(); }
		et = SystemClock.startClock();
		testCache(onHeapByteCache); 
		result = new StringBuilder(et.printAvg("OnHeap ByteBufferKeyChronicleCache Samples", SAMPLE_SIZE));
		before = beforeClear();		
		onHeapByteCache.purge();
		diff = afterClear(before);
		results.add(result.append("\n\t\tHeap Usage: ").append(diff/1024).append(" KB").toString());				
		log("OnHeap ByteBufferKeyChronicleCache Complete");		
		// =============================================================================================		
		final IByteArrayKeyCache offHeapByteCache = new ByteBufferKeyChronicleCache(SAMPLE_SIZE, 0.75f, true);
		for(int i = 0; i < warmupLoops; i++) { testCache(offHeapByteCache); offHeapByteCache.clear(); }
		et = SystemClock.startClock();
		testCache(offHeapByteCache); 
		result = new StringBuilder(et.printAvg("OffHeap ByteBufferKeyChronicleCache Samples", SAMPLE_SIZE));
		before = beforeClear();		
		offHeapByteCache.purge();
		diff = afterClear(before);
		results.add(result.append("\n\t\tHeap Usage: ").append(diff/1024).append(" KB").toString());				
		log("OffHeap ByteBufferKeyChronicleCache Complete");				
		// =============================================================================================
		log("\n\t==================================\n\tResults\n\t==================================");
		for(String s: results) {
			log("\n\t%s", s);
		}
		
		log("\n\t==================================\n");
		

	}
	
	/**
	 * Tests a key cache instance
	 * @param cache The cache to test
	 */
	public void testCache(IStringKeyCache cache) {
		// =================================================================================
		// Puts all the samples and verifies the cache size
		// =================================================================================
		for(Map.Entry<Long, String> entry: uuidSamples.entrySet()) {
			cache.put(entry.getValue(), entry.getKey());			
		}
		Assert.assertEquals("Unexpected cache size", SAMPLE_SIZE,  cache.size());
		// =================================================================================
		// For each key, validates that the bound value is the expected value
		// =================================================================================		
		for(long index: sampleList) {
			long key = cache.get(uuidSamples.get(index));
			Assert.assertEquals("Key was not expected ", index, key);
		}
		// =================================================================================
		// For each key, validates the removed value and ensures the cache now reports absent
		// =================================================================================				
		for(long index: sampleList) {
			String cacheKey = uuidSamples.get(index);
			Assert.assertNotNull("Retrieved Key was null", cacheKey);
			long cacheValue = cache.remove(cacheKey);
			Assert.assertEquals("Key was not expected", index, cacheValue);
			Assert.assertTrue("Key was not absent", !cache.containsKey(cacheKey));
			Assert.assertEquals("Lookup did not return NO_ENTRY_VALUE", NO_ENTRY_VALUE, cache.get(cacheKey));
			// put the pair back into the cache
			cache.put(cacheKey, index);
			Assert.assertEquals("Key was not expected ", index, cache.get(cacheKey));			
		}
		// =================================================================================
		// For each key, validates the replaced value
		// =================================================================================				
		for(long index: sampleList) {
			String cacheKey = uuidSamples.get(index);
			Assert.assertNotNull("Retrieved Key was null", cacheKey);
			Assert.assertTrue("Value was not replaced", cache.adjustValue(cacheKey, index * -2L));
			Assert.assertEquals("Lookup did not return negative prior value", index * -1L, cache.get(cacheKey));
			Assert.assertTrue("Value was not replaced", cache.adjustValue(cacheKey, index * 2L));
			Assert.assertEquals("Lookup did not return expected value", index, cache.get(cacheKey));
		}
		
		
		
	}
	
	/**
	 * Tests a key cache instance
	 * @param cache The cache to test
	 */
	public void testCache(IByteArrayKeyCache cache) {
		// =================================================================================
		// Puts all the samples and verifies the cache size
		// =================================================================================		
		for(Map.Entry<Long, String> entry: uuidSamples.entrySet()) {
			cache.put(entry.getValue().getBytes(), entry.getKey());			
		}
		Assert.assertEquals("Unexpected cache size", SAMPLE_SIZE,  cache.size());
		// =================================================================================
		// For each key, validates that the bound value is the expected value
		// =================================================================================				
		for(long index: sampleList) {
			long key = cache.get(uuidSamples.get(index).getBytes());
			Assert.assertEquals("Key was not expected ", index, key);
		}
		// =================================================================================
		// For each key, validates the removed value and ensures the cache now reports absent
		// =================================================================================				
		for(long index: sampleList) {
			byte[] cacheKey = uuidSamples.get(index).getBytes();
			Assert.assertNotNull("Retrieved Key was null", cacheKey);
			long cacheValue = cache.remove(cacheKey);
			Assert.assertEquals("Key was not expected", index, cacheValue);
			Assert.assertTrue("Key was not absent", !cache.containsKey(cacheKey));
			Assert.assertEquals("Lookup did not return NO_ENTRY_VALUE", NO_ENTRY_VALUE, cache.get(cacheKey));
			// put the pair back into the cache
			cache.put(cacheKey, index);
			Assert.assertEquals("Key was not expected ", index, cache.get(cacheKey));			
		}
		// =================================================================================
		// For each key, validates the replaced value
		// =================================================================================				
		for(long index: sampleList) {
			byte[] cacheKey = uuidSamples.get(index).getBytes();
			Assert.assertNotNull("Retrieved Key was null", cacheKey);
			Assert.assertTrue("Value was not replaced", cache.adjustValue(cacheKey, index * -2L));
			Assert.assertEquals("Lookup did not return negative prior value", index * -1L, cache.get(cacheKey));
			Assert.assertTrue("Value was not replaced", cache.adjustValue(cacheKey, index * 2L));
			Assert.assertEquals("Lookup did not return expected value", index, cache.get(cacheKey));
		}
	}
	
	
	
	

	
	/**
	 * Executes a GC and then returns the current heap usage
	 * @return the current heap usage in bytes
	 */
	protected long beforeClear() {
		memBean.gc(); memBean.gc(); 
		return memBean.getHeapMemoryUsage().getUsed();
	}
	
	/**
	 * Executes a GC and then returns the delta between the passed heap usage and the current. 
	 * @param beforeClear A priot reading of the heap space in bytes
	 * @return the heap space delta in bytes
	 */
	protected long afterClear(long beforeClear) {
		memBean.gc(); memBean.gc();
		return beforeClear - memBean.getHeapMemoryUsage().getUsed();
	}

}
