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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.pag.Constants;
import org.helios.pag.util.SystemClock;
import org.helios.pag.util.SystemClock.ElapsedTime;
import org.helios.pag.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: TestStringKeyChronicleCache</p>
 * <p>Description: A basic test case for {@link StringKeyChronicleCache<}</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.TestStringKeyChronicleCache</code></p>
 */

public class TestStringKeyChronicleCache {
	
	private static final Logger LOG = LogManager.getLogger("TESTC");
	private static final MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty(Constants.TRACK_MEM_PROP, "true");
		LOG.info("TestStringKeyChronicleCache");
		int sampleSize = 1500000;
		String[] samples = new String[sampleSize];
		Map<String, Long> sampleMap = new HashMap<String, Long>(sampleSize);		
		for(int i = 0; i < sampleSize; i++) {
			String s = UUID.randomUUID().toString();
			samples[i] = s;
			sampleMap.put(s, (long)i);
		}
		LOG.info("Samples Created");
		ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>(sampleSize);
//		ConcurrentHashMap<StringPointer, Long> cache = new ConcurrentHashMap<StringPointer, Long>(sampleSize);
		StringKeyChronicleCache cache = new StringKeyChronicleCache(sampleSize);
//		OffHeapCharSequenceCache cache = new OffHeapCharSequenceCache(sampleSize, 0.75f);
		ElapsedTime et = SystemClock.startClock();
		for(int i = 0; i < sampleSize; i++) {
			map.put(samples[i], (long)i);
			cache.put(samples[i], i);
		}
		LOG.info("Samples Loaded");
		int matches = 0;
		int fails = 0;
		for(int i = 0; i < sampleSize; i++) {			
			long v = map.get(samples[i]);
			long k = cache.get(samples[i]);
			if(v==k) matches++;
			else fails++;			
		}
		LOG.info("Test Complete. Passes: {}, Fails: {}\nElapsed: {}", matches, fails, et.printAvg("Samples", sampleSize));		
		map.clear();
		cache.clear();
		try { Thread.sleep(1000); } catch (Exception x) {}
		LOG.info("Starting Map Only Put Test");
		et = SystemClock.startClock();
		for(int i = 0; i < sampleSize; i++) {
			map.put(samples[i], (long)i);
		}
		LOG.info("Map Only Put Test: {}", et.printAvg("Samples", sampleSize));
		et = SystemClock.startClock();
		for(int i = 0; i < sampleSize; i++) {
			long v = map.get(samples[i]);
			if(v!=i) {
				LOG.error("Map Test Failed. {} !=  {}", v, i);
				return;
			}
		}
		LOG.info("Map Only Get Test: {}", et.printAvg("Samples", sampleSize));
		System.gc(); System.gc();
		long before = memBean.getHeapMemoryUsage().getUsed();
		LOG.info("\n\tMap Heap Used Before Clear: {} KB\n\tCache Off-Heap Used Before Clear: {} KB", before/1024, UnsafeAdapter.unsafeMemoryStats.getTotalAllocatedMemoryKb());
		map.clear();
		System.gc(); System.gc();
		try { Thread.sleep(1000); } catch (Exception x) {}
		long after = memBean.getHeapMemoryUsage().getUsed();
		LOG.info("\n\tMap Heap Used After Clear: {} KB\n\tCache Off-Heap Used After Clear: {} KB", after/1024, UnsafeAdapter.unsafeMemoryStats.getTotalAllocatedMemoryKb());
		LOG.info("----> Map Mem Used: {} KB", (before-after)/1024);
		try { Thread.sleep(1000); } catch (Exception x) {}
		LOG.info("Starting Cache Only Put Test");
		et = SystemClock.startClock();
//		cache.putAll(sampleMap);
		for(int i = 0; i < sampleSize; i++) {
//			cache.putAll(sampleMap);
			cache.put(samples[i], i);
		}
		LOG.info("Cache Only Put Test: {}", et.printAvg("Samples", sampleSize));		
		et = SystemClock.startClock();
		for(int i = 0; i < sampleSize; i++) {
			long v = cache.get(samples[i]);
			if(v!=i) {
				LOG.error("Cache Test Failed. {} !=  {}", v, i);
				return;
			}
		}
		LOG.info("Cache Only Get Test: {}", et.printAvg("Samples", sampleSize));
		System.gc(); System.gc();
		before = memBean.getHeapMemoryUsage().getUsed();
		LOG.info("\n\tCache Heap Used Before Clear: {} KB\n\tCache Off-Heap Used Before Clear: {} KB", before/1024, UnsafeAdapter.unsafeMemoryStats.getTotalAllocatedMemoryKb());
		cache.clear();
		cache.trimToSize();
		System.gc(); System.gc();
		try { Thread.sleep(1000); } catch (Exception x) {}
		after = memBean.getHeapMemoryUsage().getUsed();
		LOG.info("\n\tCache Heap Used After Clear: {} KB\n\tCache Off-Heap Used After Clear: {} KB", after/1024, UnsafeAdapter.unsafeMemoryStats.getTotalAllocatedMemoryKb());
		LOG.info("----> Cache Mem Used: {} KB", (before-after)/1024);

		
		
		
		
	}
	

}
