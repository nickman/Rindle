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

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.util.Pool;

/**
 * <p>Title: ExtendedJedisPool</p>
 * <p>Description: A Jedis pool extended for some additional functionailty</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.ExtendedJedisPool</code></p>
 */

public class ExtendedJedisPool extends Pool<ExtendedJedis> {

	/**
	 * Creates a new ExtendedJedisPool
	 * @param poolConfig The apache commons pooling generic pool configuration
	 * @param host The redis host or ip address
	 * @param port The redis listening port
	 * @param timeout The redis connection timeout in s.
	 * @param password The redis password
	 * @param database The redis DB to default to
	 * @param clientName The client name prefix to which a serial number will be appended
	 * @param listener The extended jedis lifecycle listener
	 */
	public ExtendedJedisPool(GenericObjectPoolConfig poolConfig, String host,
			int port, int timeout, String password, int database,
			String clientName, ExtendedJedisLifecycleListener listener) {
		//super(poolConfig, host, port, timeout, password, database, clientName);
		super(poolConfig, new ExtendedJedisFactory(host, port, timeout, password,
				database, clientName, listener));
	}

}
