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

import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_AUTH;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_BLOCK_WHEN_EXHAUSTED;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_CLIENT_NAME;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_DB;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_EVICTION_POLICY_CLASS_NAME;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_HOST;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_JMX_ENABLED;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_JMX_NAME_PREFIX;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_LIFO;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_MAX_IDLE;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_MAX_TOTAL;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_MAX_WAIT;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_MIN_EVICTABLE_IDLE_TIME;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_MIN_IDLE;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_NUM_TESTS_PER_EVICTION_RUN;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_PORT;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_SOFT_MIN_EVICTABLE_IDLE_TIME;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_TEST_ON_BORROW;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_TEST_ON_RETURN;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_TEST_WHILE_IDLE;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_TIMEOUT;
import static org.helios.pag.store.redis.RedisConstants.DEFAULT_REDIS_TIME_BETWEEN_EVICTION_RUNS;
import static org.helios.pag.store.redis.RedisConstants.REDIS_AUTH_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_BLOCK_WHEN_EXHAUSTED_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_CLIENT_NAME_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_DB_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_EVICTION_POLICY_CLASS_NAME_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_HOST_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_JMX_ENABLED_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_JMX_NAME_PREFIX_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_LIFO_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_MAX_IDLE_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_MAX_TOTAL_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_MAX_WAIT_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_MIN_EVICTABLE_IDLE_TIME_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_MIN_IDLE_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_NUM_TESTS_PER_EVICTION_RUN_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_PORT_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_SOFT_MIN_EVICTABLE_IDLE_TIME_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_TEST_ON_BORROW_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_TEST_ON_RETURN_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_TEST_WHILE_IDLE_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_TIMEOUT_CONF;
import static org.helios.pag.store.redis.RedisConstants.REDIS_TIME_BETWEEN_EVICTION_RUNS_CONF;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.pag.RindleService;
import org.helios.pag.util.ConfigurationHelper;
import org.helios.pag.util.StringHelper;

import com.google.common.util.concurrent.AbstractService;

/**
 * <p>Title: RedisConnectionPool</p>
 * <p>Description: A pool of connections to a redis store</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.RedisConnectionPool</code></p>
 */

public class RedisConnectionPool extends AbstractService implements RindleService {
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
	
	/** The redis authentication */
	protected final String redisAuth;
	/** The redis client name */
	protected final String clientName;
	
	/** The jedis connection pool */
	protected ExtendedJedisPool pool = null;

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
			pool = new ExtendedJedisPool(poolConfig, redisHost, redisPort, timeout, redisAuth, redisDb, clientName);
			log.info(StringHelper.banner("Started Redis Connection Pool.\n\tHost:%s\n\tPort:%s\n\tTimeout:%s", redisHost, redisPort, timeout));
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
	 * @see org.helios.pag.RindleService#getDependentServices()
	 */
	@Override
	public Collection<RindleService> getDependentServices() {
		return Collections.emptyList();
	}

	
}
