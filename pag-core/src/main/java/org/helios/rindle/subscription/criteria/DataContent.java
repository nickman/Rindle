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
import org.helios.rindle.util.enums.BitMaskedEnum.Support;

/**
 * <p>Title: DataContent</p>
 * <p>Description: Defines the fields of content that will be delivered to the subscriber</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.subscription.criteria.DataContent</code></p>
 */

public enum DataContent implements PickMask<DataContent> {
	/** The metric global id */
	GLOBAL_ID,
	/** The metric name */
	METRIC_NAME,
	/** The metric opaque key */
	OPAQUE_KEY,
	/** The metric data */
	DATA;	
	
	/** A map of DataContents keyed by the enum ordinal */
	public static final Map<Integer, DataContent> ORD2ENUM = BitMaskedEnum.Support.generateIntOrdinalMap(DataContent.values());
	
	/**
	 * Decodes the passed object to an enum member
	 * @param obj the object to decode
	 * @return the decoded enum member or null if not matched
	 */
	public static DataContent decode(Object obj) {
		return Support.decode(ORD2ENUM, obj);
	}
}
