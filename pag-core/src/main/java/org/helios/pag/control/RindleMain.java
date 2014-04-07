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
package org.helios.pag.control;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.pag.Constants;
import org.helios.pag.store.IStore;
import org.helios.pag.util.ConfigurationHelper;
import org.helios.pag.util.StringHelper;
import org.helios.pag.util.jmx.concurrency.JMXManagedThreadPool;

import com.google.common.util.concurrent.AbstractService;

/**
 * <p>Title: RindleMain</p>
 * <p>Description: The main Rindle bootstrap</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.control.RindleMain</code></p>
 */

public class RindleMain extends AbstractService {
	/** Static class logger */
	protected static final Logger LOG = LogManager.getLogger(RindleMain.class);
	/** Singleton instance */
	protected static volatile RindleMain instance = null;
	/** Singleton instance ctor lock */
	protected static final Object lock = new Object();
	/** The main general purpose rindle thread pool */
	protected final JMXManagedThreadPool threadPool;
	/** The rindle registry */
	protected Registry registry;
	/** The rindle istore */
	protected IStore istore;
	
	/**
	 * Acquires the RindleMain singleton instance
	 * @return the RindleMain singleton instance
	 */
	public static RindleMain getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new RindleMain();
					instance.start();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Creates a new RindleMain
	 */
	private RindleMain() {
		LOG.info("Rindle Core Starting......");
		threadPool = new JMXManagedThreadPool("main");
		registry = Registry.getInstance();
		LOG.info("Rindle Core Started.");
		
	}

	public static void main(String[] args) {
		LOG.info("Booting Rindle");
		getInstance();
		try { Thread.currentThread().join(); } catch (Exception ex) {/* No Op */}
	}

	/**
	 * Returns the main shared thread pool
	 * @return the main thread pool
	 */
	public JMXManagedThreadPool getThreadPool() {
		return threadPool;
	}

	/**
	 * Returns the rindle registry
	 * @return the registry
	 */
	public Registry getRegistry() {
		return registry;
	}

	/**
	 * Returns the istore implementation
	 * @return the istore
	 */
	public IStore getIstore() {
		return istore;
	}

	/**
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStart()
	 */
	@Override
	protected void doStart() {
		LOG.info("Rindle Services Starting......");
		String istoreClassName = ConfigurationHelper.getSystemThenEnvProperty(Constants.ISTORE_CLASS_NAME, Constants.DEFAULT_ISTORE_CLASS_NAME);
		try {
			Class<?> clazz = Class.forName(istoreClassName);
			if(!IStore.class.isAssignableFrom(clazz)) {
				throw new Exception("Invalid IStore classname [" + istoreClassName + "]");				
			}
			@SuppressWarnings("unchecked")
			Class<IStore> iclazz = (Class<IStore>)clazz;
			istore = iclazz.newInstance();			
			istore.start().addListener(new Runnable(){
				public void run() {
					try { Thread.currentThread().join(100); } catch (Exception ex) {/* No Op */}
					LOG.info(StringHelper.banner("Rindle Started"));
				}
			}, threadPool);
			LOG.info("Rindle Services Started");	
		} catch (Exception ex) {
			LOG.error("Failed to create IStore: {}", istoreClassName, ex);
			throw new RuntimeException("IStore Creation Failure", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStop()
	 */
	@Override
	protected void doStop() {

		
	}
}
