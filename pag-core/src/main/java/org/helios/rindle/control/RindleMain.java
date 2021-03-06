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
package org.helios.rindle.control;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.rindle.AbstractRindleService;
import org.helios.rindle.Constants;
import org.helios.rindle.RindleService;
import org.helios.rindle.session.ISession;
import org.helios.rindle.session.SessionManager;
import org.helios.rindle.store.IStore;
import org.helios.rindle.submit.ISubmit;
import org.helios.rindle.submit.SubmitImpl;
import org.helios.rindle.util.ConfigurationHelper;
import org.helios.rindle.util.jmx.concurrency.JMXManagedThreadPool;
import org.helios.rindle.util.jmx.jmxmp.JMXMPConnectionServer;

import com.google.common.util.concurrent.ServiceManager;

/**
 * <p>Title: RindleMain</p>
 * <p>Description: The main Rindle bootstrap</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.control.RindleMain</code></p>
 */

public class RindleMain extends AbstractRindleService {
	/** Static class logger */
	protected static final Logger LOG = LogManager.getLogger(RindleMain.class);
	/** Singleton instance */
	protected static volatile RindleMain instance = null;
	/** Singleton instance ctor lock */
	protected static final Object lock = new Object();
	/** The main general purpose rindle thread pool */
	protected final JMXManagedThreadPool threadPool;
	/** The rindle registry */
	protected Registry registry = Registry.getInstance();
	/** The rindle istore */
	protected IStore istore = createIStore();
	/** The rindle session manager */
	protected SessionManager sessionManager = new SessionManager(istore);
	/** The rindle JMX connector server */
	protected final JMXMPConnectionServer jmxmpServer = new JMXMPConnectionServer(null);
	/** The core submitter */
	protected ISubmit submitter = null;
	/**
	 * Returns the 
	 * @return the submitter
	 */
	public ISubmit getSubmitter() {
		return submitter;
	}

	/** The rindle service manager */
	protected ServiceManager serviceManager;
	
	
	/** The rindle services */
	protected final Set<RindleService> rindleServices = new CopyOnWriteArraySet<RindleService>();

	/**
	 * Acquires the RindleMain singleton instance
	 * @return the RindleMain singleton instance
	 */
	public static RindleMain getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new RindleMain();
					LOG.info("Rindle Services Starting......");
					instance.serviceManager.startAsync();
					instance.serviceManager.awaitHealthy();
					for(RindleService svc: instance.rindleServices) {
						svc.onRindleStarted(instance);
					}
					instance.submitter = new SubmitImpl(instance.registry, instance.istore);
					LOG.info("********************************");
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
		
		LOG.info("Rindle Core Started.");
		addRindleService(this);
		addRindleService(istore);
		addRindleService(jmxmpServer);
		addRindleService(sessionManager);
		LOG.info("Rindle Services to Start: {}", rindleServices.size());
		serviceManager = new ServiceManager(rindleServices);
	}

	public static void main(String[] args) {
		LOG.info("Booting Rindle");
		try {
			RindleMain rm = getInstance();
			rm.getIstore().purge();
			
			SessionManager sm = rm.getSessionManager();
			long id = sm.newSession();
			ISession session = sm.getSession(id);
			
			session.addPatterns("*");
			
			
			//long result = rm.getIstore().getGlobalId("AAA", "BBB".getBytes());
			long result = rm.getIstore().getGlobalId("FOO", "BAR".getBytes());
			LOG.info("Foo Result: {}", result);
			result = rm.getIstore().getGlobalId("XXX");
			LOG.info("XXX Result: {}", result);
			result = rm.getIstore().getGlobalId("XYX".getBytes());
			LOG.info("XYX Result: {}", result);
			
			result = rm.getIstore().getGlobalId("XXX", "XYX".getBytes());
			LOG.info("XXX/XYX Results: {}", result);
			
			result = rm.getIstore().getGlobalId("SNA");
			LOG.info("SNA Result: {}", result);
			result = rm.getIstore().getGlobalId("SNA", "FU".getBytes());
			LOG.info("SNAFU Result: {}", result);
			
			LOG.info("METRICS:  {}", Arrays.toString(rm.getIstore().getMetrics(3,4)));
			LOG.info("JSON:  {}", rm.getIstore().getMetricsJSON(3,4));
			
			long[] arr = rm.getIstore().getGlobalIds("*");
			LOG.info("IDs: {}", Arrays.toString(arr));
			
			
			
			
			Thread.sleep(300000000);
			System.exit(0);
		} catch (Exception x) {
			try { Thread.sleep(3000); } catch (Exception xx) {}
			x.printStackTrace(System.err);
			System.exit(-1);
		}
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
		LOG.info("RINDLE BOOTED");
		notifyStarted();
	}
	
	
	/**
	 * Creates an IStore instance
	 * @return the created IStore instance
	 */
	protected static IStore createIStore() {
		String istoreClassName = ConfigurationHelper.getSystemThenEnvProperty(Constants.ISTORE_CLASS_NAME, Constants.DEFAULT_ISTORE_CLASS_NAME);
		try {
			Class<?> clazz = Class.forName(istoreClassName);
			if(!IStore.class.isAssignableFrom(clazz)) {
				throw new Exception("Invalid IStore classname [" + istoreClassName + "]");				
			}
			@SuppressWarnings("unchecked")
			Class<IStore> iclazz = (Class<IStore>)clazz;
			return iclazz.newInstance();				
		} catch (Exception ex) {
			LOG.error("Failed to create IStore instance", ex);
			throw new RuntimeException("Failed to create IStore instance", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStop()
	 */
	@Override
	protected void doStop() {

		
	}
	
	/**
	 * Recursively adds a tree of dependent rindle services
	 * @param services a collection of rindle services to add
	 */
	protected void addRindleServices(Collection<RindleService> services) {
		for(RindleService svc: services) {
			if(svc==null) continue;			
			if(!rindleServices.contains(svc)) {
				LOG.info("===========  Adding: {}", svc.getClass().getSimpleName());
				rindleServices.add(svc);
				addRindleServices(svc.getDependentServices());
			}
		}
	}
	
	/**
	 * Adds a rindle service
	 * @param service The service to add
	 */
	protected void addRindleService(RindleService service) {
		if(service!=null) {
			addRindleServices(Collections.singleton(service));
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.RindleService#getDependentServices()
	 */
	@Override
	public Collection<RindleService> getDependentServices() {
		return rindleServices;
	}

	/**
	 * Returns the session manager
	 * @return the session manager
	 */
	public SessionManager getSessionManager() {
		return sessionManager;
	}
}
