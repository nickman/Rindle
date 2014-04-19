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
 * <p>Title: Format</p>
 * <p>Description: Defines the data format options for incoming and/or outgoing data</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.subscription.criteria.Format</code></p>
 */

public enum Format implements PickOne<Format> {
	/** Comma separated values */
	CSV,
	/** JSON format */
	JSON,
	/** Google protocol buffers */
	PROTOBUF,
	/** Apache thrift serialized data */
	THRIFT,
	/** MessagePack binary serialization format */
	MSGPACK,
	/** Standard XML */
	XML,
	/** Off heap allocated space passed by address */
	INVM;
	
	
	/** A map of enums keyed by the enum ordinal */
	public static final Map<Integer, Format> ORD2ENUM = BitMaskedEnum.Support.generateIntOrdinalMap(Format.values());
	

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.subscription.criteria.PickOne#getDefault()
	 */
	@Override
	public Format getDefault() {
		return JSON;
	}
}
