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
package org.helios.pag.store.redis;


/**
 * <p>Title: RedisClientStatValueParser</p>
 * <p>Description: Defines a parser for the name/value pair in a RedisClientStat string.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.RedisClientStatValueParser</code></p>
 */

public interface RedisClientStatValueParser {
	/**
	 * Parses a name value pair in the format <b><code>&lt;name&gt;=&lt;value&gt;</code></b> and returns the value.
	 * @param nameValuePair The name value string
	 * @return the value
	 */
	public Object parseValue(String nameValuePair);
	
	/**
	 * <p>Title: IntParser</p>
	 * <p>Description: An int value parser</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>org.helios.pag.store.redis.RedisClientStatValueParser.IntParser</code></p>
	 */
	static class IntParser implements RedisClientStatValueParser {
		public Object parseValue(String value) {
			try {
				return Integer.parseInt(value.trim());
			} catch (Exception ex) {
				return -1;
			}
		}
	}
	/**
	 * <p>Title: LongParser</p>
	 * <p>Description: An long value parser</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>org.helios.pag.store.redis.RedisClientStatValueParser.LongParser</code></p>
	 */
	static class LongParser implements RedisClientStatValueParser {
		public Object parseValue(String value) {
			try {
				return Long.parseLong(value.trim());
			} catch (Exception ex) {
				return -1L;
			}
		}
	}
	
	/**
	 * <p>Title: StringParser</p>
	 * <p>Description: A string value parser</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>org.helios.pag.store.redis.RedisClientStatValueParser.StringParser</code></p>
	 */
	static class StringParser implements RedisClientStatValueParser {
		public Object parseValue(String value) {
			try {
				return value.trim();
			} catch (Exception ex) {
				return "";
			}
		}
	}
	/** A static constant reusable string value parser */
	public static final RedisClientStatValueParser STRING_PARSER = new StringParser();
	/** A static constant reusable int value parser */
	public static final RedisClientStatValueParser INT_PARSER = new IntParser();
	/** A static constant reusable long value parser */
	public static final RedisClientStatValueParser LONG_PARSER = new LongParser();
	
}
