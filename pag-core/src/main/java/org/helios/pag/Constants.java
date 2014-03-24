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
package org.helios.pag;

import java.lang.management.ManagementFactory;

import org.helios.pag.util.unsafe.UnsafeAdapter;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.stumbleupon.async.Deferred;

/**
 * <p>Title: Constants</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.Constants</code></p>
 */

public class Constants {
	/** Config property name for unsafe mem tracking */
	public static final String TRACK_MEM_PROP = "org.helios.pag.umem.trackmem";
	/** Config property name for aligned memory allocation */
	public static final String ALIGN_MEM_PROP = "org.helios.pag.umem.align";
	
	/** The default unsafe mem tracking */
	public static final boolean DEFAULT_TRACK_MEM = false;
	/** The default aligned memory allocation */
	public static final boolean DEFAULT_ALIGN_MEM = false;
	
	/** The number of processors available to the JVM */
	public static final int CORES = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	
	/** A null deferred response const */
	public static final Deferred<Object> NULL_DEFERED = Deferred.fromResult(null);

	// ===========================================================================================	
	//		Registry Config
	// ===========================================================================================
	/** The config property name for the registry period map initial size. If the provided value
	 is not a power of 2, the next highest power of 2 will be used */
	public static final String REG_INIT_SIZE = "helios.pag.reg.initialsize";
	/** The default value for the registry period map initial size */
	public static final int DEFAULT_REG_INIT_SIZE = 128;
	/** The config property name for the space-for-speed setting on the registry map, where true will use more space for more speed */
	public static final String REG_SPACE_FOR_SPEED = "helios.pag.reg.space4speed";
	/** The default value for the registry period map space-for-speed setting on the registry map */
	public static final boolean DEFAULT_REG_SPACE_FOR_SPEED = true;
	
	// ===========================================================================================	
	//		Period Config
	// ===========================================================================================
	/** The config property name for the minimum granularity for requested periods */
	public static final String PERIOD_MIN_GRANULARITY = "helios.pag.period.granularity";
	/** The default minimum granularity for requested periods */
	public static final int DEFAULT_PERIOD_MIN_GRANULARITY = 5;
	/** The config property name for the maximum period */
	public static final String PERIOD_MAX = "helios.pag.period.max";
	/** The default maximum period */
	public static final int DEFAULT_PERIOD_MAX = 300;

	// ===========================================================================================	
	//		Raw Container Config
	// ===========================================================================================
	/** The config property name for the maximum number of slots that can be allocated before we start rolling out old values */
	public static final String MAX_SLOTS_ALLOC = "helios.pag.raw.max";
	/** The default maximum number of slots that can be allocated before we start rolling out old values */
	public static final int DEFAULT_MAX_SLOTS_ALLOC = 1048576;
	/** The config property name for the initial number of slots to be allocated in a new container */
	public static final String INIT_SLOTS_ALLOC = "helios.pag.raw.init";
	/** The default initial number of slots to be allocated in a new container */
	public static final int DEFAULT_INIT_SLOTS_ALLOC = 128;
	/** The config property name for the number of slots to be added when a container resizes */
	public static final String RESIZE_SLOTS_ALLOC = "helios.pag.raw.resize";
	/** The default initial number of slots to be added when a container resizes */
	public static final int DEFAULT_RESIZE_SLOTS_ALLOC = 128;
	
	
	
	
	
	// ===========================================================================================	
	//		Event Handler Executor Config
	// ===========================================================================================
	/** The config property name for the async dispatcher core pool size */
	public static final String ASYNC_CORE_SIZE = "helios.events.async.core";
	/** The default async dispatcher core pool size */
	public static final int DEFAULT_ASYNC_CORE_SIZE = Constants.CORES;
	/** The config property name for the async dispatcher max pool size */
	public static final String ASYNC_MAX_SIZE = "helios.events.async.max";
	/** The default async dispatcher max pool size */
	public static final int DEFAULT_ASYNC_MAX_SIZE = Constants.CORES * 2;
	/** The config property name for the async dispatcher keep alive time in ms. */
	public static final String ASYNC_KEEPALIVE_TIME = "helios.events.async.keepalive";
	/** The default async dispatcher keep alive time in ms. */
	public static final long DEFAULT_ASYNC_KEEPALIVE_TIME = 60000;
	/** The config property name for the async dispatcher work queue size */
	public static final String ASYNC_QUEUE_SIZE = "helios.events.async.queuesize";
	/** The default async dispatcher work queue size */
	public static final int DEFAULT_ASYNC_QUEUE_SIZE = 1024;
	
	/** The config property name for the thread pool name */
	public static final String ASYNC_EXECUTOR_NAME = "helios.events.async.name";
	/** The default async dispatcher work queue size */
	public static final String DEFAULT_ASYNC_EXECUTOR_NAME = "TSDBPluginAsyncDispatcher";
	// ===========================================================================================	
	//		Disruptor AsyncHandler Config
	// ===========================================================================================
	/** The config property name for the number of slots in the ring buffer. Must be a power of 2 */
	public static final String RING_BUFFER_SIZE = "helios.events.async.disruptor.ringsize";
	/** The default number of slots in the ring buffer */
	public static final int DEFAULT_RING_BUFFER_SIZE = 1024;
	
	/** The config property name for the ring buffer wait strategy */
	public static final String RING_BUFFER_WAIT_STRAT = "helios.events.async.disruptor.waitstrat";
	/** The default number of slots in the ring buffer */
	public static final String DEFAULT_RING_BUFFER_WAIT_STRAT = SleepingWaitStrategy.class.getSimpleName();
	/** The config property name for the ring buffer wait strategy ctor parameters */
	public static final String RING_BUFFER_WAIT_STRAT_ARGS = "helios.events.async.disruptor.waitstrat.args";
	/** The optional wait strategy class ctor parameters */
	public static final String DEFAULT_RING_BUFFER_WAIT_STRAT_ARGS = "";

	// ===========================================================================================	
	//		Netty Unified Protocol Server Config
	// ===========================================================================================
	/** The config property name for the port number the Netty listener will listen on. -1 will disable. */
	public static final String NETTY_REMOTING_PORT = "helios.netty.listen.port";
	/** The default JMXMP listener port */
	public static final int DEFAULT_NETTY_REMOTING_PORT = 5252;
	/** The config property name for the interface the Netty service listener will bind to */
	public static final String NETTY_REMOTING_INTERFACE = "helios.netty.listen.iface";
	/** The default JMXMP listener port */
	public static final String DEFAULT_NETTY_REMOTING_INTERFACE = "0.0.0.0";
	
	
	// ===========================================================================================	
	//		JMXMP JMXConnectorServer Config
	// ===========================================================================================
	/** The config property name for the port number the JMXMP listener will listen on. -1 will disable. */
	public static final String JMXMP_PORT = "helios.jmxmp.listen.port";
	/** The default JMXMP listener port */
	public static final int DEFAULT_JMXMP_PORT = 5255;
	/** The config property name for the interface the JMXMP listener will bind to */
	public static final String JMXMP_INTERFACE = "helios.jmxmp.listen.iface";
	/** The default JMXMP listener port */
	public static final String DEFAULT_JMXMP_INTERFACE = "0.0.0.0";
	
	
	private Constants() {
	}


}
