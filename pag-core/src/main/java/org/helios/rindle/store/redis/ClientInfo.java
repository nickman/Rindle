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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>Title: ClientInfo</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.ClientInfo</code></p>
 */

public class ClientInfo implements ClientInfoMBean {
	/** The client address/port */
	protected String address = null;
	/** The file descriptor corresponding to the socket */
	protected long fileDescriptor = -1;
	/** The total duration of the connection in seconds */
	protected int age = -1;
	/** The  idle time of the connection in seconds */
	protected int idle = -1;
	/** The client flags */
	protected RedisClientFlag[] clientFlags = new RedisClientFlag[0];
	/** The current database id */
	protected int database = -1;
	/** The number of channel subscriptions */
	protected int subCount = -1;
	/** The number of pattern matching subscriptions */
	protected int psubCount = -1;
	/** The number of commands in a MULTI/EXEC context */
	protected int multiCount = 0;
	/** The query buffer length (0 means no query pending) */
	protected int queryBufferLength = -1;
	/** The query buffer free space (0 means the buffer is full) */
	protected int queryBufferFree = -1;
	/** The output buffer length */
	protected int outputBufferLength = -1;
	/** The output list length (replies are queued in this list when the buffer is full) */
	protected int outputListLength = -1;
	/** The output buffer memory usage */
	protected int outputBufferMemUsage = -1;
	/** The file descriptor events */
	protected RedisClientFDEvent[] fdEvents = new RedisClientFDEvent[0];
	/** The Last command executed for this client */
	protected String lastCommand;
	/** The name assigned to this client */
	protected String name;
	
	/** A reference to the originating ClientInfoProvider so we can test if it is connected */
	protected final ClientInfoProvider provider;
	
	// ID Key:  <address> + fd
	// split address:  <X>:port  (what do we call X ?)
	// touch flag, so we can keep track of "gone" connections
	// create new, update with ClientStat
	
	/** Single space splitter expression */
	protected static final Pattern SPACE_SPLITTER = Pattern.compile(" ");
	/** Equals splitter expression */
	protected static final Pattern EQ_SPLITTER = Pattern.compile("=");
	
	/** The platform charset */
	public static final Charset CHARSET  = Charset.defaultCharset();
	
	/**
	 * Creates a new uninitialized ClientInfo
	 * @param name The client name
	 * @param addressKey The address key
	 * @param provider The originating provider
	 */
	public ClientInfo(String name, String addressKey, ClientInfoProvider provider) {
		this.name = name;
		this.address = addressKey;
		this.provider = provider;
	}
	
	/**
	 * Returns the originating provider
	 * @return the originating provider
	 */
	public ClientInfoProvider getProvider() {
		return provider;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.ClientInfoMBean#getProviderType()
	 */
	@Override
	public String getProviderType() {
		return provider.getClass().getName();
	}
	
	/**
	 * Indicates if the originating provider is connected
	 * @return true if the originating provider is connected, false otherwise
	 */
	public boolean isConnected() {
		return provider.isConnected();
	}
	
	/**
	 * Updates this client info with the latest polled data
	 * @param stats A map of redis client stats 
	 */
	public void update(Map<RedisClientStat, Object> stats) {
		if(stats==null || stats.isEmpty()) return;
		for(Map.Entry<RedisClientStat, Object> entry: stats.entrySet()) {
			RedisClientStat rcs = entry.getKey();
			switch(rcs) {
			case AGE:
				this.age = (Integer)entry.getValue();
				break;
			case CMD:
				this.lastCommand = (String)entry.getValue();
				break;
			case DB:
				this.database = (Integer)entry.getValue();
				break;
			case EVENTS:
				this.fdEvents = (RedisClientFDEvent[])entry.getValue();
				break;
			case FD:
				this.fileDescriptor = (Integer)entry.getValue();
				break;
			case FLAGS:
				this.clientFlags = (RedisClientFlag[])entry.getValue();
				break;
			case IDLE:
				this.idle = (Integer)entry.getValue();
				break;
			case MULTI:
				this.multiCount = (Integer)entry.getValue();
				break;
			case OBL:
				this.outputBufferLength = (Integer)entry.getValue();
				break;
			case OLL:
				this.outputListLength = (Integer)entry.getValue();
				break;
			case OMEM:
				this.outputBufferMemUsage = (Integer)entry.getValue();
				break;
			case PSUB:
				this.psubCount = (Integer)entry.getValue();
				break;
			case QBUF:
				this.queryBufferLength = (Integer)entry.getValue();
				break;
			case QBUF_FREE:
				this.queryBufferFree = (Integer)entry.getValue();
				break;
			case SUB:
				this.subCount = (Integer)entry.getValue();
				break;
			default:
				break;				
			}
		}
	}
	


	/**
	 * Returns the client local address
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}


	/**
	 * Returns the file descriptor 
	 * @return the fileDescriptor
	 */
	public long getFileDescriptor() {
		return fileDescriptor;
	}


	/**
	 * Returns the age of the client in seconds
	 * @return the age
	 */
	public int getAge() {
		return age;
	}


	/**
	 * Returns the client idle time in seconds
	 * @return the idle
	 */
	public int getIdle() {
		return idle;
	}


	/**
	 * Returns the client status flags
	 * @return the clientFlags
	 */
	public RedisClientFlag[] getClientFlags() {
		return clientFlags;
	}
	
	/**
	 * Returns the client status flag names
	 * @return the clientFlags
	 */
	public String getClientFlagNames() {
		RedisClientFlag[] flags = getClientFlags();
		if(flags.length==0) return "";		
		StringBuilder flagNames = new StringBuilder();		
		for(int i = 0; i < flags.length; i++) {
			flagNames.append(flags[i].shortName).append("|");
		}
		return flagNames.deleteCharAt(flagNames.length()-1).toString();
	}



	/**
	 * Returns the connected DB id
	 * @return the database
	 */
	public int getDatabase() {
		return database;
	}


	/**
	 * Returns the number of subscriptions
	 * @return the subCount
	 */
	public int getSubCount() {
		return subCount;
	}


	/**
	 * Returns the number of pattern subscriptions
	 * @return the psubCount
	 */
	public int getPsubCount() {
		return psubCount;
	}


	/**
	 * Returns number of pending multicount ops
	 * @return the multiCount
	 */
	public int getMultiCount() {
		return multiCount;
	}


	/**
	 * Returns the query buffer length in bytes
	 * @return the queryBufferLength
	 */
	public int getQueryBufferLength() {
		return queryBufferLength;
	}


	/**
	 * Returns the query buffer free space in bytes
	 * @return the queryBufferFree
	 */
	public int getQueryBufferFree() {
		return queryBufferFree;
	}


	/**
	 * Returns the output buffer length in bytes
	 * @return the outputBufferLength
	 */
	public int getOutputBufferLength() {
		return outputBufferLength;
	}


	/**
	 * Returns the output list length where replies are queued when the buffer is full
	 * @return the outputListLength
	 */
	public int getOutputListLength() {
		return outputListLength;
	}


	/**
	 * Returns the output buffer memory usage in bytes 
	 * @return the outputBufferMemUsage
	 */
	public int getOutputBufferMemUsage() {
		return outputBufferMemUsage;
	}


	/**
	 * Returns file descriptor events
	 * @return the fdEvents
	 */
	public RedisClientFDEvent[] getFdEvents() {
		return fdEvents;
	}
	
	/**
	 * Returns the file descriptor event names
	 * @return the fdEvents
	 */
	public String getFdEventNames() {
		RedisClientFDEvent[] events = getFdEvents();
		if(events.length==0) return "";
		StringBuilder eventNames = new StringBuilder();
		for(int i = 0; i < events.length; i++) {
			eventNames.append(events[i].shortName).append("|");
		}
		return eventNames.deleteCharAt(eventNames.length()-1).toString();		
	}
	


	/**
	 * Returns this client's last command
	 * @return the lastCommand
	 */
	public String getLastCommand() {
		return lastCommand;
	}


	/**
	 * Returns the assigned client name
	 * @return the name
	 */
	public String getName() {
		return name;
	}

}
