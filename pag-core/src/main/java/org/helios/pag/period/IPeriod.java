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
 * <p>Title: IPeriod</p>
 * <p>Description: Represents the "header" of a collection of period aggregators that all have the same period window in common</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.period.IPeriod</code></p>
 */

public interface IPeriod {
	
	/**
	 * Returns the duration of the period in s. 
	 * @return the duration of the period
	 */
	public int getPeriod();

	/**
	 * Returns the minimum period granularity in seconds.
	 * @return the minimum period granularity in seconds.
	 */
	public int getPeriodGranularity();
	
	/**
	 * Returns the maximum period in seconds.
	 * @return the maximum period in seconds.
	 */
	public int getMaxPeriod();
	
}
