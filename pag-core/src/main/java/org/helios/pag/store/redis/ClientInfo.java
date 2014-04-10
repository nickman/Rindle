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

import java.util.regex.Pattern;

/**
 * <p>Title: ClientInfo</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.ClientInfo</code></p>
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
	protected int multiCount = -1;
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
	
	// ID Key:  <address> + fd
	// split address:  <X>:port  (what do we call X ?)
	// touch flag, so we can keep track of "gone" connections
	// create new, update with ClientStat
	
	/** Single space splitter expression */
	protected static final Pattern SPACE_SPLITTER = Pattern.compile(" ");
	/** Equals splitter expression */
	protected static final Pattern EQ_SPLITTER = Pattern.compile("=");
	
	
	/**
	 * Creates a new ClientInfo
	 * @param clientInfoDetails The returned details for a connection
	 */
	public ClientInfo(String clientInfoDetails) {
		String[] nvps = SPACE_SPLITTER.split(clientInfoDetails);
		for(String nvp: nvps) {
			String[] frags = EQ_SPLITTER.split(nvp);
			RedisClientStat stat = RedisClientStat.decode(frags[0]);
			switch(stat) {
			case ADDR:
				address = frags[1];
				break;
			case AGE:
				age = Integer.parseInt(frags[1]);
				break;
			case CMD:
				lastCommand = frags[1];
				break;
			case DB:
				 database = Integer.parseInt(frags[1]);
				break;
			case EVENTS:
				fdEvents = RedisClientFDEvent.decode(frags[1]);
				break;
			case FD:
				fileDescriptor = Long.parseLong(frags[1]);
				break;
			case FLAGS:
				clientFlags = RedisClientFlag.decode(frags[1]);
				break;
			case IDLE:
				idle = Integer.parseInt(frags[1]);
				break;
			case MULTI:
				multiCount = Integer.parseInt(frags[1]);
				break;
			case OBL:
				outputBufferLength = Integer.parseInt(frags[1]);
				break;
			case OLL:
				outputListLength = Integer.parseInt(frags[1]);
				break;
			case OMEM:
				outputBufferMemUsage = Integer.parseInt(frags[1]);
				break;
			case PSUB:
				psubCount = Integer.parseInt(frags[1]);
				break;
			case QBUF:
				queryBufferLength = Integer.parseInt(frags[1]);
				break;
			case QBUF_FREE:
				queryBufferFree = Integer.parseInt(frags[1]);
				break;
			case SUB:
				subCount = Integer.parseInt(frags[1]);
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
	public String[] getClientFlagNames() {
		RedisClientFlag[] flags = getClientFlags();
		String[] flagNames = new String[flags.length];
		for(int i = 0; i < flags.length; i++) {
			flagNames[i] = flags[i].name();
		}
		return flagNames;
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
	public String[] getFdEventNames() {
		RedisClientFDEvent[] events = getFdEvents();
		String[] eventNames = new String[events.length];
		for(int i = 0; i < events.length; i++) {
			eventNames[i] = events[i].name();
		}
		return eventNames;		
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
