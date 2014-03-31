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
package org.helios.pag.store;

import java.nio.CharBuffer;

import javassist.ByteArrayClassPath;

import org.helios.pag.util.unsafe.DeAllocateMe;
import org.helios.pag.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: OffHeapCharSequenceKey</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.OffHeapCharSequenceKey</code></p>
 */

public class OffHeapCharSequenceKey implements DeAllocateMe, CharSequence {
	/** The address of the allocated charsequence data */
	protected final long[] address = new long[1];
	
	/** The offset of the char arr length */
	public static final byte LENGTH = 0;		// Int
	/** The offset of the char arr hashcode */
	public static final byte HASHCODE = LENGTH + UnsafeAdapter.INT_SIZE;		// Int
	/** The offset of the chars */
	public static final byte CHARS = HASHCODE + UnsafeAdapter.INT_SIZE;
	
	
	/**
	 * Creates a new OffHeapCharSequenceKey
	 * @param stringy The char sequence to put off-heap
	 */
	public OffHeapCharSequenceKey(CharSequence stringy) {
		if(stringy==null) throw new IllegalArgumentException("The passed stringy was null");
		String s = stringy.toString();
		char[] chars = s.toCharArray();
		address[0] = UnsafeAdapter.allocateAlignedMemory((chars.length*2) + (UnsafeAdapter.INT_SIZE*2));
		UnsafeAdapter.registerForDeAlloc(this);
		UnsafeAdapter.putInt(address[0] + LENGTH, chars.length);		
		UnsafeAdapter.putInt(address[0] + HASHCODE, s.hashCode());
		UnsafeAdapter.copyMemory(chars, UnsafeAdapter.CHAR_ARRAY_OFFSET, null, address[0] + CHARS, chars.length * 2);		
	}
	
	/**
	 * Creates a new OffHeapCharSequenceKey
	 * @param stringy The char sequence to put off-heap
	 */
	public OffHeapCharSequenceKey(char[] stringy) {
		this(new String(stringy));
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.util.unsafe.DeAllocateMe#getAddresses()
	 */
	@Override
	public long[][] getAddresses() {
		return new long[][]{address};
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length() {
		return UnsafeAdapter.getInt(address[0] + LENGTH);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return UnsafeAdapter.getInt(address[0] + HASHCODE);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if(other==null) return false;
		if(!(other instanceof CharSequence)) return false;
		if(other instanceof OffHeapCharSequenceKey) {
			OffHeapCharSequenceKey otherKey = (OffHeapCharSequenceKey)other;
			if(hashCode()!=otherKey.hashCode()) return false;
			if(length()!=otherKey.length()) return false;
			return UnsafeAdapter.compareTo(address[0], length(), otherKey.address[0], length());
		} 
		return this.toString().equals(other.toString());
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		char[] chars = new char[length()];
		UnsafeAdapter.copyMemory(null, address[0] + CHARS, chars, UnsafeAdapter.CHAR_ARRAY_OFFSET, length() * 2);
		return new String(chars);
	}
	

	/**
	 * {@inheritDoc}
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt(int index) {
		return UnsafeAdapter.getChar(address[0] + CHARS + (index*2));
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		if(end>=length() || start < 0 || start > end) throw new IllegalArgumentException("Invalid range [" + start + " --> " + end + "]");
		char[] chars = new char[end-start];
		UnsafeAdapter.copyMemory(null, UnsafeAdapter.CHAR_SIZE * start, chars, UnsafeAdapter.CHAR_ARRAY_OFFSET, (end-start)*UnsafeAdapter.CHAR_SIZE);
		return new OffHeapCharSequenceKey(chars);
	}

}
