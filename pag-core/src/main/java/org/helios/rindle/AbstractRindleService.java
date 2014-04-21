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
package org.helios.rindle;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.helios.rindle.control.RindleMain;
import org.helios.rindle.util.JMXHelper;

import com.google.common.util.concurrent.AbstractService;

/**
 * <p>Title: AbstractRindleService</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.AbstractRindleService</code></p>
 */

public abstract class AbstractRindleService extends AbstractService implements RindleService, AbstractRindleServiceMXBean {
	/** Instance logger */
	protected final Logger log = (Logger) LogManager.getLogger(getClass());
	/** Dependent Services */
	protected final Set<RindleService> deps = new HashSet<RindleService>(); 
	
	/**
	 * Creates a new AbstractRindleService
	 */
	public AbstractRindleService() {
		JMXHelper.registerMBean(this, JMXHelper.objectName(
				new StringBuilder(getClass().getPackage().getName())
				.append(":service=").append(getClass().getSimpleName())
		));
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.RindleService#onRindleStarted(org.helios.rindle.control.RindleMain)
	 */
	public void onRindleStarted(RindleMain rindleMain) {
		/* No Op */
	}

	/**
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStart()
	 */
	@Override
	protected void doStart() {

	}

	/**
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStop()
	 */
	@Override
	protected void doStop() {

	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.RindleService#getDependentServices()
	 */
	@Override
	public Collection<RindleService> getDependentServices() {
		return deps;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.RindleService#addDependentServices(org.helios.rindle.RindleService[])
	 */
	@Override
	public void addDependentServices(RindleService...dependencies) {
		if(dependencies!=null) {
			for(RindleService d: dependencies) {
				if(d==null) continue;
				deps.add(d);
			}
		}
	}


	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.LoggerControl#getLevel()
	 */
	@Override
	public String getLevel() {
		return log.getLevel().name();
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.LoggerControl#setLevel(java.lang.String)
	 */
	@Override
	public void setLevel(String levelName) {
		log.setLevel(Level.valueOf(levelName));
		
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.ServiceControl#getState()
	 */
	@Override
	public String getState() {
		return state().name();
	}

}
