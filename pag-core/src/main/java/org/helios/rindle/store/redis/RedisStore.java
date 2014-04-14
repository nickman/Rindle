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
import java.util.ArrayList;
import java.util.Collection;

import javax.management.MXBean;

import org.helios.rindle.AbstractRindleService;
import org.helios.rindle.RindleService;
import org.helios.rindle.control.RindleMain;
import org.helios.rindle.store.IStore;
import org.helios.rindle.store.redis.netty.EmptySubListener;
import org.helios.rindle.util.StringHelper;

/**
 * <p>Title: RedisStore</p>
 * <p>Description: Redis implementation of the Rindle {@link IStore}.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.RedisStore</code></p>
 */
@MXBean
public class RedisStore extends AbstractRindleService implements IStore {
	/** The redis connection pool */
	protected RedisConnectionPool connectionPool = new RedisConnectionPool();
	protected ScriptControl scriptControl = null;
	protected byte[] processNameOpaqueScriptSha = null;
	
	/** The default platform charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	
	/** A constant for a Null string as bytes */
	private static final byte[] NULL_BYTES = "NULL".getBytes(CHARSET);
	
	/**
	 * Creates a new RedisStore
	 */
	public RedisStore() {
		
	}
	
	/**
	 * Conserts a string to bytes for redis store
	 * @param s The string to convert
	 * @return the bytes
	 */
	public static byte[] strToBytes(String s) {
		return s==null ? NULL_BYTES : s.trim().getBytes(CHARSET);
	}
	
	/**
	 * Performs a null/zero-length byte check on the passed byte array
	 * @param b The byte array to check
	 * @return the passed byte array or the NULL_BYTES constant if the array was null or zero-length
	 */
	public static byte[] nvl(byte[] b) {
		return (b==null || b.length==0) ? NULL_BYTES : b;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.IStore#getGlobalId(java.lang.String, byte[])
	 */
	@Override
	public long[] getGlobalId(final String name, final byte[] opaqueKey) {
		return connectionPool.redisTask(new RedisTask<long[]>() {
			@Override
			public long[] redisTask(ExtendedJedis jedis) throws Exception {			
				Object result = scriptControl.invokeScript(jedis, processNameOpaqueScriptSha, 2, strToBytes(name), nvl(opaqueKey));
				if(result==null) return new long[]{-1L};
				if(result instanceof Long) return new long[]{(Long)result};
				if(result instanceof byte[]) return new long[]{Long.parseLong(new String((byte[])result))};
				throw new Exception("Unrecognized type: [" + result.getClass().getName() + "]");
			}
		});
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.IStore#purge()
	 */
	@Override
	public void purge() {
		connectionPool.redisTask(new RedisTask<Void>() {
			@Override
			public Void redisTask(ExtendedJedis jedis) throws Exception {
				jedis.flushAll();
				return null;
			}
		});
		
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.IStore#getGlobalId(java.lang.String)
	 */
	@Override
	public long getGlobalId(String name) {
		return getGlobalId(name, null)[0];
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.IStore#getGlobalId(byte[])
	 */
	@Override
	public long getGlobalId(byte[] opaqueKey) {
		return getGlobalId(null, opaqueKey)[0];
	}
	
	/**
	 * <p>Starts the Redis Store Service</p>
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStart()
	 */
	@Override
	protected void doStart() {
		
		final RedisStore service = this;						
		connectionPool.addListener(new Listener() {
			@Override
			public void running() {
				
				scriptControl  = connectionPool.getScriptControl();
				
				scriptControl.addListener(new Listener(){
					/**
					 * {@inheritDoc}
					 * @see com.google.common.util.concurrent.Service.Listener#running()
					 */
					@Override
					public void running() {
						processNameOpaqueScriptSha = scriptControl.getScriptSha("processNameOpaque.lua");
						log.info("processNameOpaqueScriptSha: [{}]", processNameOpaqueScriptSha);
						service.notifyStarted();
						super.running();
					}
				}, RindleMain.getInstance().getThreadPool());
				if(scriptControl.isRunning()) {
					processNameOpaqueScriptSha = scriptControl.getScriptSha("processNameOpaque.lua");
					log.info("processNameOpaqueScriptSha: [{}]", processNameOpaqueScriptSha);
					service.notifyStarted();
				}
//				connectionPool.pubSub.subscribe("RINDLELOG:" + connectionPool.pubSub.getClientInfo().getName());
//				connectionPool.pubSub.subscribe("RINDLELOG");
				connectionPool.pubSub.psubscribe("RINDLE*");
				
				connectionPool.pubSub.registerListener(new EmptySubListener() {
					@Override
					public void onChannelMessage(String channel, String message) {
						log.info("[{}]:{}", channel, message);
					}
					@Override
					public void onPatternMessage(String pattern, String channel, String message) {
						log.info("[{}/@/{}]:{}", pattern, channel, message);
					}
				});
				connectionPool.redisTask(new RedisTask<Void>() {
					@Override
					public Void redisTask(ExtendedJedis jedis) throws Exception {
						
						return null;
					}
				});
			}

			@Override
			public void starting() {
			}

			@Override
			public void stopping(State from) {
				log.info(StringHelper.banner("RedisConnectionPool Stopping from %s", from));
			}

			@Override
			public void terminated(State from) {
				log.info(StringHelper.banner("RedisConnectionPool Terminated from %s", from));				
			}

			@Override
			public void failed(State from, Throwable failure) {
				log.error("Failed from {}", from, failure);				
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
//		connectionPool.stop().addListener(new Runnable(){
//			public void run() {
//				log.info(StringHelper.banner("RedisConnectionPool Shut Down"));
//			}
//		}, RindleMain.getInstance().getThreadPool());		
	}

	@Override
	public Collection<RindleService> getDependentServices() {
		Collection<RindleService> services = new ArrayList<RindleService>();
		services.add(connectionPool);
		return services;
	}

}
