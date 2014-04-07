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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.pag.control.RindleMain;
import org.helios.pag.store.IStore;
import org.helios.pag.util.StringHelper;

import com.google.common.util.concurrent.AbstractService;

/**
 * <p>Title: RedisStore</p>
 * <p>Description: Redis implementation of the Rindle {@link IStore}.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.RedisStore</code></p>
 */

public class RedisStore extends AbstractService implements IStore {
	/** The redis connection pool */
	protected RedisConnectionPool rcp = null;
	/** Instance logger */
	protected Logger log = LogManager.getLogger(getClass());
	
	
	/**
	 * Creates a new RedisStore
	 */
	public RedisStore() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * <p>Starts the Redis Store Service</p>
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStart()
	 */
	@Override
	protected void doStart() {
		rcp = new RedisConnectionPool();
		rcp.start().addListener(new Runnable(){
			public void run() {
				log.info(StringHelper.banner("RedisConnectionPool Started"));
			}
		}, RindleMain.getInstance().getThreadPool());
		
	}


	/**
	 * <p>Stops the Redis Store Service</p>
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStop()
	 */
	@Override
	protected void doStop() {
		rcp.stop().addListener(new Runnable(){
			public void run() {
				log.info(StringHelper.banner("RedisConnectionPool Shut Down"));
			}
		}, RindleMain.getInstance().getThreadPool());		
	}

}
