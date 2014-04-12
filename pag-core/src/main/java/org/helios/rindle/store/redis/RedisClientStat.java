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

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: RedisClientStat</p>
 * <p>Description: Enumeration of the redis client stats.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.RedisClientStat</code></p>
 */

public enum RedisClientStat implements RedisClientStatValueParser {
	/** The address/port of the client */
	ADDR("The address/port of the client", "", STRING_PARSER),
	/** The file descriptor corresponding to the socket */
	FD("The file descriptor corresponding to the socket", "", INT_PARSER),
	/** The total duration of the connection in seconds */
	AGE("The total duration of the connection in seconds", "", INT_PARSER),
	/** The idle time of the connection in seconds */
	IDLE("The idle time of the connection in seconds", "", INT_PARSER),
	/** The client flags (see {@link RedisClientFlag}) */
	FLAGS("The client flags", "", RedisClientFlag.PARSER),
	/** The current database ID */
	DB("The current database ID", "", INT_PARSER),
	/** The number of channel subscriptions */
	SUB("The number of channel subscriptions", "", INT_PARSER),
	/** The number of pattern matching subscriptions */
	PSUB("The number of pattern matching subscriptions", "", INT_PARSER),
	/** The number of commands in a MULTI/EXEC context */
	MULTI("The number of commands in a MULTI/EXEC context", "", INT_PARSER),
	/** The query buffer length (0 means no query pending) */
	QBUF("The query buffer length", "", INT_PARSER),
	/** The free space of the query buffer (0 means the buffer is full) */
	QBUF_FREE("The free space of the query buffer", "", INT_PARSER),
	/** The output buffer length */
	OBL("The output buffer length", "", INT_PARSER),
	/** The output list length */
	OLL("The output list length (replies are queued in this list when the buffer is full)", "", INT_PARSER),
	/** The output buffer memory usage */
	OMEM("The output buffer memory usage", "", INT_PARSER),
	/** The file descriptor events (see {@link RedisClientFDEvent}) */
	EVENTS("The file descriptor events", "", RedisClientFDEvent.PARSER),
	/** The last command played */
	CMD("The last command played", "", STRING_PARSER),
	/** The assigned client name */
	NAME("The assigned client name", "", STRING_PARSER);

	/** The key pattern for a client info line */
	private static final String NAME_KEY = "name=";
	/** The length of the key pattern for a client info line */
	private static final int NAME_KEY_LENGTH = NAME_KEY.length();
	/** The const default empty map */
	private static final Map<RedisClientStat, Object> EMPTY_MAP = Collections.unmodifiableMap(new EnumMap<RedisClientStat, Object>(RedisClientStat.class));
	

	
	private RedisClientStat(String description, String shortName, RedisClientStatValueParser parser) {
		this.description = description;
		this.shortName = shortName;
		this.parser = parser;
	}
	
	/** The flag description */
	public final String description;
	/** The flag short name */
	public final String shortName;
	/** The name/value parser */
	private final RedisClientStatValueParser parser;
	
	/** The client info string splitter */
	private static final Pattern STAT_SPLITTER = Pattern.compile("(.*?=.*?(?:\\s|$))");
	/** The platform EOL */
	private static final String EOL = System.getProperty("line.separator", "\n");
	/** Platform EOL splitter */
	private static final Pattern EOL_SPLITTER = Pattern.compile(EOL);
	
	/**
	 * Parses a client info string from redis into a map of of maps of stats keyed by the client name
	 * @param info The redis client info string
	 * @return the stats map
	 */
	public static Map<String, Map<RedisClientStat, Object>> parseClientInfo(String info) {
		Map<String, Map<RedisClientStat, Object>> map = new HashMap<String, Map<RedisClientStat, Object>>();
		if(info==null || info.trim().isEmpty()) return map;
		String[] lines = EOL_SPLITTER.split(info.trim());
		for(String line: lines) {
			if(line==null || line.trim().isEmpty()) continue;
			Map<RedisClientStat, Object> emap = extract(line);
			map.put((String) emap.get(NAME), emap);			
		}
		return map;
	}
	
	
	
	
	/**
	 * Extracts the metrics client info text for the specified client name
	 * @param name The client name
	 * @param info the client info text to parse
	 * @return a map of client info stats
	 */
	public static Map<RedisClientStat, Object> extract(String name, String info) {		
		if(info==null || info.trim().isEmpty()) return EMPTY_MAP;
		String[] lines = EOL_SPLITTER.split(info.trim());
		for(String line: lines) {
			if(line==null || line.trim().isEmpty()) continue;
			if(isClientInfoLine(name, line)) {
				return extract(line);
			}
		}
		return EMPTY_MAP;
	}
	
	
	/**
	 * Determines if the passed line has the name specified
	 * @param name The client name we're looking for
	 * @param line The line to test
	 * @return true for a match, false otherwise
	 */
	public static boolean isClientInfoLine(String name, String line) {
		int index = line.indexOf(NAME_KEY);
		if(index==-1) return false;
		String ename = line.substring(index + NAME_KEY_LENGTH, line.indexOf(' ', index));
		return name.equals(ename);		
	}
	
	
	/**
	 * Extracts the metrics from a single line of client info text
	 * @param line the line of text to parse
	 * @return a map of client info stats
	 */
	public static Map<RedisClientStat, Object> extract(String line) {		
		if(line==null || line.trim().isEmpty()) return EMPTY_MAP;
		Map<RedisClientStat, Object> map = new EnumMap<RedisClientStat, Object>(RedisClientStat.class);
		Matcher m = STAT_SPLITTER.matcher(line.trim());
		while(m.find()) {
			String pair = m.group(1);
			int index = pair.indexOf('=');
			if(index==-1) continue;
	        String name = pair.substring(0, index).trim().toUpperCase().replace('-', '_');
	        String value = pair.substring(index+1).trim();
	        try {
	        	RedisClientStat rcs = RedisClientStat.valueOf(name);
	        	map.put(rcs, rcs.parseValue(value));
	        } catch (Exception ex) {/* No Op */}		        
		}
		return map;							
	}
	
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


	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.RedisClientStatValueParser#parseValue(java.lang.String)
	 */
	@Override
	public Object parseValue(String value) {
		return parser.parseValue(value);
	}
	
}
