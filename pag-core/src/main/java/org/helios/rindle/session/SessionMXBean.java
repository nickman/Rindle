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
package org.helios.rindle.session;

import java.util.Map;

import javax.management.MXBean;

/**
 * <p>Title: SessionMXBean</p>
 * <p>Description: MXBean interface for {@link ISession} instances</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.session.SessionMXBean</code></p>
 */
@MXBean
public interface SessionMXBean {
	/**
	 * Returns an array of the session's subscribed global ids
	 * @return an array of global ids
	 */
	public long[] getGlobalIds();
	
	/**
	 * Subscribe this session to the specified global id
	 * @param globalId the global id
	 */
	public void addGlobalId(long globalId);
	
	/**
	 * Remove the specified global id from this session
	 * @param globalId the global id
	 */
	public void removeGlobalId(long globalId);
	
	
	/**
	 * Returns the session sub-keys
	 * @return the session sub-keys
	 */
	public Map<String, String> getSessionKeys();
	
	/**
	 * Returns an array of the session's pattern matched subscribed global ids
	 * @return an array of global ids
	 */
	public long[] getMatchedIds();
	
	/**
	 * Returns an array of the session's metric patterns
	 * @return an array of metric patterns
	 */
	public String[] getPatterns();
	
	/**
	 * Remove the pattern from this session
	 * @param pattern the pattern to remove
	 */
	public void removePattern(String pattern);
	
	
	/**
	 * Subscribes this session to the specified pattern
	 * @param pattern the pattern to subscribe to
	 */
	public void addPattern(String pattern);
	
	/**
	 * Terminates this session 
	 */
	public void terminateSession();
	
	/**
	 * Touches the session, keeping it alive for a new expiration period
	 */
	public void touchSession();
	
	/**
	 * Returns this session's time to live
	 * @return this session's time to live in seconds
	 */
	public long getTTL();

}
