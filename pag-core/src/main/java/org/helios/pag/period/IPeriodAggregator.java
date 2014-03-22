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
	 * Returns the number of raw values received in the current period
	 * @return the number of raw values
	 */
	public long getCount();
	
	/**
	 * Increments the count by 1 and the last time to current
	 * @return the new count
	 */
	public long increment();
	
	/**
	 * Increments the count and the last time to current
	 * @param value The amount to increment the count by
	 * @return the new count
	 */
	public long increment(long value);
	
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
}
