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
package org.helios.rindle;

import java.util.Arrays;

import org.helios.rindle.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: Stats</p>
 * <p>Description: Statistical helper methods</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.Stats</code></p>
 */

public class Stats {

	/**
	 * Returns the median value in the long array at the passed address
	 * @param address The address of the long array
	 * @param size The size of the targetted array
	 * @return the median
	 */
	public static long medianl(long address, int size) {
		if(size==0) return 0;
		if(size==1) return UnsafeAdapter.getLong(address);
		long[] arr = UnsafeAdapter.getLongArray(address, size);
		Arrays.sort(arr);
		if(size%2==1) {
			return arr[(size/2)-1];
		}
		return (long)miniMedian(arr[(size/2)-1], arr[(size/2)]);
	}
	
	/**
	 * Returns the median value in the double array at the passed address
	 * @param address The address of the double array
	 * @param size The size of the targetted array
	 * @return the median
	 */
	public static double mediand(long address, int size) {
		if(size==0) return 0;
		if(size==1) return UnsafeAdapter.getDouble(address);
		double[] arr = UnsafeAdapter.getDoubleArray(address, size);
		Arrays.sort(arr);
		if(size%2==1) {
			return arr[(size/2)-1];
		}
		return miniMedian(arr[(size/2)-1], arr[(size/2)]);
	}
	
	
	/**
	 * Returns the median between two doubles
	 * @param d1 The first double
	 * @param d2 The second double
	 * @return the median between the 2 doubles
	 */
	public static double miniMedian(double d1, double d2) {
		return (d1 + d2)/2d;
	}
	
	private Stats() {}

}
