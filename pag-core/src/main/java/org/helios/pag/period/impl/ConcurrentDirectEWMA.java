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
package org.helios.pag.period.impl;

import java.util.Date;

import org.helios.pag.util.unsafe.UnsafeAdapter;
import org.helios.pag.util.unsafe.UnsafeAdapter.DoubleCallable;
import org.helios.pag.util.unsafe.UnsafeAdapter.LongCallable;

/**
 * <p>Title: ConcurrentDirectEWMA</p>
 * <p>Description: A thread safe version of {@link DirectEWMA} which is safe to be accessed by concurrent threads at the small cost of a spin lock on each access.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.period.impl.ConcurrentDirectEWMA</code></p>
 */

public class ConcurrentDirectEWMA extends DirectEWMA {

	/** The offset of the spin lock */
	public final static byte XLOCK = AVERAGE + UnsafeAdapter.LONG_SIZE;
	/** The total memory allocation  */
	public final static byte CTOTAL = XLOCK + UnsafeAdapter.DOUBLE_SIZE;

	/**
	 * Creates a new ConcurrentDirectEWMA
	 * @param windowSize The length of the sliding window in ms.
	 */
	public ConcurrentDirectEWMA(long windowSize) {
		super(windowSize, CTOTAL);
		UnsafeAdapter.putLong(address + XLOCK, UnsafeAdapter.NO_LOCK);
	}

	/**
	 * Returns the timestamp of the last sample as a long UTC.
	 * @return the timestamp of the last sample 
	 */
	public long getLastSample() {
		return UnsafeAdapter.runInLock(address + XLOCK,  new LongCallable() {
			@Override
			public long longCall() {
				return UnsafeAdapter.getLong(address + LAST_SAMPLE);
			}
		});
	}
	
	/**
	 * Returns the last computed average.
	 * @return the last computed average 
	 */
	public double getAverage() {
		return UnsafeAdapter.runInLock(address + XLOCK,  new DoubleCallable() {
			@Override
			public double doubleCall() {
				return UnsafeAdapter.getDouble(address + AVERAGE);
			}
		});
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("EWMA [");
		UnsafeAdapter.runInLock(address + XLOCK, new Runnable() {
			public void run() {
				b.append("ts:").append(new Date(UnsafeAdapter.getLong(address + LAST_SAMPLE)));
				b.append(", avg:").append(UnsafeAdapter.getDouble(address + AVERAGE));				
			}
		});
		b.append("]");		
		return b.append("]").toString();
	}
	
	
}
