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
package org.helios.rindle.subscription.criteria;

import java.util.Map;

import org.helios.rindle.util.enums.BitMaskedEnum;

/**
 * <p>Title: Delivery</p>
 * <p>Description: Defines the trigger events for data delivery to a subscriber</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.subscription.criteria.Delivery</code></p>
 */

public enum Delivery implements PickOne<Delivery> {
	/** Data is delivered each interval */
	ON_INTERVAL,
	/** Data is delivered each interval if it has changed from the prior interval */
	ON_INTERVAL_CHANGE,
	/** Data is delivered each interval if the state meets the provided custom criteria */
	ON_INTERVAL_CUSTOM,
	/** Data is delivered on each input incident if the data changes */
	ON_CHANGE,
	/** Data is delivered on each input incident */
	ON_DATA;
	
	/** A map of enums keyed by the enum ordinal */
	public static final Map<Integer, Delivery> ORD2ENUM = BitMaskedEnum.Support.generateIntOrdinalMap(Delivery.values());
	

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.subscription.criteria.PickOne#getDefault()
	 */
	@Override
	public Delivery getDefault() {
		return ON_INTERVAL;
	}
}
