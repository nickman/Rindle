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

import org.helios.pag.util.unsafe.UnsafeAdapter;

import com.higherfrequencytrading.chronicle.impl.ByteBufferExcerpt;
import com.higherfrequencytrading.chronicle.impl.DirectChronicle;

/**
 * <p>Title: MemCopyByteBufferExcerpt</p>
 * <p>Description: MemCopy implementation for use when the chronicle is not unsafe</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.MemCopyByteBufferExcerpt</code></p>
 */
 
public class MemCopyByteBufferExcerpt extends ByteBufferExcerpt implements MemCopyExcerpt {

	/**
	 * Creates a new MemCopyByteBufferExcerpt
	 * @param chronicle The chronicle this excerpt interacts with
	 */
	public MemCopyByteBufferExcerpt(DirectChronicle chronicle) {
		super(chronicle);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.MemCopyExcerpt#writeExcerpt(org.helios.pag.store.MemCopyExcerpt)
	 */
	@Override
	public void writeExcerpt(MemCopyExcerpt excerpt) {
		if(excerpt==null) return;
		excerpt.position(0);
		byte[] xfer = new byte[(int)excerpt.size()];
		excerpt.read(xfer);
		position(0);
		excerpt.write(xfer);
		excerpt.finish();
	}
	

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.MemCopyExcerpt#writeMemory(java.lang.Object, long, long, long)
	 */
	@Override
	public void writeMemory(Object sourceBase, long sourceOffset, long targetOffset, long bytes) {
		if(bytes<1) return;
		byte[] xfer = new byte[(int)bytes];
		UnsafeAdapter.copyMemory(sourceBase, sourceOffset, xfer, UnsafeAdapter.BYTE_ARRAY_OFFSET, bytes);
		write((int)targetOffset, xfer);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.MemCopyExcerpt#writeMemory(long, long, long)
	 */
	@Override
	public void writeMemory(long sourceAddress, long targetOffset, long bytes) {
		if(bytes<1) return;
		byte[] xfer = new byte[(int)bytes];
		UnsafeAdapter.copyMemory(null, sourceAddress, xfer, UnsafeAdapter.BYTE_ARRAY_OFFSET, bytes);
		write((int)targetOffset, xfer);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.MemCopyExcerpt#readMemory(java.lang.Object, long, long, long)
	 */
	@Override
	public void readMemory(Object targetBase, long targetOffset, long sourceOffset, long bytes) {
		if(bytes<1) return;
		byte[] xfer = new byte[(int)bytes];
		final int cpos = position();
		try {
			position((int)sourceOffset);
			read(xfer);
			UnsafeAdapter.copyMemory(xfer, UnsafeAdapter.BYTE_ARRAY_OFFSET, targetBase, targetOffset, bytes);
		} finally {
			position(cpos);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.pag.store.MemCopyExcerpt#readMemory(long, long, long)
	 */
	@Override
	public void readMemory(long targetAddress, long sourceOffset, long bytes) {
		if(bytes<1) return;
		byte[] xfer = new byte[(int)bytes];
		final int cpos = position();
		try {
			position((int)sourceOffset);
			read(xfer);
			UnsafeAdapter.copyMemory(xfer, UnsafeAdapter.BYTE_ARRAY_OFFSET, null, targetAddress, bytes);
		} finally {
			position(cpos);
		}
	}

}
