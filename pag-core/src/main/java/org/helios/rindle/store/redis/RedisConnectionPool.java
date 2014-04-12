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

import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_AUTH;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_BLOCK_WHEN_EXHAUSTED;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_CLIENT_NAME;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_DB;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_EVICTION_POLICY_CLASS_NAME;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_HOST;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_JMX_ENABLED;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_JMX_NAME_PREFIX;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_LIFO;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_MAX_IDLE;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_MAX_TOTAL;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_MAX_WAIT;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_MIN_EVICTABLE_IDLE_TIME;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_MIN_IDLE;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_NUM_TESTS_PER_EVICTION_RUN;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_POOL_MONITOR_PERIOD;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_PORT;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_SOFT_MIN_EVICTABLE_IDLE_TIME;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_TEST_ON_BORROW;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_TEST_ON_RETURN;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_TEST_WHILE_IDLE;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_TIMEOUT;
import static org.helios.rindle.store.redis.RedisConstants.DEFAULT_REDIS_TIME_BETWEEN_EVICTION_RUNS;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_AUTH_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_BLOCK_WHEN_EXHAUSTED_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_CLIENT_NAME_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_DB_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_EVICTION_POLICY_CLASS_NAME_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_HOST_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_JMX_ENABLED_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_JMX_NAME_PREFIX_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_LIFO_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_MAX_IDLE_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_MAX_TOTAL_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_MAX_WAIT_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_MIN_EVICTABLE_IDLE_TIME_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_MIN_IDLE_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_NUM_TESTS_PER_EVICTION_RUN_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_POOL_MONITOR_PERIOD_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_PORT_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_SOFT_MIN_EVICTABLE_IDLE_TIME_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_TEST_ON_BORROW_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_TEST_ON_RETURN_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_TEST_WHILE_IDLE_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_TIMEOUT_CONF;
import static org.helios.rindle.store.redis.RedisConstants.REDIS_TIME_BETWEEN_EVICTION_RUNS_CONF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.rindle.RindleService;
import org.helios.rindle.control.FlushScheduler;
import org.helios.rindle.control.IFlushPeriodListener;
import org.helios.rindle.control.RindleMain;
import org.helios.rindle.store.redis.netty.OptimizedPubSub;
import org.helios.rindle.util.ConfigurationHelper;
import org.helios.rindle.util.JMXHelper;
import org.helios.rindle.util.StringHelper;

import com.google.common.util.concurrent.AbstractService;

/**
 * <p>Title: RedisConnectionPool</p>
 * <p>Description: A pool of connections to a redis store</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.RedisConnectionPool</code></p>
 */

public class RedisConnectionPool extends AbstractService implements RindleService, IFlushPeriodListener, ExtendedJedisLifecycleListener {
	/** The redis connection pool configuration */
	protected final GenericObjectPoolConfig  poolConfig = new GenericObjectPoolConfig ();
	
	/** Instance logger */
	protected final Logger log = LogManager.getLogger(getClass());
	
	/** The redis host or ip address */
	protected final String redisHost;
	/** The redis listening port */
	protected final int redisPort;
	/** The redis default DB id */
	protected final int redisDb;
	/** The redis connection timeout in s. */
	protected final int timeout;
	/** The redis pool monitor period in s. */
	protected int monitorPeriod;
	
	/** The redis authentication */
	protected final String redisAuth;
	/** The redis client name */
	protected final String clientName;
	/** Pool connection monitor connection */
	protected ExtendedJedis monitorJedis = null;
	
	/** The jedis connection pool */
	protected ExtendedJedisPool pool = null;
	/** The pub/sub redis interface */
	protected OptimizedPubSub pubSub;
	/** The script controller */
	protected ScriptControl scriptControl = new ScriptControl(this);
	
	protected final Set<RindleService> deps = new HashSet<RindleService>(Arrays.asList(scriptControl)); 
	
	/** A map of this pool's connected extended jedis client info objects */
	protected final Map<String, ClientInfo> infos = new ConcurrentHashMap<String, ClientInfo>(); 
	
	/**
	 * Creates a new RedisConnectionPool
	 */
	public RedisConnectionPool() {
		redisHost = ConfigurationHelper.getSystemThenEnvProperty(REDIS_HOST_CONF, DEFAULT_REDIS_HOST);
		redisPort = ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_PORT_CONF, DEFAULT_REDIS_PORT);
		redisDb= ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_DB_CONF, DEFAULT_REDIS_DB);
		redisAuth = ConfigurationHelper.getSystemThenEnvProperty(REDIS_AUTH_CONF, DEFAULT_REDIS_AUTH);
		clientName = ConfigurationHelper.getSystemThenEnvProperty(REDIS_CLIENT_NAME_CONF, DEFAULT_REDIS_CLIENT_NAME);
		timeout = ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_TIMEOUT_CONF, DEFAULT_REDIS_TIMEOUT);
		monitorPeriod = ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_POOL_MONITOR_PERIOD_CONF, DEFAULT_REDIS_POOL_MONITOR_PERIOD);
		poolConfig.setMaxTotal(ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_MAX_TOTAL_CONF, DEFAULT_REDIS_MAX_TOTAL));
		poolConfig.setMinIdle(ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_MIN_IDLE_CONF, DEFAULT_REDIS_MIN_IDLE));
		poolConfig.setMaxIdle(ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_MAX_IDLE_CONF, DEFAULT_REDIS_MAX_IDLE));
		poolConfig.setTestWhileIdle(ConfigurationHelper.getBooleanSystemThenEnvProperty(REDIS_TEST_WHILE_IDLE_CONF, DEFAULT_REDIS_TEST_WHILE_IDLE));
		poolConfig.setTimeBetweenEvictionRunsMillis(ConfigurationHelper.getLongSystemThenEnvProperty(REDIS_TIME_BETWEEN_EVICTION_RUNS_CONF, DEFAULT_REDIS_TIME_BETWEEN_EVICTION_RUNS));
		poolConfig.setEvictionPolicyClassName(ConfigurationHelper.getSystemThenEnvProperty(REDIS_EVICTION_POLICY_CLASS_NAME_CONF, DEFAULT_REDIS_EVICTION_POLICY_CLASS_NAME));
		poolConfig.setBlockWhenExhausted(ConfigurationHelper.getBooleanSystemThenEnvProperty(REDIS_BLOCK_WHEN_EXHAUSTED_CONF, DEFAULT_REDIS_BLOCK_WHEN_EXHAUSTED));
		poolConfig.setJmxEnabled(ConfigurationHelper.getBooleanSystemThenEnvProperty(REDIS_JMX_ENABLED_CONF, DEFAULT_REDIS_JMX_ENABLED));
		poolConfig.setJmxNamePrefix(ConfigurationHelper.getSystemThenEnvProperty(REDIS_JMX_NAME_PREFIX_CONF, DEFAULT_REDIS_JMX_NAME_PREFIX));
		poolConfig.setLifo(ConfigurationHelper.getBooleanSystemThenEnvProperty(REDIS_LIFO_CONF, DEFAULT_REDIS_LIFO));
		poolConfig.setMaxWaitMillis(ConfigurationHelper.getLongSystemThenEnvProperty(REDIS_MAX_WAIT_CONF, DEFAULT_REDIS_MAX_WAIT));
		poolConfig.setMinEvictableIdleTimeMillis(ConfigurationHelper.getLongSystemThenEnvProperty(REDIS_MIN_EVICTABLE_IDLE_TIME_CONF, DEFAULT_REDIS_MIN_EVICTABLE_IDLE_TIME));
		poolConfig.setSoftMinEvictableIdleTimeMillis(ConfigurationHelper.getLongSystemThenEnvProperty(REDIS_SOFT_MIN_EVICTABLE_IDLE_TIME_CONF, DEFAULT_REDIS_SOFT_MIN_EVICTABLE_IDLE_TIME));
		poolConfig.setNumTestsPerEvictionRun(ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_NUM_TESTS_PER_EVICTION_RUN_CONF, DEFAULT_REDIS_NUM_TESTS_PER_EVICTION_RUN));
		poolConfig.setTestOnBorrow(ConfigurationHelper.getBooleanSystemThenEnvProperty(REDIS_TEST_ON_BORROW_CONF, DEFAULT_REDIS_TEST_ON_BORROW));
		poolConfig.setTestOnReturn(ConfigurationHelper.getBooleanSystemThenEnvProperty(REDIS_TEST_ON_RETURN_CONF, DEFAULT_REDIS_TEST_ON_RETURN));		
	}

	/**
	 * <p>Starts the pool</p>
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStart()
	 */
	@Override
	protected void doStart() {
		try {
			pool = new ExtendedJedisPool(poolConfig, redisHost, redisPort, timeout, redisAuth, redisDb, clientName, this);
			log.info(StringHelper.banner("Started Redis Connection Pool.\n\tHost:%s\n\tPort:%s\n\tTimeout:%s", redisHost, redisPort, timeout));
			pubSub = OptimizedPubSub.getInstance(redisHost, redisPort, redisAuth, timeout);
			monitorJedis = new ExtendedJedis(redisHost, redisPort, timeout, RedisConstants.DEFAULT_REDIS_CLIENT_NAME.replace("Rindle", "RindlePoolMonitor"), null);
			registerClientInfo(pubSub);
			pubSub.getClientInfo().update(RedisClientStat.extract(pubSub.getClientInfo().getName(), monitorJedis.clientList()));
			onConnect(monitorJedis);
			FlushScheduler.getInstance().registerListener(this);
			notifyStarted();
		} catch (Exception ex) {
			notifyFailed(ex);
		}
	}
	
	/**
	 * Returns a redis connection from the pool
	 * @return a redis connection from the pool
	 */
	public ExtendedJedis getJedis() {
		return pool.getResource();
	}
	
	/**
	 * Executes the passed task
	 * @param task The task to execute with a redis connection
	 * @return the return value of the task
	 */
	public <T> T redisTask(RedisTask<T> task) {
		ExtendedJedis jedis = null;
		try {
			jedis = pool.getResource();
			return task.redisTask(jedis);
		} catch (Exception ex) {
			log.error("Failed to execute redis task [{}]", task, ex);
			throw new RuntimeException("Failed to execute redis task", ex);
		} finally {
			if(jedis!=null) try { jedis.close(); } catch (Exception x) {/* No Op */}
		}				
	}

	/**
	 * <p>Stops and destroys the pool</p>
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStop()
	 */
	@Override
	protected void doStop() {
		if(pool!=null) {
			log.info("Stopping Redis Connection Pool....");
			pool.destroy();
			log.info("Redis Connection Pool Stopped");			
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.RindleService#getDependentServices()
	 */
	@Override
	public Collection<RindleService> getDependentServices() {
		return deps;
	}

	@Override
	public void onPeriodFlush(int period) {
		RindleMain.getInstance().getThreadPool().execute(new Runnable() {
			public void run() {
				List<ClientInfo> disconnected = new ArrayList<ClientInfo>();
				for(ClientInfo ci: infos.values()) {
					if(!ci.isConnected()) {
						disconnected.add(ci);
					}
				}
				for(ClientInfo ci: disconnected) {
					removeClientInfo(ci.getProvider());
				}
				String clientList = monitorJedis.clientList();
				
				Map<String, Map<RedisClientStat, Object>> statsMap = RedisClientStat.parseClientInfo(clientList);
				for(Map.Entry<String, Map<RedisClientStat, Object>> clientEntry: statsMap.entrySet()) {
					ClientInfo ci = infos.get(clientEntry.getKey());					
					if(ci!=null) {
						ci.update(clientEntry.getValue());
					}
				}				
			}
		});
	}

	@Override
	public int[] getPeriods() {		
		return new int[] {monitorPeriod};
	}

	@Override
	public void setAdjustedPeriods(int[] adjustedPeriods) {
		monitorPeriod = adjustedPeriods[0];		
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.ExtendedJedisLifecycleListener#onConnect(org.helios.rindle.store.redis.ExtendedJedis)
	 */
	@Override
	public void onConnect(ExtendedJedis jedis) {
		registerClientInfo(jedis);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.store.redis.ExtendedJedisLifecycleListener#onClose(org.helios.rindle.store.redis.ExtendedJedis)
	 */
	@Override
	public void onClose(ExtendedJedis jedis) {
		removeClientInfo(jedis);
	}
	
	/**
	 * Registers the passed client info provider
	 * @param cip the client info provider to register
	 */
	public void registerClientInfo(ClientInfoProvider cip) {
		if(cip!=null) {
			infos.put(cip.getClientInfo().getName(), cip.getClientInfo());
			ObjectName on = JMXHelper.objectName("org.helios.rindle:type=PooledConnection,name=" + ObjectName.quote(cip.getClientInfo().getName()));
			JMXHelper.registerMBean(cip.getClientInfo(), on);
		}
	}

	/**
	 * Unregisters the passed client info provider
	 * @param cip the client info provider to unregister
	 */
	public void removeClientInfo(ClientInfoProvider cip) {
		if(cip!=null) {
			infos.remove(cip.getClientInfo().getName());
			ObjectName on = JMXHelper.objectName("org.helios.rindle:type=PooledConnection,name=" + ObjectName.quote(cip.getClientInfo().getName()));
			JMXHelper.unregisterMBean(on);
		}
	}
	
}
