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

import java.io.File;

import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.impl.IndexedChronicle;

/**
 * <p>Title: DumpStore</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.DumpStore</code></p>
 */

public class DumpStore {

	/**
	 * Creates a new DumpStore
	 */
	public DumpStore() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Excerpt ex = null;
		IndexedChronicle ic = null;
		ChronicleConfiguration config = new ChronicleConfiguration(); 
		try {
			ic = new IndexedChronicle(config.dataDir.getAbsolutePath() + File.separator + ChronicleCache.CACHE_NAME);
			ex = ic.createExcerpt();
			UnsafeMetricDefinitionMarshaller marshaller = UnsafeMetricDefinitionMarshaller.INSTANCE;
			ex.toStart();
			while(ex.hasNextIndex()) {
				ex.nextIndex();
				long index = ex.index();
				if(ex.readByte(0)==1) {
					System.out.println(index + ":-> Deleted: " + ex.size() + " bytes");
					continue;
				}
				UnsafeMetricDefinition umd = marshaller.read(ex);
//				System.out.println(index + ":-> " + umd);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			if(ex!=null) ex.close();
			if(ic!=null) ic.close();
		}
		
	}

}
