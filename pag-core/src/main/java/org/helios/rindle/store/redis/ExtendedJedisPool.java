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

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.helios.rindle.store.ConnectionPool;

import redis.clients.util.Pool;

/**
 * <p>Title: ExtendedJedisPool</p>
 * <p>Description: A Jedis pool extended for some additional functionailty</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.ExtendedJedisPool</code></p>
 */

public class ExtendedJedisPool extends Pool<ExtendedJedis> implements ConnectionPool {
	/** The commons pool pooling the jedis connections */
	protected final GenericObjectPool<ExtendedJedis> pool;
	
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
		this.pool = internalPool;
	}
	
	/**
	 * Returns the pool instrumentation
	 * @return the pool instrumentation
	 */
	public ConnectionPool getConnectionPool() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getNumActive()
	 */
	@Override
	public int getNumActive() {
		return pool.getNumActive();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getNumIdle()
	 */
	@Override
	public int getNumIdle() {
		return pool.getNumIdle();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getNumWaiters()
	 */
	@Override
	public int getNumWaiters() {
		return pool.getNumWaiters();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getBorrowedCount()
	 */
	@Override
	public long getBorrowedCount() {
		return pool.getBorrowedCount();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getCreatedCount()
	 */
	@Override
	public long getCreatedCount() {
		return pool.getCreatedCount();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getDestroyedCount()
	 */
	@Override
	public long getDestroyedCount() {
		return pool.getDestroyedCount();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getMaxBorrowWaitTime()
	 */
	@Override
	public long getMaxBorrowWaitTime() {
		return pool.getMaxBorrowWaitTimeMillis();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getMaxTotal()
	 */
	@Override
	public long getMaxTotal() {
		return pool.getMaxTotal();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getReturnedCount()
	 */
	@Override
	public long getReturnedCount() {
		return pool.getReturnedCount();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getMaxWait()
	 */
	@Override
	public long getMaxWait() {
		return pool.getMaxWaitMillis();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getMeanActiveTime()
	 */
	@Override
	public long getMeanActiveTime() {
		return pool.getMeanActiveTimeMillis();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.ConnectionPool#getMeanBorrowWaitTime()
	 */
	@Override
	public long getMeanBorrowWaitTime() {
		return pool.getMeanBorrowWaitTimeMillis();
	}

	
	
}
