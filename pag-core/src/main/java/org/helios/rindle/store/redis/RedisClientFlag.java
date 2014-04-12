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
 * <p>Title: RedisClientFlag</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.RedisClientFlag</code></p>
 */

public enum RedisClientFlag implements RedisClientStatValueParser {
	/** The client is a slave in MONITOR mode */
	O("The client is a slave in MONITOR mode", "Monitor"),
	/** The client is a normal slave server */
	S("The client is a normal slave server", "Slave:"),
	/** The client is a master */
	M("The client is a master", "Master"),
	/** The client is in a MULTI/EXEC context */
	X("The client is in a MULTI/EXEC context", "MultiExec"),
	/** The client is waiting in a blocking operation */
	B("The client is waiting in a blocking operation", "Blocking"),
	/** The client is waiting for a VM I/O (deprecated) */
	I("The client is waiting for a VM I/O (deprecated)", "VM IO"),
	/** a watched keys has been modified - EXEC will fail */
	D("a watched keys has been modified - EXEC will fail", "WatchKey Mod"),
	/** connection to be closed after writing entire reply */
	C("connection to be closed after writing entire reply", "Closing"),
	/** The client is unblocked */
	U("The client is unblocked", "Unblocked"),
	/** connection to be closed ASAP */
	A("connection to be closed ASAP", "Closing ASAP"),
	/** no specific flag set */
	N("no specific flag set", "None");
	
	private RedisClientFlag(String description, String shortName) {
		this.description = description;
		this.shortName = shortName;
	}
	
	/**
	 * Decodes the passed string to an array of RedisClientFlags
	 * @param flags The flag string to decode
	 * @return the array of represented client flags
	 */
	public static RedisClientFlag[] decode(String flags) {
		Set<RedisClientFlag> rdfs = EnumSet.noneOf(RedisClientFlag.class);
		if(flags==null || flags.trim().isEmpty()) return new RedisClientFlag[0];
		for(int x = 0; x < flags.length(); x++) {
			try {
				rdfs.add(RedisClientFlag.valueOf(flags.substring(x, x+1).toUpperCase()));
			} catch (Exception ex) {/* No Op */}
		}		
		return rdfs.toArray(new RedisClientFlag[rdfs.size()]);
	}
	
	/**
	 * Formats the short names of the passed flags into a pipe delimited string
	 * @param flags The flags to format
	 * @return the formated string
	 */
	public static String format(RedisClientFlag...flags) {
		if(flags==null || flags.length==0) return "";
		StringBuilder b = new StringBuilder();
		
		for(RedisClientFlag flag: flags) {
			if(flag==null) continue;
			b.append(flag.shortName).append("|");
		}
		if(b.length()>0) b.deleteCharAt(b.length()-1);
		return b.toString();
	}
	
	/** The flag description */
	public final String description;
	/** The flag short name */
	public final String shortName;
	
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
