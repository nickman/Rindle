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
package test.ewma;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.rindle.period.impl.ConcurrentDirectEWMA;
import org.helios.rindle.period.impl.DirectEWMA;
import org.helios.rindle.util.SystemClock;
import org.helios.rindle.util.SystemClock.ElapsedTime;
import org.helios.rindle.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: EWMATest</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.ewma.EWMATest</code></p>
 */

public class EWMATest {
	private static final Logger LOG = LogManager.getLogger(EWMATest.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("org.helios.rindle.umem.trackmem", "true");
		LOG.info("EWMA Test");
		Random R = new Random(System.currentTimeMillis());
		int sampleCount = 15000000;
		long windowSize = 5000;
		double[] samples = new double[sampleCount];
		for(int i = 0; i < sampleCount; i++) {
			samples[i] = R.nextGaussian();
		}
		LOG.info("Sample Data Generated");
		DirectEWMA de = new DirectEWMA(windowSize);
		ConcurrentDirectEWMA cde = new ConcurrentDirectEWMA(windowSize);
		ElapsedTime et = SystemClock.startClock();
		for(double sample: samples) {
			de.append(sample);
			cde.append(sample);
		}		
		LOG.info("Warmup Complete");
		de = new DirectEWMA(windowSize);
		cde = new ConcurrentDirectEWMA(windowSize);
		et = SystemClock.startClock();
		for(double sample: samples) {
			de.append(sample);
		}		
		LOG.info(et.printAvg("Samples for DirectEWMA", sampleCount));
		et = SystemClock.startClock();
		for(double sample: samples) {
			cde.append(sample);
		}		
		LOG.info(et.printAvg("Samples for ConcurrentDirectEWMA", sampleCount));
		System.out.println("\n==============================\n");
		
		LOG.info("DE:\n\t{}", de);
		LOG.info("CDE:\n\t{}", cde);
		de=null; cde=null;
		System.gc();
		//try { Thread.sleep(30000000); } catch (Exception e) {}
//		int allocations = 10000;
//		int size = 1000000;
//		while(true) {
//			long[] addresses = new long[allocations];
//			long totalMem = 0;
//			LOG.info("====== Allocating ======");
//			for(int i = 0; i < allocations; i++) {
//				addresses[i] = UnsafeAdapter.allocateMemory(size);
//				totalMem += size;
//			}
//			double d = totalMem / 1024 / 1024;
//			LOG.info("====== Allocated {} MB ======", d);
//			try { Thread.sleep(10000); } catch (Exception e) {}
//			LOG.info("====== Freeing ======");
//			for(int i = 0; i < allocations; i++) {
//				UnsafeAdapter.freeMemory(addresses[i]);
//			}			
//			System.gc();
//			LOG.info("====== Freed ======");
//			try { Thread.sleep(10000); } catch (Exception e) {}			
//		}
		
	}
	
	

}
