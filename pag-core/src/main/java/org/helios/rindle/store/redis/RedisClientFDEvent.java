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
package org.helios.rindle.store.redis;

import java.util.EnumSet;
import java.util.Set;

/**
 * <p>Title: RedisClientFDEvent</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.RedisClientFDEvent</code></p>
 */

public enum RedisClientFDEvent implements RedisClientStatValueParser {
	/** The client socket is readable (event loop) */
	R("Client socket is readable", "Readable"),
	/** The client socket is writable (event loop) */
	W("Client socket is writable", "Writable");
	
	private RedisClientFDEvent(String description, String shortName) {
		this.description = description;
		this.shortName = shortName;
	}
	
	/** The event description */
	public final String description;
	/** The event short name */
	public final String shortName;
	
	/**
	 * Decodes the passed string to an array of RedisClientFDEvent
	 * @param events The event string to decode
	 * @return the array of represented client events
	 */
	public static RedisClientFDEvent[] decode(String events) {
		Set<RedisClientFDEvent> rdfs = EnumSet.noneOf(RedisClientFDEvent.class);
		if(events==null || events.trim().isEmpty()) return new RedisClientFDEvent[0];
		for(int x = 0; x < events.length(); x++) {
			try {
				rdfs.add(RedisClientFDEvent.valueOf(events.substring(x, x+1).toUpperCase()));
			} catch (Exception ex) {/* No Op */}
		}		
		return rdfs.toArray(new RedisClientFDEvent[rdfs.size()]);
	}
	
	/**
	 * Formats the short names of the passed events into a pipe delimited string
	 * @param events The events to format
	 * @return the formated string
	 */
	public static String format(RedisClientFDEvent...events) {
		if(events==null || events.length==0) return "";
		StringBuilder b = new StringBuilder();
		
		for(RedisClientFDEvent event: events) {
			if(event==null) continue;
			b.append(event.shortName).append("|");
		}
		if(b.length()>0) b.deleteCharAt(b.length()-1);
		return b.toString();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.RedisClientStatValueParser#parseValue(java.lang.String)
	 */
	@Override
	public Object parseValue(String value) {		
		return PARSER.parseValue(value);
	}
	
	/** Static reusable client flag value parser */
	public static final RedisClientStatValueParser PARSER = new RedisClientStatValueParser() {
		public Object parseValue(String value) {		
			return decode(value.trim());
		}		
	};
	
	
}
