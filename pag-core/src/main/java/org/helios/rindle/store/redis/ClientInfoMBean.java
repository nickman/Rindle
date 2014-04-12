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
package org.helios.rindle.store.redis;

/**
 * <p>Title: ClientInfoMBean</p>
 * <p>Description: The JMX MBean interface for {@link ClientInfo}</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.ClientInfoMBean</code></p>
 */

public interface ClientInfoMBean {

	/**
	 * Returns the client local address
	 * @return the address
	 */
	public String getAddress();


	/**
	 * Returns the file descriptor 
	 * @return the fileDescriptor
	 */
	public long getFileDescriptor();


	/**
	 * Returns the age of the client in seconds
	 * @return the age
	 */
	public int getAge();


	/**
	 * Returns the client idle time in seconds
	 * @return the idle
	 */
	public int getIdle();


	/**
	 * Returns the client status flag names
	 * @return the clientFlags
	 */
	public String getClientFlagNames();


	/**
	 * Returns the connected DB id
	 * @return the database
	 */
	public int getDatabase();


	/**
	 * Returns the number of subscriptions
	 * @return the subCount
	 */
	public int getSubCount();


	/**
	 * Returns the number of pattern subscriptions
	 * @return the psubCount
	 */
	public int getPsubCount();


	/**
	 * Returns number of pending multicount ops
	 * @return the multiCount
	 */
	public int getMultiCount();


	/**
	 * Returns the query buffer length in bytes
	 * @return the queryBufferLength
	 */
	public int getQueryBufferLength();


	/**
	 * Returns the query buffer free space in bytes
	 * @return the queryBufferFree
	 */
	public int getQueryBufferFree();


	/**
	 * Returns the output buffer length in bytes
	 * @return the outputBufferLength
	 */
	public int getOutputBufferLength();


	/**
	 * Returns the output list length where replies are queued when the buffer is full
	 * @return the outputListLength
	 */
	public int getOutputListLength();


	/**
	 * Returns the output buffer memory usage in bytes 
	 * @return the outputBufferMemUsage
	 */
	public int getOutputBufferMemUsage();


	/**
	 * Returns the file descriptor event names
	 * @return the fdEvents
	 */
	public String getFdEventNames();


	/**
	 * Returns this client's last command
	 * @return the lastCommand
	 */
	public String getLastCommand();


	/**
	 * Returns the assigned client name
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Returns the provider type name
	 * @return the provider type name
	 */
	public String getProviderType();

}
