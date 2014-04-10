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

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import redis.clients.jedis.BinaryJedis;
import redis.clients.util.Pool;


/**
 * <p>Title: ExtendedJedisFactory</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.ExtendedJedisFactory</code></p>
 */

public class ExtendedJedisFactory implements PooledObjectFactory<ExtendedJedis> {
    /** The redis host */
    private final String host;
    /** The redis port */
    private final int port;
    /** The redis connection timeout */
    private final int timeout;
    /** The redis password */
    private final String password;
    /** The redis database id to default to */
    private final int database;
    /** The redis client name */
    private final String clientName;
    /** The pool this factory is creating connections for */
    private Pool<ExtendedJedis> pool;
	/** The connection serial number factory */
    private final AtomicLong connectionSerial = new AtomicLong();
    /**
     * Creates a new ExtendedJedisFactory
	 * @param host The redis host or ip address
	 * @param port The redis listening port
	 * @param timeout The redis connection timeout in s.
	 * @param password The redis password
	 * @param database The redis DB to default to
	 * @param clientName The client name
     */
    public ExtendedJedisFactory(final String host, final int port, final int timeout,
    	    final String password, final int database, final String clientName) {
    	super();
    	this.host = host;
    	this.port = port;
    	this.timeout = timeout;
    	this.password = password;
    	this.database = database;
    	this.clientName = clientName;
     }
    
    /**
     * Sets the pool this factory is creating connections for
     * @param pool the factory pool
     */
    void setPool(Pool<ExtendedJedis> pool) {
    	this.pool = pool;
    }
	

    /**
     * {@inheritDoc}
     * @see org.apache.commons.pool2.PooledObjectFactory#activateObject(org.apache.commons.pool2.PooledObject)
     */
    @SuppressWarnings("resource")
	@Override
    public void activateObject(PooledObject<ExtendedJedis> pooledJedis)
    		throws Exception {
    	final BinaryJedis jedis = pooledJedis.getObject();
    	if (jedis.getDB() != database) {
    		jedis.select(database);
    	}

    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.pool2.PooledObjectFactory#destroyObject(org.apache.commons.pool2.PooledObject)
     */
    @SuppressWarnings("resource")
	@Override
    public void destroyObject(PooledObject<ExtendedJedis> pooledJedis) throws Exception {
    	final ExtendedJedis jedis = pooledJedis.getObject();
    	if(jedis!=null) {
    		jedis.realClose();
    	}

    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.pool2.PooledObjectFactory#makeObject()
     */

	@Override
    public PooledObject<ExtendedJedis> makeObject() throws Exception {
    	final ExtendedJedis jedis = new ExtendedJedis(this.host, this.port, this.timeout, pool);

    	jedis.connect();
    	if (null != this.password) {
    		jedis.auth(this.password);
    	}
    	if (database != 0) {
    		jedis.select(database);
    	}
   		jedis.clientSetname((clientName + "#" + connectionSerial.incrementAndGet()).getBytes());

    	return new DefaultPooledObject<ExtendedJedis>(jedis);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.pool2.PooledObjectFactory#passivateObject(org.apache.commons.pool2.PooledObject)
     */
    @Override
    public void passivateObject(PooledObject<ExtendedJedis> pooledJedis) throws Exception {
    	// TODO maybe should select db 0? Not sure right now.
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.pool2.PooledObjectFactory#validateObject(org.apache.commons.pool2.PooledObject)
     */
    @SuppressWarnings("resource")
	@Override
    public boolean validateObject(PooledObject<ExtendedJedis> pooledJedis) {
    	final BinaryJedis jedis = pooledJedis.getObject();
    	try {
    		return jedis.isConnected() && jedis.ping().equals("PONG");
    	} catch (final Exception e) {
    		return false;
    	}
    }
}
