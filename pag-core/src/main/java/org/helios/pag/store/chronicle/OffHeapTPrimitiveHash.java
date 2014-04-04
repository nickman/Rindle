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
package org.helios.pag.store.chronicle;

/**
 * <p>Title: OffHeapTPrimitiveHash</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.OffHeapTPrimitiveHash</code></p>
 */

///////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
//Copyright (c) 2009, Rob Eden All Rights Reserved.
//Copyright (c) 2009, Jeff Randall All Rights Reserved.
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////



import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.THash;

import org.helios.pag.util.unsafe.DeAllocateMe;
import org.helios.pag.util.unsafe.UnsafeAdapter;



/**
 * The base class for hashtables of primitive values.  Since there is
 * no notion of object equality for primitives, it isn't possible to
 * use a `REMOVED' object to track deletions in an open-addressed table.
 * So, we have to resort to using a parallel `bookkeeping' array of bytes,
 * in which flags can be set to indicate that a particular slot in the
 * hash table is FREE, FULL, or REMOVED.
 *
 * @author Eric D. Friedman, Rob Eden, Jeff Randall
 * @version $Id: TPrimitiveHash.java,v 1.1.2.6 2010/03/01 23:39:07 robeden Exp $
 */
abstract public class OffHeapTPrimitiveHash extends THash implements DeAllocateMe {
	static final long serialVersionUID = 1L;

	/**
	 * flags indicating whether each position in the hash is
	 * FREE, FULL, or REMOVED
	 */
//	public transient byte[] _states;
	
	protected final long[] thash_address = new long[0];

	/* constants used for state flags */

	/** flag indicating that a slot in the hashtable is available */
	public static final byte FREE = 0;

	/** flag indicating that a slot in the hashtable is occupied */
	public static final byte FULL = 1;

	/**
	 * flag indicating that the value of a slot in the hashtable
	 * was deleted
	 */
	public static final byte REMOVED = 2;
	
	/**
	 * Creates a new <code>THash</code> instance with the default
	 * capacity and load factor.
	 */
	public OffHeapTPrimitiveHash() {
		super();
		thash_address[0] = UnsafeAdapter.allocateAlignedMemory(UnsafeAdapter.INT_SIZE + DEFAULT_CAPACITY);
//		UnsafeAdapter.registerForDeAlloc(this);
		UnsafeAdapter.putInt(thash_address[0], DEFAULT_CAPACITY);
	}


	/**
	 * Creates a new <code>TPrimitiveHash</code> instance with a prime
	 * capacity at or near the specified capacity and with the default
	 * load factor.
	 *
	 * @param initialCapacity an <code>int</code> value
	 */
	public OffHeapTPrimitiveHash( int initialCapacity ) {
		this( initialCapacity, DEFAULT_LOAD_FACTOR );
	}


	/**
	 * Creates a new <code>TPrimitiveHash</code> instance with a prime
	 * capacity at or near the minimum needed to hold
	 * <tt>initialCapacity<tt> elements with load factor
	 * <tt>loadFactor</tt> without triggering a rehash.
	 *
	 * @param initialCapacity an <code>int</code> value
	 * @param loadFactor      a <code>float</code> value
	 */
	public OffHeapTPrimitiveHash( int initialCapacity, float loadFactor ) {
		super();
		initialCapacity = Math.max( 1, initialCapacity );
		thash_address[0] = UnsafeAdapter.allocateAlignedMemory(UnsafeAdapter.INT_SIZE + initialCapacity);
//		UnsafeAdapter.registerForDeAlloc(this);
		UnsafeAdapter.putInt(thash_address[0], initialCapacity);
		_loadFactor = loadFactor;
		setUp( HashFunctions.fastCeil( initialCapacity / loadFactor ) );		
	}
	
	protected final int lengthb() {
		return UnsafeAdapter.getInt(thash_address[0]);
	}
	
	protected final byte getb(int index) {
		return UnsafeAdapter.getByte(thash_address[0] + UnsafeAdapter.INT_SIZE + index);
	}

	protected final void setb(int index, byte value) {
		UnsafeAdapter.putByte(thash_address[0] + UnsafeAdapter.INT_SIZE + index, value);
	}
	


	/**
	 * Returns the capacity of the hash table.  This is the true
	 * physical capacity, without adjusting for the load factor.
	 *
	 * @return the physical capacity of the hash table.
	 */
	public int capacity() {
		return UnsafeAdapter.getInt(thash_address[0]);
	}


	/**
	 * Delete the record at <tt>index</tt>.
	 *
	 * @param index an <code>int</code> value
	 */
	protected void removeAt( int index ) {
		UnsafeAdapter.putByte(thash_address[0] + UnsafeAdapter.INT_SIZE + index, REMOVED);
//		_states[index] = REMOVED;
		super.removeAt( index );
	}


	/**
	 * initializes the hashtable to a prime capacity which is at least
	 * <tt>initialCapacity + 1</tt>.
	 *
	 * @param initialCapacity an <code>int</code> value
	 * @return the actual capacity chosen
	 */
	protected int setUp( int initialCapacity ) {
		int capacity;

		capacity = super.setUp( initialCapacity );
		UnsafeAdapter.putInt(thash_address[0], initialCapacity);
//		_states = new byte[capacity];
		return capacity;
	}
} // TPrimitiveHash
