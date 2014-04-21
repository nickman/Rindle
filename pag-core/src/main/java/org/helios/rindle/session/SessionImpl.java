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

import javax.management.ObjectName;

import org.helios.rindle.store.IStore;
import org.helios.rindle.util.JMXHelper;

/**
 * <p>Title: SessionImpl</p>
 * <p>Description: The default session implementation</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.session.SessionImpl</code></p>
 */

public class SessionImpl implements ISession, SessionMXBean {
	/** The assigned session id */
	protected final long sessionId;
	/** The rindle istore */
	protected final IStore istore;
	/** This session's JMX ObjectName */
	protected final ObjectName objectName;
	
	/**
	 * Creates a new SessionImpl
	 * @param sessionId The assigned session id
	 * @param istore The rindle istore
	 */
	public SessionImpl(long sessionId, IStore istore) {
		this.sessionId = sessionId;
		this.istore = istore;
		objectName = JMXHelper.objectName(new StringBuilder(getClass().getPackage().getName()).append(":type=").append(getClass().getSimpleName()).append(",id=").append(sessionId));
		JMXHelper.registerMBean(this, objectName);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#initSession()
	 */
	@Override
	public void initSession() {
		istore.initSession(sessionId);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#addGlobalIds(long[])
	 */
	@Override
	public void addGlobalIds(long... globalIds) {
		istore.addGlobalIds(sessionId, globalIds);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#removeGlobalIds(long[])
	 */
	@Override
	public void removeGlobalIds(long... globalIds) {
		istore.removeGlobalIds(sessionId, globalIds);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#addMatchedIds(long[])
	 */
	@Override
	public void addMatchedIds(long... globalIds) {
		istore.addMatchedIds(sessionId, globalIds);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#removeMatchedIds(long[])
	 */
	@Override
	public void removeMatchedIds(long... globalIds) {
		istore.removeMatchedIds(sessionId, globalIds);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#addPatterns(java.lang.String[])
	 */
	@Override
	public void addPatterns(String... patterns) {
		istore.addPatterns(sessionId, patterns);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#removePatterns(java.lang.String[])
	 */
	@Override
	public void removePatterns(String... patterns) {
		istore.removePatterns(sessionId, patterns);
	}
	
	/**
	 * Returns an array of the session's subscribed global ids
	 * @return an array of global ids
	 */
	public long[] getGlobalIds() {
		return istore.getGlobalIds(sessionId);
	}
	
	/**
	 * Returns an array of the session's pattern matched subscribed global ids
	 * @return an array of global ids
	 */
	public long[] getMatchedIds() {
		return istore.getMatchedIds(sessionId);
	}
	
	/**
	 * Returns an array of the session's metric patterns
	 * @return an array of metric patterns
	 */
	public String[] getPatterns() {
		return istore.getPatterns(sessionId);
	}
	
	/**
	 * Terminates this session 
	 */
	public void terminateSession() {
		istore.terminateSession(sessionId);
		try { JMXHelper.unregisterMBean(objectName); } catch (Exception x) {/* No Op */}
	}
	
	public long getTTL() {
		return istore.ttl(sessionId);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.SessionMXBean#addGlobalId(long)
	 */
	@Override
	public void addGlobalId(long globalId) {
		addGlobalIds(globalId);
		
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.SessionMXBean#addPattern(java.lang.String)
	 */
	@Override
	public void addPattern(String pattern) {
		addPatterns(pattern);
		
	}
	


}
