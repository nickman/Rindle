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
package org.helios.rindle.subscription.criteria;

import java.util.Map;

import org.helios.rindle.util.enums.BitMaskedEnum;

/**
 * <p>Title: IntervalOption</p>
 * <p>Description: An aggregate enumeration of interval options for interval data subscriptions</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.subscription.criteria.IntervalOption</code></p>
 */

public enum IntervalOption implements PickOne<IntervalOption> {
	/** The requested interval period in seconds */
	PERIOD,		// seconds
	/** The interval retention indicating if min/max/avg bases should be reatined across intervals, or reset */
	RETENTION,  
	/** The subscription publication elligibility determination for the subscribed metrics */
	DELIVERY;  
	
	/** A map of enums keyed by the enum ordinal */
	public static final Map<Integer, IntervalOption> ORD2ENUM = BitMaskedEnum.Support.generateIntOrdinalMap(IntervalOption.values());
	

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.subscription.criteria.PickOne#getDefault()
	 */
	@Override
	public IntervalOption getDefault() {
		return PERIOD;
	}	
	
	
}
