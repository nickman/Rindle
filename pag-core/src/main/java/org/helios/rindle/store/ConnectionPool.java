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
package org.helios.rindle.store;

import javax.management.MXBean;

/**
 * <p>Title: ConnectionPool</p>
 * <p>Description: MXBean instrumentation for an {@link IStore} pool</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.ConnectionPool</code></p>
 */
@MXBean
public interface ConnectionPool {
	/**
	 * Returns the number of instances currently borrowed from this pool.
	 * @return the number of instances currently borrowed from this pool.
	 */
	public int getNumActive();
	
	/**
	 * Returns the number of idle instances in this pool.
	 * @return the number of idle instances in this pool.
	 */
	public int getNumIdle();
	
	/**
	 * Returns an estimate of the number of threads currently blocked waiting for an object from the pool.
	 * @return an estimate of the number of threads currently blocked waiting for an object from the pool.
	 */
	public int getNumWaiters();
	
	/**
	 * Returns the total number of objects successfully borrowed from this pool over the lifetime of the pool.
	 * @return the total number of objects successfully borrowed from this pool over the lifetime of the pool.
	 */
	public long getBorrowedCount();
	
	/**
	 * Returns the total number of objects created for this pool over the lifetime of the pool.
	 * @return the total number of objects created for this pool over the lifetime of the pool.
	 */
	public long getCreatedCount();
	
	/**
	 * Returns the total number of objects destroyed by this pool over the lifetime of the pool.
	 * @return the total number of objects destroyed by this pool over the lifetime of the pool.
	 */
	public long getDestroyedCount();
	
	/**
	 * Returns the maximum time a thread has waited to borrow objects from the pool.
	 * @return the maximum time a thread has waited to borrow objects from the pool.
	 */
	public long getMaxBorrowWaitTime();
	
	/**
	 * Returns the maximum number of objects that can be allocated by the pool (checked out to clients, or idle awaiting checkout) at a given time.
	 * @return the maximum number of objects that can be allocated by the pool (checked out to clients, or idle awaiting checkout) at a given time.
	 */
	public long getMaxTotal(); 
	
	/**
	 * Returns the total number of objects returned to this pool over the lifetime of the pool.
	 * @return the total number of objects returned to this pool over the lifetime of the pool.
	 */
	public long getReturnedCount();

	/**
	 * Returns the maximum amount of time (in milliseconds) the borrowObject() method should block before throwing an exception when the pool is exhausted
	 * @return the maximum amount of time (in milliseconds) the borrowObject() method should block before throwing an exception when the pool is exhausted
	 */
	public long getMaxWait();
	
	/**
	 * Returns the mean time objects are active for
	 * @return the mean time objects are active for
	 */
	public long getMeanActiveTime();
	
	/**
	 * Returns the mean time threads wait to borrow an object
	 * @return the mean time threads wait to borrow an object
	 */
	public long getMeanBorrowWaitTime();
    
	
	
}
