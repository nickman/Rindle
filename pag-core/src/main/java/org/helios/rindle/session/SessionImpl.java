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

/**
 * <p>Title: SessionImpl</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.session.SessionImpl</code></p>
 */

public class SessionImpl implements ISession {
	/** The assigned session id */
	protected final long sessionId;
	
	/**
	 * Creates a new SessionImpl
	 * @param sessionId The assigned session id
	 */
	public SessionImpl(long sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#initSession()
	 */
	@Override
	public void initSession() {

	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#addGlobalIds(long[])
	 */
	@Override
	public void addGlobalIds(long... globalIds) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#removeGlobalIds(long[])
	 */
	@Override
	public void removeGlobalIds(long... globalIds) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#addMatchedIds(long[])
	 */
	@Override
	public void addMatchedIds(long... globalIds) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#removeMatchedIds(long[])
	 */
	@Override
	public void removeMatchedIds(long... globalIds) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#addPatterns(java.lang.String[])
	 */
	@Override
	public void addPatterns(String... patterns) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.session.ISession#removePatterns(java.lang.String[])
	 */
	@Override
	public void removePatterns(String... patterns) {
		// TODO Auto-generated method stub

	}

}
