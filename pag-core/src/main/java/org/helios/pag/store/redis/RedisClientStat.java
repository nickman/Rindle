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
package org.helios.pag.store.redis;

/**
 * <p>Title: RedisClientStat</p>
 * <p>Description: Enumeration of the redis client stats.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.RedisClientStat</code></p>
 */

public enum RedisClientStat {
	/** The address/port of the client */
	ADDR("The address/port of the client", ""),
	/** The file descriptor corresponding to the socket */
	FD("The file descriptor corresponding to the socket", ""),
	/** The total duration of the connection in seconds */
	AGE("The total duration of the connection in seconds", ""),
	/** The idle time of the connection in seconds */
	IDLE("The idle time of the connection in seconds", ""),
	/** The client flags (see {@link RedisClientFlag}) */
	FLAGS("The client flags", ""),
	/** The current database ID */
	DB("The current database ID", ""),
	/** The number of channel subscriptions */
	SUB("The number of channel subscriptions", ""),
	/** The number of pattern matching subscriptions */
	PSUB("The number of pattern matching subscriptions", ""),
	/** The number of commands in a MULTI/EXEC context */
	MULTI("The number of commands in a MULTI/EXEC context", ""),
	/** The query buffer length (0 means no query pending) */
	QBUF("The query buffer length", ""),
	/** The free space of the query buffer (0 means the buffer is full) */
	QBUF_FREE("The free space of the query buffer", ""),
	/** The output buffer length */
	OBL("The output buffer length", ""),
	/** The output list length */
	OLL("The output list length (replies are queued in this list when the buffer is full)", ""),
	/** The output buffer memory usage */
	OMEM("The output buffer memory usage", ""),
	/** The file descriptor events (see {@link RedisClientFDEvent}) */
	EVENTS("The file descriptor events", ""),
	/** The last command played */
	CMD("The last command played", "");

	
	private RedisClientStat(String description, String shortName) {
		this.description = description;
		this.shortName = shortName;
	}
	
	/** The flag description */
	public final String description;
	/** The flag short name */
	public final String shortName;
	
	/**
	 * Decodes the passed stringy after trimming and uppercasing 
	 * @param code The code to decode
	 * @return the decoded RedisClientStat
	 */
	public static RedisClientStat decode(CharSequence code) {
		if(code==null) throw new IllegalArgumentException("The passed code was null");
		String _code = code.toString().trim().toUpperCase();
		if(_code.isEmpty()) throw new IllegalArgumentException("The passed code was empty");
		try {
			return RedisClientStat.valueOf(_code);
		} catch (Exception ex) {
			throw new IllegalArgumentException("The passed code [" + _code + "] was not a valid RedisClientStat");
		}
	}
	
}
