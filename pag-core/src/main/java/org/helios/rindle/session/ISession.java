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
 * <p>Title: ISession</p>
 * <p>Description: Defines a stateful session representing a connected client</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.session.ISession</code></p>
 */
public interface ISession {
	
	
//			session.session = function(Id)
//			session.addSpecifiedGlobalId = function(globalId, Id)
//			session.removeSpecifiedGlobalId = function(globalId, Id)
//
//			session.addPatternedGlobalId = function(globalId, ...)
//			session.removePatternedGlobalId = function(globalId, ...)
//
//			session.addPattern = function(Id, ...)
//			session.removePattern = function(Id, ...)
	
	/**
	 * Initializes this session
	 */
	public void initSession();
	
	/**
	 * Adds the passed global IDs to this session's subscribed metrics
	 * @param globalIds the specified global IDs to add
	 */
	public void addGlobalIds(long...globalIds);
	
	/**
	 * Removes the passed global IDs from this session's subscribed metrics
	 * @param globalIds the specified global IDs to remove
	 */
	public void removeGlobalIds(long...globalIds);
	
	/**
	 * Adds the passed global IDs to this session's pattern matched subscribed metrics
	 * @param globalIds the pattern matched global IDs to add
	 */
	public void addMatchedIds(long...globalIds);
	
	/**
	 * Removes the passed global IDs from this session's pattern matched subscribed metrics
	 * @param globalIds the pattern matched global IDs to remove
	 */
	public void removeMatchedIds(long...globalIds);
	
	/**
	 * Adds metric name matching patterns to this session's subscribed patterns
	 * @param patterns the metric name matching patterns to add
	 */
	public void addPatterns(String...patterns);
	
	/**
	 * Removes metric name matching patterns from this session's subscribed patterns
	 * @param patterns the metric name matching patterns to remove
	 */
	public void removePatterns(String...patterns);
	
	/**
	 * Returns an array of the session's subscribed global ids
	 * @return an array of global ids
	 */
	public long[] getGlobalIds();
	
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
	 * Terminates this session 
	 */
	public void terminateSession();
	
	/**
	 * Returns this session's time to live
	 * @return this session's time to live in seconds
	 */
	public long getTTL();
	
}
