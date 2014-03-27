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

import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.pag.period.impl.ConcurrentDirectEWMA;
import org.helios.pag.period.impl.SynchronizedDirectEWMA;
import org.helios.pag.util.SystemClock;
import org.helios.pag.util.SystemClock.ElapsedTime;

/**
 * <p>Title: MultiThreadedEWMATest</p>
 * <p>Description: Multi threaded EWMA test</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.ewma.MultiThreadedEWMATest</code></p>
 */

public class MultiThreadedEWMATest {
	private static final Logger LOG = LogManager.getLogger("MTEWMATest");
	private static final int CORES = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("org.helios.pag.umem.trackmem", "true");
		LOG.info("MT EWMA Test");
		Random R = new Random(System.currentTimeMillis());
		int sampleCount = 15000;
		int warmupLoops = 1500000;
		long windowSize = 5000;
		final int loops = 1000;
		int threadCount = 1;
		final double[] samples = new double[sampleCount];
		for(int i = 0; i < sampleCount; i++) {
			//samples[i] = R.nextGaussian();
			samples[i] = Math.abs(R.nextInt(100));
		}
		LOG.info("Sample Data Generated");
		ConcurrentDirectEWMA warmupCde = new ConcurrentDirectEWMA(windowSize);
//		SynchronizedDirectEWMA warmupCde = new SynchronizedDirectEWMA(windowSize);  
		for(int i = 0; i < warmupLoops; i++) {
			warmupCde.append(i);
		}
		warmupCde = null;
		System.gc();
		LOG.info("Warmup Complete");
		final ConcurrentDirectEWMA cde = new ConcurrentDirectEWMA(windowSize);
//		final SynchronizedDirectEWMA cde = new SynchronizedDirectEWMA(windowSize);
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch completionLatch = new CountDownLatch(threadCount);
		Thread[] threads = new Thread[threadCount];
		for(int i = 0; i < threadCount; i++) {
			threads[i] = new Thread("TestThread#" + i) {
				public void run() {
					try {
						startLatch.await();
					} catch (Exception ex) {
						LOG.error("Thread Failed", ex);
					}
					LOG.info("Thread {} Started", Thread.currentThread());
					for(int x = 0; x < loops; x++) {
						for(double d: samples) {
							cde.append(d);
						}
					}
					completionLatch.countDown();
					LOG.info("Thread {} Completed", Thread.currentThread());
				}
			};
			threads[i].setDaemon(true);
			threads[i].start();
		}
		
		ElapsedTime et = SystemClock.startClock();
		startLatch.countDown();
		try {
			completionLatch.await();
			LOG.info(et.printAvg("Samples for ConcurrentDirectEWMA", threadCount * loops * sampleCount));
			LOG.info(cde);
			//cde = null;
			System.gc();
			Thread.sleep(3000);
		} catch (Exception x) {
			LOG.error("Main error", x);
		}
	}

}
