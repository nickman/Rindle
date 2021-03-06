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
package org.helios.rindle.store.chronicle;

/**
 * <p>Title: OffHeapTLongLongHash</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.OffHeapTLongLongHash</code></p>
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
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.procedure.TLongProcedure;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.helios.rindle.util.unsafe.DeAllocateMe;
import org.helios.rindle.util.unsafe.UnsafeAdapter;


//////////////////////////////////////////////////
//THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * An open addressed hashing implementation for long/long primitive entries.
 *
 * Created: Sun Nov  4 08:56:06 2001
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 * @version $Id: _K__V_Hash.template,v 1.1.2.6 2009/11/07 03:36:44 robeden Exp $
 */
abstract public class OffHeapTLongLongHash extends OffHeapTPrimitiveHash implements DeAllocateMe {
	static final long serialVersionUID = 1L;
	
	protected final long[] ll_hash_address = new long[0];
	
	

	/** the set of longs */
//	public transient long[] _set;


	/**
	 * key that represents null
	 *
	 * NOTE: should not be modified after the Hash is created, but is
	 *       not final because of Externalization
	 *
	 */
	protected long no_entry_key;


	/**
	 * value that represents null
	 *
	 * NOTE: should not be modified after the Hash is created, but is
	 *       not final because of Externalization
	 *
	 */
	protected long no_entry_value;

	protected boolean consumeFreeSlot;

	/**
	 * Creates a new <code>T#E#Hash</code> instance with the default
	 * capacity and load factor.
	 */
	public OffHeapTLongLongHash() {
		super();
		no_entry_key = ( long ) 0;
		no_entry_value = ( long ) 0;
	}


	/**
	 * Creates a new <code>T#E#Hash</code> instance whose capacity
	 * is the next highest prime above <tt>initialCapacity + 1</tt>
	 * unless that value is already prime.
	 *
	 * @param initialCapacity an <code>int</code> value
	 */
	public OffHeapTLongLongHash( int initialCapacity ) {
		super( initialCapacity );
		no_entry_key = ( long ) 0;
		no_entry_value = ( long ) 0;
	}


	/**
	 * Creates a new <code>TLongLongHash</code> instance with a prime
	 * value at or near the specified capacity and load factor.
	 *
	 * @param initialCapacity used to find a prime capacity for the table.
	 * @param loadFactor used to calculate the threshold over which
	 * rehashing takes place.
	 */
	public OffHeapTLongLongHash( int initialCapacity, float loadFactor ) {
		super(initialCapacity, loadFactor);
		no_entry_key = ( long ) 0;
		no_entry_value = ( long ) 0;
	}


	/**
	 * Creates a new <code>TLongLongHash</code> instance with a prime
	 * value at or near the specified capacity and load factor.
	 *
	 * @param initialCapacity used to find a prime capacity for the table.
	 * @param loadFactor used to calculate the threshold over which
	 * rehashing takes place.
	 * @param no_entry_value value that represents null
	 */
	public OffHeapTLongLongHash( int initialCapacity, float loadFactor,
			long no_entry_key, long no_entry_value ) {
		super(initialCapacity, loadFactor);
		this.no_entry_key = no_entry_key;
		this.no_entry_value = no_entry_value;
		ll_hash_address[0] = UnsafeAdapter.allocateAlignedMemory(UnsafeAdapter.INT_SIZE + initialCapacity * UnsafeAdapter.LONG_SIZE);
//		UnsafeAdapter.registerForDeAlloc(this);
		UnsafeAdapter.putInt(ll_hash_address[0], initialCapacity);
		
	}


	/**
	 * Returns the value that is used to represent null as a key. The default
	 * value is generally zero, but can be changed during construction
	 * of the collection.
	 *
	 * @return the value that represents null
	 */
	public long getNoEntryKey() {
		return no_entry_key;
	}


	/**
	 * Returns the value that is used to represent null. The default
	 * value is generally zero, but can be changed during construction
	 * of the collection.
	 *
	 * @return the value that represents null
	 */
	public long getNoEntryValue() {
		return no_entry_value;
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
		ll_hash_address[0] = UnsafeAdapter.reallocateAlignedMemory(ll_hash_address[0], UnsafeAdapter.INT_SIZE + capacity * UnsafeAdapter.LONG_SIZE);
		UnsafeAdapter.putInt(ll_hash_address[0], capacity);
//		_set = new long[capacity];
		return capacity;
	}


	/**
	 * Searches the set for <tt>val</tt>
	 *
	 * @param val an <code>long</code> value
	 * @return a <code>boolean</code> value
	 */
	public boolean contains( long val ) {
		return index(val) >= 0;
	}

	protected int length() {
		return UnsafeAdapter.getInt(ll_hash_address[0]);
	}
	
	protected long get(int index) {
		return UnsafeAdapter.getLong(ll_hash_address[0] + UnsafeAdapter.INT_SIZE + (index * UnsafeAdapter.LONG_SIZE));
	}

	protected void set(int index, long value) {
		UnsafeAdapter.putLong(ll_hash_address[0] + UnsafeAdapter.INT_SIZE + (index * UnsafeAdapter.LONG_SIZE), value);
	}
	
	/**
	 * Executes <tt>procedure</tt> for each key in the map.
	 *
	 * @param procedure a <code>TLongProcedure</code> value
	 * @return false if the loop over the set terminated because
	 * the procedure returned false for some value.
	 */
	public boolean forEach( TLongProcedure procedure ) {
//		byte[] states = _states;
//		long[] set = _set;
		for ( int i = length(); i-- > 0; ) {
			if ( getb(i) == FULL && ! procedure.execute( get(i) ) ) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Releases the element currently stored at <tt>index</tt>.
	 *
	 * @param index an <code>int</code> value
	 */
	protected void removeAt( int index ) {
//		get(index) = no_entry_key;
		set(index, no_entry_key);
		super.removeAt( index );
	}


	/**
	 * Locates the index of <tt>val</tt>.
	 *
	 * @param key an <code>long</code> value
	 * @return the index of <tt>val</tt> or -1 if it isn't in the set.
	 */
	protected int index( long key ) {
		int hash, probe, index, length;

//		final byte[] states = _states;
//		final long[] set = _set;
		length = lengthb(); //states.length;
		hash = HashFunctions.hash( key ) & 0x7fffffff;
		index = hash % length;
		byte state = getb(index); //states[index];

		if (state == FREE)
			return -1;

		if (state == FULL && get(index) == key)
			return index;

		return indexRehashed(key, index, hash, state);
	}

	int indexRehashed(long key, int index, int hash, byte state) {
		// see Knuth, p. 529
		int length = length(); //_set.length;
		int probe = 1 + (hash % (length - 2));
		final int loopIndex = index;

		do {
			index -= probe;
			if (index < 0) {
				index += length;
			}
			state = getb(index); //_states[index];
			//
			if (state == FREE)
				return -1;

			//
			if (key == get(index) && state != REMOVED)
				return index;
		} while (index != loopIndex);

		return -1;
	}


	/**
	 * Locates the index at which <tt>val</tt> can be inserted.  if
	 * there is already a value equal()ing <tt>val</tt> in the set,
	 * returns that value as a negative integer.
	 *
	 * @param key an <code>long</code> value
	 * @return an <code>int</code> value
	 */
	protected int insertKey( long val ) {
		int hash, index;

		hash = HashFunctions.hash(val) & 0x7fffffff;
		index = hash % lengthb(); //_states.length;
		byte state = getb(index); //_states[index];

		consumeFreeSlot = false;

		if (state == FREE) {
			consumeFreeSlot = true;
			insertKeyAt(index, val);

			return index;       // empty, all done
		}

		if (state == FULL && get(index) == val) {
			return -index - 1;   // already stored
		}

		// already FULL or REMOVED, must probe
		return insertKeyRehash(val, index, hash, state);
	}

	int insertKeyRehash(long val, int index, int hash, byte state) {
		// compute the double hash
		final int length = length(); //_set.length;
		int probe = 1 + (hash % (length - 2));
		final int loopIndex = index;
		int firstRemoved = -1;

		/**
		 * Look until FREE slot or we start to loop
		 */
		do {
			// Identify first removed slot
			if (state == REMOVED && firstRemoved == -1)
				firstRemoved = index;

			index -= probe;
			if (index < 0) {
				index += length;
			}
			state = getb(index); //_states[index];

			// A FREE slot stops the search
			if (state == FREE) {
				if (firstRemoved != -1) {
					insertKeyAt(firstRemoved, val);
					return firstRemoved;
				} else {
					consumeFreeSlot = true;
					insertKeyAt(index, val);
					return index;
				}
			}

			if (state == FULL && get(index) == val) {
				return -index - 1;
			}

			// Detect loop
		} while (index != loopIndex);

		// We inspected all reachable slots and did not find a FREE one
		// If we found a REMOVED slot we return the first one found
		if (firstRemoved != -1) {
			insertKeyAt(firstRemoved, val);
			return firstRemoved;
		}

		// Can a resizing strategy be found that resizes the set?
		throw new IllegalStateException("No free or removed slots available. Key set full?!!");
	}

	void insertKeyAt(int index, long val) {
		set(index, val); // = val;  // insert value
		setb(index, FULL); //_states[index] = FULL;
	}

	protected int XinsertKey( long key ) {
		int hash, probe, index, length;

//		final byte[] states = _states;
//		final long[] set = _set;
		length = lengthb(); //states.length;
		hash = HashFunctions.hash( key ) & 0x7fffffff;
		index = hash % length;
		byte state = getb(index); //states[index];

		consumeFreeSlot = false;

		if ( state == FREE ) {
			consumeFreeSlot = true;
			set(index, key); //set[index] = key;			
			setb(index, FULL); //states[index] = FULL;

			return index;       // empty, all done
		} else if ( state == FULL && get(index) == key ) {
			return -index -1;   // already stored
		} else {                // already FULL or REMOVED, must probe
			// compute the double hash
			probe = 1 + ( hash % ( length - 2 ) );

			// if the slot we landed on is FULL (but not removed), probe
			// until we find an empty slot, a REMOVED slot, or an element
			// equal to the one we are trying to insert.
			// finding an empty slot means that the value is not present
			// and that we should use that slot as the insertion point;
			// finding a REMOVED slot means that we need to keep searching,
			// however we want to remember the offset of that REMOVED slot
			// so we can reuse it in case a "new" insertion (i.e. not an update)
			// is possible.
			// finding a matching value means that we've found that our desired
			// key is already in the table

			if ( state != REMOVED ) {
				// starting at the natural offset, probe until we find an
				// offset that isn't full.
				do {
					index -= probe;
					if (index < 0) {
						index += length;
					}
					state = getb(index); //states[index];
				} while ( state == FULL && get(index) != key );
			}

			// if the index we found was removed: continue probing until we
			// locate a free location or an element which equal()s the
			// one we have.
			if ( state == REMOVED) {
				int firstRemoved = index;
				while ( state != FREE && ( state == REMOVED || get(index) != key ) ) {
					index -= probe;
					if (index < 0) {
						index += length;
					}
					state = getb(index);
				}

				if (state == FULL) {
					return -index -1;
				} else {
					set(index, key); //set[index] = key;
					setb(index, FULL); //states[index] = FULL;

					return firstRemoved;
				}
			}
			// if it's full, the key is already stored
			if (state == FULL) {
				return -index -1;
			} else {
				consumeFreeSlot = true;
				set(index, key); //set[index] = key;
				setb(index, FULL); //states[index] = FULL;

				return index;
			}
		}
	}


	/** {@inheritDoc} */
	public void writeExternal( ObjectOutput out ) throws IOException {
		// VERSION
		out.writeByte( 0 );

		// SUPER
		super.writeExternal( out );

		// NO_ENTRY_KEY
		out.writeLong( no_entry_key );

		// NO_ENTRY_VALUE
		out.writeLong( no_entry_value );
	}


	/** {@inheritDoc} */
	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException {
		// VERSION
		in.readByte();

		// SUPER
		super.readExternal( in );

		// NO_ENTRY_KEY
		no_entry_key = in.readLong();

		// NO_ENTRY_VALUE
		no_entry_value = in.readLong();
	}
} // TLongLongHash
