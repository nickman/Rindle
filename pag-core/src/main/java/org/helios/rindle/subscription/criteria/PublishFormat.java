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

import gnu.trove.map.hash.TByteObjectHashMap;

/**
 * <p>Title: PublishFormat</p>
 * <p>Description: Enumerates the formats in which published interval data can be delivered in</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.subscriptions.criteria.PublishFormat</code></p>
 */

public enum PublishFormat implements PickOne<PublishFormat>{
	/** Google protocol buffers */
	PROTOBUF,
	/** Apache thrift */
	THRIFT,	
	/** Standard JSON */
	JSON,
	/** MessagePack binary serialization format */
	MSGPACK,
	/** Standard XML */
	XML,
	/** Comma separated values */
	CSV,
	/** Off heap allocated space passed by address */
	INVM;

	private static final TByteObjectHashMap<PublishFormat> BYTE2ENUM;
	
	static {
		PublishFormat[] values = PublishFormat.values();
		BYTE2ENUM = new TByteObjectHashMap<PublishFormat>(values.length);
		for(PublishFormat pf: values) {
			BYTE2ENUM.put((byte)pf.ordinal(), pf);
		}
	}
	
	/**
	 * Decodes the passed byte to an enum member
	 * @param bcode The enum byte code
	 * @return the decoded enum member
	 */
	public static PublishFormat decode(byte bcode) {
		PublishFormat pf = BYTE2ENUM.get(bcode);
		if(pf==null) throw new IllegalArgumentException("Invalid code [" + bcode + "]");
		return pf;
	}
	
	/**
	 * Decodes the passed ordinal to an enum member
	 * @param ordinal The enum ordinal
	 * @return the decoded enum member
	 */
	public static PublishFormat decode(int ordinal) {
		PublishFormat pf = BYTE2ENUM.get((byte)ordinal);
		if(pf==null) throw new IllegalArgumentException("Invalid ordinal [" + ordinal + "]");
		return pf;
	}
	
	/**
	 * Returns the byte code for this enum member
	 * @return the byte code for this enum member
	 */
	public byte getCode() {
		return (byte)ordinal();
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.subscription.criteria.PickOne#getDefault()
	 */
	@Override
	public PublishFormat getDefault() {
		return PROTOBUF;
	}
	
}
