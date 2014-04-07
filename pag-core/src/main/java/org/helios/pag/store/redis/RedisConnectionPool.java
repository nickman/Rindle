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

import org.helios.pag.util.ConfigurationHelper;
import static org.helios.pag.store.redis.RedisConstants.*;
import redis.clients.jedis.JedisPoolConfig;

/**
 * <p>Title: RedisConnectionPool</p>
 * <p>Description: A pool of connections to a redis store</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.redis.RedisConnectionPool</code></p>
 */

public class RedisConnectionPool {
	/** The redis connection pool configuration */
	protected final JedisPoolConfig poolConfig = new JedisPoolConfig();
	
	/** The redis host or ip address */
	protected final String redisHost;
	/** The redis listening port */
	protected final int redisPort;
	/** The redis default DB id */
	protected final int redisDb;
	/** The redis authentication */
	protected final String redisAuth;
	/** The redis client name */
	protected final String clientName;

	/**
	 * Creates a new RedisConnectionPool
	 */
	public RedisConnectionPool() {
		redisHost = ConfigurationHelper.getSystemThenEnvProperty(REDIS_HOST_CONF, DEFAULT_REDIS_HOST);
		redisPort = ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_PORT_CONF, DEFAULT_REDIS_PORT);
		redisDb= ConfigurationHelper.getIntSystemThenEnvProperty(REDIS_DB_CONF, DEFAULT_REDIS_DB);
		redisAuth = ConfigurationHelper.getSystemThenEnvProperty(REDIS_AUTH_CONF, DEFAULT_REDIS_AUTH);
		clientName = ConfigurationHelper.getSystemThenEnvProperty(REDIS_CLIENT_NAME_CONF, DEFAULT_REDIS_CLIENT_NAME);
		
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

}
