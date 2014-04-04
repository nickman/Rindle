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
package org.helios.pag.store.chronicle;

import org.helios.pag.util.unsafe.UnsafeAdapter.SpinLock;

import com.higherfrequencytrading.chronicle.EnumeratedMarshaller;
import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.StopCharTester;

/**
 * <p>Title: ChronicleCacheEntryMarshaller</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.ChronicleCacheEntryMarshaller</code></p>
 */

public class ChronicleCacheEntryMarshaller implements EnumeratedMarshaller<ChronicleCacheEntry> {
	/** The spin lock that guards writes to the chronicle */
	protected final SpinLock spinLock;
	
	/**
	 * Creates a new ChronicleCacheEntryMarshaller
	 * @param spinLock The spin lock that guards writes to the chronicle
	 */
	ChronicleCacheEntryMarshaller(SpinLock spinLock) {
		this.spinLock = spinLock;
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.EnumeratedMarshaller#classMarshaled()
	 */
	@Override
	public Class<ChronicleCacheEntry> classMarshaled() {
		return ChronicleCacheEntry.class;
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.EnumeratedMarshaller#write(com.higherfrequencytrading.chronicle.Excerpt, java.lang.Object)
	 */
	@Override
	public void write(Excerpt excerpt, ChronicleCacheEntry e) {
		if(e.isNull()) return;
		spinLock.xlock();
		try {
			excerpt.index(e.getGlobalId());
			ChronicleCacheEntry stored = read(excerpt);
			if(!e.equals(stored)) {
				
			}
			e.writeMarshallable(excerpt);
		} finally {
			spinLock.xunlock();
		}		
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.EnumeratedMarshaller#read(com.higherfrequencytrading.chronicle.Excerpt)
	 */
	@Override
	public ChronicleCacheEntry read(Excerpt excerpt) {
		excerpt.toStart();		
		return ChronicleCacheEntry.load(excerpt.index(), excerpt);
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.EnumeratedMarshaller#parse(com.higherfrequencytrading.chronicle.Excerpt, com.higherfrequencytrading.chronicle.StopCharTester)
	 */
	@Override
	public ChronicleCacheEntry parse(Excerpt excerpt, StopCharTester tester) {
		// TODO Auto-generated method stub
		return null;
	}

}
