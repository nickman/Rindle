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

import com.higherfrequencytrading.chronicle.Excerpt;

/**
 * <p>Title: MemCopyExcerpt</p>
 * <p>Description: Defines the basic MemCpy excerpt signatures</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.MemCopyExcerpt</code></p>
 */

public interface MemCopyExcerpt extends Excerpt {

	/**
	 * Writes the specified number of bytes from the source base and offset to the chronicle at this excerpt's current index
	 * @param sourceBase The base object to copy from
	 * @param sourceOffset The base offset to copy from
	 * @param targetOffset The offset from the start of the excerpt's address to write to
	 * @param bytes The number of bytes to copy
	 */
	public abstract void writeMemory(Object sourceBase, long sourceOffset,
			long targetOffset, long bytes);

	/**
	 * Writes the specified number of bytes from the source base and offset to the chronicle at this excerpt's current index
	 * @param sourceAddress The address to copy from
	 * @param targetOffset The offset from the start of the excerpt's address to write to
	 * @param bytes The number of bytes to copy
	 */
	public abstract void writeMemory(long sourceAddress, long targetOffset,
			long bytes);

	/**
	 * Writes the specified number of bytes from the the chronicle at this excerpt's current index to the specified base object and offset
	 * @param targetBase The base object to copy to
	 * @param targetOffset The base offset to copy to
	 * @param sourceOffset The offset from the start of the excerpt's address to read from
	 * @param bytes The number of bytes to copy
	 */
	public abstract void readMemory(Object targetBase, long targetOffset,
			long sourceOffset, long bytes);

	/**
	 * Writes the specified number of bytes from the the chronicle at this excerpt's current index to the specified base object and offset
	 * @param targetAddress The address to copy to
	 * @param sourceOffset The offset from the start of the excerpt's address to read from
	 * @param bytes The number of bytes to copy
	 */
	public abstract void readMemory(long targetAddress, long sourceOffset,
			long bytes);

}