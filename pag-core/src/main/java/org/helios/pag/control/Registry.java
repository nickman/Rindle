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
package org.helios.pag.control;

import javax.management.ObjectName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.helios.pag.Constants;
import org.helios.pag.period.IPeriodAggregator;
import org.helios.pag.util.ConfigurationHelper;
import org.helios.pag.util.JMXHelper;
import org.helios.pag.util.StringHelper;
import org.helios.pag.util.unsafe.UnsafeAdapter;

import ch.qos.logback.classic.BasicConfigurator;

/**
 * <p>Title: Registry</p>
 * <p>Description: The main registry for {@link IPeriodAggregator}s</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.control.Registry</code></p>
 */

public class Registry implements RegistryMXBean {
	/** The singleton instance */
	private static volatile Registry instance = null;
	/** The singleton instance ctor lock */
	private static final Object lock = new Object();
	/** The timestamp of the start of the current period */
	protected long startTime;
	/** The timestamp of the end of the current period */
	protected long endTime;
	/** Instance logger */
	protected Logger log = LogManager.getLogger(getClass());
	
	/** The map of aggregators keyed by the global metric ID */
	protected final NonBlockingHashMapLong<IPeriodAggregator> aggregators;
	
	/** The registry's JMX ObjectName */
	public static final ObjectName OBJECT_NAME = JMXHelper.objectName(new StringBuilder(Registry.class.getPackage().getName()).append(":service=").append(Registry.class.getSimpleName()));
	
	/**
	 * Acquires the registry singleton instance
	 * @return the registry
	 */
	public static Registry getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new Registry();
					JMXHelper.registerMBean(instance, OBJECT_NAME);
				}
			}
		}
		return instance;
	}
	
	public static void main(String[] args) {
		BasicConfigurator.configureDefaultContext();
		Registry reg = Registry.getInstance();
		
		try { Thread.sleep(100000); } catch (Exception x) {}
	}

	/**
	 * Creates a new Registry
	 */
	private Registry() {
		int size = UnsafeAdapter.findNextPositivePowerOfTwo(ConfigurationHelper.getIntSystemThenEnvProperty(Constants.REG_INIT_SIZE, Constants.DEFAULT_REG_INIT_SIZE));
		boolean space4speed = ConfigurationHelper.getBooleanSystemThenEnvProperty(Constants.REG_SPACE_FOR_SPEED, Constants.DEFAULT_REG_SPACE_FOR_SPEED);
		log.info("Registry Map Options:\n\tsize: {}\n\tspaceForspeed: {}", size, space4speed);
		aggregators = new NonBlockingHashMapLong<IPeriodAggregator>(size, space4speed);		
		log.info(StringHelper.banner("Registry Started"));
	}
	
	/**
	 * Returns the timestamp of the start of the current period
	 * @return the timestamp of the start of the current period
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Returns the timestamp of the end of the current period
	 * @return the timestamp of the end of the current period
	 */
	public long getEndTime() {
		return endTime;
	}

	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.control.RegistryMXBean#getAggregatorCount()
	 */
	@Override
	public long getAggregatorCount() {
		return aggregators.size();
	}

}
