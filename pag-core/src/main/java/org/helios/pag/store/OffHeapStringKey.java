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
package org.helios.pag.store;

import java.nio.charset.Charset;

import org.helios.pag.util.unsafe.DeAllocateMe;
import org.helios.pag.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: OffHeapStringKey</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.OffHeapStringKey</code></p>
 */

public class OffHeapStringKey implements OffHeapKey<String>,  DeAllocateMe {
	/** The address of the StringPointer */
	protected final long address;
	
	/** The offset of the represented string's hashcode */
	public static final byte HASH_CODE = 0;
	/** The offset of the represented string's byte length */
	public static final byte LENGTH = HASH_CODE + UnsafeAdapter.INT_SIZE;
	/** The offset of the represented string's bytes */
	public static final byte BYTES = LENGTH + UnsafeAdapter.INT_SIZE;
	
	/** The size of the header in bytes */
	public static final byte HEADER_SIZE = UnsafeAdapter.INT_SIZE*2;
	
	/** The default platform charset */
	public static final Charset CHARSET = Charset.defaultCharset();

	
	/**
	 * Creates a new OffHeapStringKey
	 * @param s The stringy to offheap
	 */
	public OffHeapStringKey(CharSequence s) {
		if(s==null) throw new IllegalArgumentException("The passed charsequence was null");
		String _s = s.toString();
		byte[] bytes = _s.getBytes(CHARSET);			
		address = UnsafeAdapter.allocateAlignedMemory(bytes.length + HEADER_SIZE);
		UnsafeAdapter.putInt(address + HASH_CODE, _s.hashCode());
		UnsafeAdapter.putInt(address + LENGTH, bytes.length);
		UnsafeAdapter.copyMemory(bytes, UnsafeAdapter.BYTE_ARRAY_OFFSET, null, address + BYTES, bytes.length);
		UnsafeAdapter.registerForDeAlloc(this);
	}


	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.util.unsafe.DeAllocateMe#getAddresses()
	 */
	@Override
	public long[] getAddresses() {
		return new long[]{address};
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return UnsafeAdapter.getInt(address + HASH_CODE);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof OffHeapKey)) return false;
		return UnsafeAdapter.byteArraysEqual(getBytes(), ((OffHeapKey<?>)obj).getBytes());
	}
	


	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.OffHeapKey#getBytes()
	 */
	public byte[] getBytes() {
		return UnsafeAdapter.getByteArray(address + BYTES, getLength());
	}

	/**
	 * Returns the number of bytes for the string pointer's content
	 * @return the number of bytes for the string pointer's content
	 */
	protected int getLength() {
		return UnsafeAdapter.getInt(address + LENGTH);
	}
	
	/**
	 * <p>Dereferences the pointer</p>
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new String(getBytes(), CHARSET);
	}
	
	/**
	 * Creates a temporary on-heap look up key that will hashcode/equals against true off-heap keys in a cache
	 * @param stringy The stringy to create a key for
	 * @return the offheap lookup key for the stringy
	 */
	public static OffHeapKey<String> lookupKey(CharSequence stringy) {
		if(stringy==null) throw new IllegalArgumentException("The passed stringy was null");
		final String s = stringy.toString();
		return new OffHeapKey<String>() {
			/**
			 * {@inheritDoc}
			 * @see java.lang.Object#hashCode()
			 */
			@Override
			public int hashCode() {
				return s.hashCode();
			}
			
			/**
			 * {@inheritDoc}
			 * @see org.helios.pag.store.OffHeapKey#getBytes()
			 */
			@Override
			public byte[] getBytes() {				
				return s.getBytes(CHARSET);
			}
			
			/**
			 * {@inheritDoc}
			 * @see java.lang.Object#equals(java.lang.Object)
			 */
			@Override
			public boolean equals(Object obj) {
				if(obj==null || !(obj instanceof OffHeapKey)) return false;
				return UnsafeAdapter.byteArraysEqual(getBytes(), ((OffHeapKey<?>)obj).getBytes());
			}
		};
	}

}
