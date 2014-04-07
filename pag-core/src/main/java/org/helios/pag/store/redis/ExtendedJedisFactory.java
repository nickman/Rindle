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

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;


/**
 * <p>Title: ExtendedJedisFactory</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.ExtendedJedisFactory</code></p>
 */

public class ExtendedJedisFactory implements PooledObjectFactory<Jedis> {
    private final String host;
    private final int port;
    private final int timeout;
    private final String password;
    private final int database;
    private final String clientName;
	
    /**
     * Creates a new ExtendedJedisFactory
     * @param host
     * @param port
     * @param timeout
     * @param password
     * @param database
     * @param clientName
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
	

    @Override
    public void activateObject(PooledObject<Jedis> pooledJedis)
	    throws Exception {
	final BinaryJedis jedis = pooledJedis.getObject();
	if (jedis.getDB() != database) {
	    jedis.select(database);
	}

    }

    @Override
    public void destroyObject(PooledObject<Jedis> pooledJedis) throws Exception {
	final BinaryJedis jedis = pooledJedis.getObject();
	if (jedis.isConnected()) {
	    try {
		try {
		    jedis.quit();
		} catch (Exception e) {
		}
		jedis.disconnect();
	    } catch (Exception e) {

	    }
	}

    }

    @Override
    public PooledObject<Jedis> makeObject() throws Exception {
	final Jedis jedis = new Jedis(this.host, this.port, this.timeout);

	jedis.connect();
	if (null != this.password) {
	    jedis.auth(this.password);
	}
	if (database != 0) {
	    jedis.select(database);
	}
	if (clientName != null) {
	    jedis.clientSetname(clientName);
	}

	return new DefaultPooledObject<Jedis>(jedis);
    }

    @Override
    public void passivateObject(PooledObject<Jedis> pooledJedis)
	    throws Exception {
	// TODO maybe should select db 0? Not sure right now.
    }

    @Override
    public boolean validateObject(PooledObject<Jedis> pooledJedis) {
	final BinaryJedis jedis = pooledJedis.getObject();
	try {
	    return jedis.isConnected() && jedis.ping().equals("PONG");
	} catch (final Exception e) {
	    return false;
	}
    }
}
