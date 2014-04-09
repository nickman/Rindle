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
package org.helios.pag.store.redis;

import redis.clients.jedis.BinaryJedis;
import redis.clients.util.Pool;

/**
 * <p>Title: ExtendedJedis</p>
 * <p>Description: An extended jedis implementation with a few addition un-thread-safe useful ops and a built in pool closer.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.ExtendedJedis</code></p>
 */

public class ExtendedJedis extends BinaryJedis {
	/** The pool is connection was created for */
	protected final Pool<ExtendedJedis> pool;
	
	/**
	 * Creates a new ExtendedJedis
	 * @param host The redis host name or ip address
	 * @param port The redis listening port
	 * @param timeout The redis connection timeout in s.
	 * @param pool The pool is connection was created for
	 */
	public ExtendedJedis(String host, int port, int timeout, Pool<ExtendedJedis> pool) {
		super(host, port, timeout);
		this.pool = pool;
	}
	
	/**
	 * <p>Returns this connection to the pool.</p>
	 * {@inheritDoc}
	 * @see redis.clients.jedis.BinaryJedis#close()
	 */
	public void close() {
		if(pool==null) {
			realClose();
		} else {
			pool.returnResource(this);
		}
	}
	
	/**
	 * Really closes the connection
	 */
	void realClose() {
    	if (isConnected()) {
    		try {
    			try {
    				quit();
    			} catch (Exception e) {/* No Op */}
    			disconnect();
    		} catch (Exception e)  {/* No Op */}
    	}
	}

}
