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
import java.util.List;
import java.util.Set;

import javax.management.MXBean;

import org.helios.rindle.AbstractRindleService;
import org.helios.rindle.RindleService;
import org.helios.rindle.control.RindleMain;
import org.helios.rindle.json.JSON;
import org.helios.rindle.metric.IMetricDefinition;
import org.helios.rindle.store.IStore;
import org.helios.rindle.store.redis.netty.EmptySubListener;
import org.helios.rindle.util.StringHelper;
import org.helios.rindle.util.unsafe.UnsafeAdapter;

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
	/** The redis script invoker */
	protected ScriptControl scriptControl = null;
	/** The SHA1 bytes for the process script */
	protected byte[] processNameOpaqueScriptSha = null;
	/** The SHA1 bytes for the get metrics script */
	protected byte[] getMetricDefsScriptSha = null;

	/** The default platform charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	
	/** The metric name key byte value*/
	private static final byte[] NAME_KEY = "n".getBytes(CHARSET);
	/** The metric opaque key byte value*/
	private static final byte[] OPAQUE_KEY = "o".getBytes(CHARSET);
	/** An empty byte array constant */
	public static final byte[] EMPTY_BYTE_ARR = {};
	/** An empty long array constant */
	public static final long[] EMPTY_LONG_ARR = {};
	 
	
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
				Object result = scriptControl.invokeScript(jedis, processNameOpaqueScriptSha, 2, strToBytes(name), nvl(opaqueKey), String.valueOf(System.currentTimeMillis()).getBytes(CHARSET));
//				if(result==null) return new long[]{-1L};
				if(result instanceof Long) return new long[]{(Long)result};
				if(result instanceof byte[]) return new long[]{Long.parseLong(new String((byte[])result))};
				if(result instanceof ArrayList) {
					ArrayList<?> results = (ArrayList<?>)result;
					if(results.isEmpty()) return new long[] {-1L};
					int size = results.size();
					long[] a = new long[size];
					for(int i = 0; i < size; i++) {
						a[i] = jedis.bytesToLong((byte[])results.get(i));
								//Long.parseLong(new String((byte[])results.get(i)));
					}
					return a;
//					log.info("Results: type:{} data:{}", results.iterator().next().getClass().getName(), results)  ; 
				}
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
				final int timeout = jedis.getSocketTimeoutMillis();
				try {
					jedis.setSocketTimeoutMillis(10000);
					jedis.flushAll();
					return null;
				} catch (Exception ex) {
					throw new RuntimeException("Flushall failed", ex);
				} finally {
					try { jedis.setSocketTimeoutMillis(timeout); } catch (Exception ex) {
						
					}
				}
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
					@Override
					public void running() {
						processNameOpaqueScriptSha = scriptControl.getScriptSha("processNameOpaque.lua");
						getMetricDefsScriptSha = scriptControl.getScriptSha("getMetricDefs.lua");
						service.notifyStarted();
						super.running();
					}
				}, RindleMain.getInstance().getThreadPool());
				if(scriptControl.isRunning()) {
					processNameOpaqueScriptSha = scriptControl.getScriptSha("processNameOpaque.lua");
					getMetricDefsScriptSha = scriptControl.getScriptSha("getMetricDefs.lua");
					service.notifyStarted();
				}
				connectionPool.pubSub.psubscribe("RINDLE.LOGGING.*");
				connectionPool.pubSub.subscribe("RINDLE.EVENT.METRIC.NEW");
				connectionPool.pubSub.subscribe("RINDLE.EVENT.METRIC.UPDATED");
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
	

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.IStore#getMetricName(long)
	 */
	@Override
	public String getMetricName(final long globalId) {
		return connectionPool.redisTask(new RedisTask<String>() {
			@Override
			public String redisTask(ExtendedJedis jedis) throws Exception {
				byte[] bytes = jedis.hget(jedis.longToBytes(globalId), NAME_KEY);
				if(bytes==null) return null;
				return new String(bytes, CHARSET);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.IStore#getOpaqueKey(long)
	 */
	@Override
	public byte[] getOpaqueKey(final long globalId) {
		return connectionPool.redisTask(new RedisTask<byte[]>() {
			@Override
			public byte[] redisTask(ExtendedJedis jedis) throws Exception {
				return jedis.hget(jedis.longToBytes(globalId), OPAQUE_KEY);
			}
		});
	}
	
	
	
	/**
	 * Returns the full metric definition JSON in bytes for the passed global ids
	 * @param globalIds The global ids to get metric definitions for
	 * @return A byte array of IMetricDefinitions JSON
	 */
	public byte[] getMetricsBytes(final long... globalIds) {
		if(globalIds==null || globalIds.length==0) return EMPTY_BYTE_ARR;
		return connectionPool.redisTask(new RedisTask<byte[]>() {
			@Override
			public byte[] redisTask(ExtendedJedis jedis) throws Exception {
				byte[][] globalIdBytes = new byte[globalIds.length][];
				for(int i = 0; i < globalIds.length; i++) {
					globalIdBytes[i] =  Long.toString(globalIds[i]).getBytes(CHARSET);
				}
				return (byte[]) jedis.evalsha(getMetricDefsScriptSha, globalIds.length, globalIdBytes);
			}
		});		
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.IStore#getMetricsJSON(long[])
	 */
	@Override
	public String getMetricsJSON(long... globalIds) {
		return new String(getMetricsBytes(globalIds), CHARSET);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.IStore#getMetrics(long[])
	 */
	@Override
	public IMetricDefinition[] getMetrics(long... globalIds) {	
		try {
			return JSON.MAP.readValue(getMetricsBytes(globalIds), IMetricDefinition[].class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get metric definitions", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.IStore#getGlobalIds(java.lang.String)
	 */
	@Override
	public long[] getGlobalIds(final String metricNamePattern) {
		if(metricNamePattern==null || metricNamePattern.trim().isEmpty()) return EMPTY_LONG_ARR;		
		List<byte[]> byteKeys =  connectionPool.redisTask(new RedisTask<List<byte[]>>() {
			@Override
			public List<byte[]> redisTask(ExtendedJedis jedis) throws Exception {
				Set<byte[]> keys = jedis.keys(strToBytes(metricNamePattern.trim()));
				return jedis.mget(keys.toArray(new byte[keys.size()][]));
			}
		});		
		if(byteKeys.isEmpty()) return EMPTY_LONG_ARR;
		int size = byteKeys.size();
		long[] keys = new long[size];
		int i = 0;
		for(byte[] key: byteKeys) {
			keys[i] = Long.parseLong(new String(key));
		}
		return keys;
	}
	
	
	/**
	 * Converts an 8 byte array to a long
	 * @param bytes The 8 byte array
	 * @return the converted long
	 */
	protected static long byteArrToLong(byte[] bytes) {
		if(bytes==null || bytes.length != 8) throw new IllegalArgumentException("byteArrToLong requires an 8 byte array but was [" + (bytes==null ? "null" : ("" + bytes.length + ":(" + new String(bytes) + ")")) + "]");
		long address = -1;
		try {
			address = UnsafeAdapter.allocateMemory(8);
			UnsafeAdapter.copyMemory(bytes, UnsafeAdapter.BYTE_ARRAY_OFFSET, null, 0, 8);
			return UnsafeAdapter.getLong(address);
		} finally {
			UnsafeAdapter.freeMemory(address);
		}
	}

}
