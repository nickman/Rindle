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

import java.lang.management.ManagementFactory;

/**
 * <p>Title: RedisConstants</p>
 * <p>Description: Redis store configuration constants</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.RedisConstants</code></p>
 */

public class RedisConstants {
	/** The configuration property name for the Redis host name or IP address  */
	public static final String REDIS_HOST_CONF = "helios.rindle.redis.host";
	/** The default Redis host name or IP address  */
	public static final String DEFAULT_REDIS_HOST = "localhost"; // 10.12.114.48
	
	/** The configuration property name for the Redis listening port */
	public static final String REDIS_PORT_CONF = "helios.rindle.redis.port";
	/** The default Redis listening port  */
	public static final int DEFAULT_REDIS_PORT = 6379;
	
	/** The configuration property name for the Redis authentication */
	public static final String REDIS_AUTH_CONF = "helios.rindle.redis.auth";
	/** The default Redis authentication  */
	public static final String DEFAULT_REDIS_AUTH = null;
	
	/** The configuration property name for the Redis connection pool's default database */
	public static final String REDIS_DB_CONF = "helios.rindle.redis.database";
	/** The default configuration for the Redis connection pool's default database */
	public static final int DEFAULT_REDIS_DB = 1;
	
	/** The JVM runtime name */
	public static final String RUNTIME_NAME = ManagementFactory.getRuntimeMXBean().getName();
	
	/** The configuration property name for the Redis connection client name */
	public static final String REDIS_CLIENT_NAME_CONF = "helios.rindle.redis.clientname";
	/** The default Redis connection client name  */
	public static final String DEFAULT_REDIS_CLIENT_NAME = String.format("Rindle:%s", RUNTIME_NAME);

	/** The configuration property name for the Redis connection pool's connection timeout in s. */
	public static final String REDIS_TIMEOUT_CONF = "helios.rindle.redis.timeout";
	/** The default configuration for the Redis connection pool's connection timeout in s. */
	public static final int DEFAULT_REDIS_TIMEOUT = 15;
	
	/** The configuration property name for the Redis connection pool's monitoring period in sec. */
	public static final String REDIS_POOL_MONITOR_PERIOD_CONF = "helios.rindle.redis.pool.monitor.period";
	/** The default Redis connection pool's monitoring period in sec. */
	public static final int DEFAULT_REDIS_POOL_MONITOR_PERIOD = 5;
	
	//====================================================================================================	
	
	/** The configuration property name for the Redis maximum total number of connections per pool  */
	public static final String REDIS_MAX_TOTAL_CONF = "helios.rindle.redis.maxtotal";
	/** The default configuration for the Redis maximum total number of connections per connection pool  */
	public static final int DEFAULT_REDIS_MAX_TOTAL = 5;

	/** The configuration property name for the Redis minimum idle number of connections per pool  */
	public static final String REDIS_MIN_IDLE_CONF = "helios.rindle.redis.minidle";
	/** The default configuration for the Redis minimum idle number of connections per pool  */
	public static final int DEFAULT_REDIS_MIN_IDLE = 2;

	/** The configuration property name for the Redis maximum idle number of connections per pool  */
	public static final String REDIS_MAX_IDLE_CONF = "helios.rindle.redis.maxidle";
	/** The default configuration for the Redis maximum idle number of connections per pool  */
	public static final int DEFAULT_REDIS_MAX_IDLE = 3;
	
	//====================================================================================================

	/** The configuration property name for the Redis pool test while idle */
	public static final String REDIS_TEST_WHILE_IDLE_CONF = "helios.rindle.redis.testwhileidle";
	/** The default configuration for the Redis pool test while idle */
	public static final boolean DEFAULT_REDIS_TEST_WHILE_IDLE = true;

	/** The configuration property name for the Redis connection pool time between connection eviction runs  */
	public static final String REDIS_TIME_BETWEEN_EVICTION_RUNS_CONF = "helios.rindle.redis.timebetweenevictionruns";
	/** The default configuration for the Redis connection pool time between connection eviction runs in ms. */
	public static final long DEFAULT_REDIS_TIME_BETWEEN_EVICTION_RUNS = 10000;

	/** The configuration property name for the Redis connection pool eviction policy class name */
	public static final String REDIS_EVICTION_POLICY_CLASS_NAME_CONF = "helios.rindle.redis.evictionpolicyclassname";
	/** The default configuration for the Redis connection pool eviction policy class name */
	public static final String DEFAULT_REDIS_EVICTION_POLICY_CLASS_NAME = "org.apache.commons.pool2.impl.DefaultEvictionPolicy";

	/** The configuration property name for the Redis connection pool block when exhausted option */
	public static final String REDIS_BLOCK_WHEN_EXHAUSTED_CONF = "helios.rindle.redis.blockwhenexhausted";
	/** The default configuration for the Redis connection pool block when exhausted option */
	public static final boolean DEFAULT_REDIS_BLOCK_WHEN_EXHAUSTED = true;

	/** The configuration property name for the Redis connection pool JMX enablement */
	public static final String REDIS_JMX_ENABLED_CONF = "helios.rindle.redis.jmxenabled";
	/** The default configuration for the Redis connection pool JMX enablement */
	public static final boolean DEFAULT_REDIS_JMX_ENABLED = true;

	/** The configuration property name for the Redis connection pool JMX name prefix  */
	public static final String REDIS_JMX_NAME_PREFIX_CONF = "helios.rindle.redis.jmxnameprefix";
	/** The default configuration for the Redis connection pool JMX name prefix */
	public static final String DEFAULT_REDIS_JMX_NAME_PREFIX = "redisPool";

	/** The configuration property name for the Redis connection pool LIFO option */
	public static final String REDIS_LIFO_CONF = "helios.rindle.redis.lifo";
	/** The default configuration for the Redis connection pool LIFO option */
	public static final boolean DEFAULT_REDIS_LIFO = true;

	/** The configuration property name for the Redis connection pool maximum wait time in ms. */
	public static final String REDIS_MAX_WAIT_CONF = "helios.rindle.redis.maxwait";
	/** The default configuration for the Redis connection pool maximum wait time in ms. */
	public static final long DEFAULT_REDIS_MAX_WAIT = 100000;

	/** The configuration property name for the Redis connection pool minimum idle connection eviction time in ms. */
	public static final String REDIS_MIN_EVICTABLE_IDLE_TIME_CONF = "helios.rindle.redis.minevictableidletime";
	/** The default configuration for the Redis connection pool minimum idle connection eviction time in ms. */
	public static final long DEFAULT_REDIS_MIN_EVICTABLE_IDLE_TIME = 60000 * 5;

	/** The configuration property name for the Redis connection pool minimum soft idle connection eviction time in ms. */
	public static final String REDIS_SOFT_MIN_EVICTABLE_IDLE_TIME_CONF = "helios.rindle.redis.softminevictableidletime";
	/** The default configuration for the Redis connection pool minimum soft idle connection eviction time in ms. */
	public static final long DEFAULT_REDIS_SOFT_MIN_EVICTABLE_IDLE_TIME = 60000 * 3;

	/** The configuration property name for the Redis connection pool number of tests per eviction run */
	public static final String REDIS_NUM_TESTS_PER_EVICTION_RUN_CONF = "helios.rindle.redis.numtestsperevictionrun";
	/** The default configuration for the Redis connection pool number of tests per eviction run */
	public static final int DEFAULT_REDIS_NUM_TESTS_PER_EVICTION_RUN = 3;

	/** The configuration property name for the Redis connection pool test on borrow policy */
	public static final String REDIS_TEST_ON_BORROW_CONF = "helios.rindle.redis.testonborrow";
	/** The default configuration for the Redis connection pool test on borrow policy */
	public static final boolean DEFAULT_REDIS_TEST_ON_BORROW = true;

	/** The configuration property name for the Redis connection pool test on return policy */
	public static final String REDIS_TEST_ON_RETURN_CONF = "helios.rindle.redis.testonreturn";
	/** The default configuration for the Redis connection pool test on return policy */
	public static final boolean DEFAULT_REDIS_TEST_ON_RETURN = true;

	
	/**
	 * Creates a new RedisConstants
	 */
	private RedisConstants() {
	}

}
