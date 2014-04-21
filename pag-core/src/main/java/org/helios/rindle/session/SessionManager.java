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
package org.helios.rindle.session;

import java.util.concurrent.atomic.AtomicLong;

import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.helios.rindle.AbstractRindleService;
import org.helios.rindle.store.IStore;
import org.helios.rindle.util.JMXHelper;

/**
 * <p>Title: SessionManager</p>
 * <p>Description: The session management service</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.session.SessionManager</code></p>
 */

public class SessionManager extends AbstractRindleService implements SessionManagerMBean {
	/** The rindle istore */
	protected final IStore istore;
	
	/** The session id factory */
	protected final AtomicLong sessionIdFactory = new AtomicLong();
	
	/** A map of sessions keyed by the session id */
	protected final NonBlockingHashMapLong<ISession> sessions = new NonBlockingHashMapLong<ISession>(256, true);
	
	/**
	 * Creates a new SessionManager
	 * @param istore The rindle istore
	 */
	public SessionManager(IStore istore) {
		this.istore = istore;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.AbstractRindleService#doStart()
	 */
	@Override
	protected void doStart() {		
		super.doStart();
//		JMXHelper.registerMBean(this, JMXHelper.objectName(new StringBuilder(getClass().getPackage().getName()).append(":service=").append(getClass().getSimpleName())));
		notifyStarted();
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.AbstractRindleService#doStop()
	 */
	@Override
	protected void doStop() {	
		super.doStop();
		notifyStopped();
	}
	
	/**
	 * Initializes a new session
	 * @return the id of the session
	 */
	@Override
	public long newSession() {
		final long id = sessionIdFactory.incrementAndGet();
		ISession session = new SessionImpl(id, istore);
		sessions.put(id, session);
		session.initSession();
		return id;
	}
	
	/**
	 * Terminates a session
	 * @param id the id of the session to terminate
	 */
	public void terminateSession(long id) {
		ISession session = sessions.get(id);
		if(session!=null) session.terminateSession();
	}
	
	/**
	 * Returns the currently active sessions
	 * @return an array of sessions
	 */
	public long[] getCurrentSessions() {
		return istore.getCurrentSessions();
	}

}
