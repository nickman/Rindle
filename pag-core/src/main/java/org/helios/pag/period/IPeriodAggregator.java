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
package org.helios.pag.period;


/**
 * <p>Title: IPeriodAggregator</p>
 * <p>Description: Defines an aggregation node which is fed raw data points for a specific id.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.period.IPeriodAggregator</code></p>
 */

public interface IPeriodAggregator {
	
	/**
	 * Returns the globally unique identifier for a metric
	 * @return the metric id
	 */
	public long getId();
	
	/**
	 * Returns the timestamp of the last raw metric submitted for this aggregator
	 * @return the timestamp of the last raw metric
	 */
	public long getLastTime();
	
	/**
	 * Indicates if this aggregator is accumulating the raw data stream
	 * @return tue if aggregating, false otherwise
	 */
	public boolean isRawEnabled();
	
	
	/**
	 * Returns the number of raw values received in the current period
	 * @return the number of raw values
	 */
	public long getCount();
	
	
	/**
	 * Returns true if this is a long based metric
	 * @return true for long based metric, false otherwise
	 */
	public boolean isLong();

	/**
	 * Returns true if this is a double based metric
	 * @return true for double based metric, false otherwise
	 */
	public boolean isDouble();
	
	/**
	 * Returns the mean value
	 * @return the mean value
	 */
	public double getDoubleMean();

	/**
	 * Returns the minimum value
	 * @return the minimum value
	 */
	public double getDoubleMin();
	
	/**
	 * Returns the raw data as an array of doubles
	 * @return an array of doubles
	 */
	public double[] getDoubles();
	
	/**
	 * Returns the maximum value
	 * @return the maximum value
	 */
	public double getDoubleMax();
	
	/**
	 * Returns the mean value
	 * @return the mean value
	 */
	public long getLongMean();

	/**
	 * Returns the minimum value
	 * @return the minimum value
	 */
	public long getLongMin();
	
	/**
	 * Returns the raw data as an array of longs
	 * @return an array of longs
	 */
	public long[] getLongs();
	

	/**
	 * Returns the maximum value
	 * @return the maximum value
	 */
	public long getLongMax();
	
	/**
	 * Returns the mean value
	 * @return the mean value
	 */
	public Number getMean();

	/**
	 * Returns the minimum value
	 * @return the minimum value
	 */
	public Number getMin();

	/**
	 * Returns the maximum value
	 * @return the maximum value
	 */
	public Number getMax();
	
	/**
	 * Returns the median value in the raw data buffer
	 * @return the median value in the raw data buffer
	 */
	public long getLongMedian();

	/**
	 * Returns the median value in the raw data buffer
	 * @return the median value in the raw data buffer
	 */
	public double getDoubleMedian();
	
	/**
	 * Returns the median value in the raw data buffer
	 * @return the median value in the raw data buffer
	 */
	public Number getMedian();
	
}
