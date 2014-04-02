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

import java.util.concurrent.Callable;

import com.higherfrequencytrading.chronicle.EnumeratedMarshaller;
import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.StopCharTester;

/**
 * <p>Title: UnsafeMetricDefinitionMarshaller</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.UnsafeMetricDefinitionMarshaller</code></p>
 */

public class UnsafeMetricDefinitionMarshaller implements EnumeratedMarshaller<UnsafeMetricDefinition> {
	/** A constant UnsafeMetricDefinitionMarshaller */
	public static final UnsafeMetricDefinitionMarshaller INSTANCE = new UnsafeMetricDefinitionMarshaller();
	
	
	
	/**
	 * Creates a new UnsafeMetricDefinitionMarshaller
	 */
	private UnsafeMetricDefinitionMarshaller() {
		/* No Op */
	}
	
	/**
	 * Attempts to locate an existing stored metric with the name/opaqueKey. If found, will execute a merge if the accompanying value has changed.
	 * If different global ids for both values are located, a new merged entry will be created.
	 * If neither are located (and at least one of them is not null) a new entry will be created.
	 * If both are null, returns {@link IMetricDefinition#NO_ENTRY_VALUE}. 
	 * @param name The metric name
	 * @param opaqueKey The metric opaque key
	 * @return the global ID of the resulting metric
	 */
	public long createOrUpdate(final String name, final byte[] opaqueKey) {
		if(name==null && opaqueKey==null) return IMetricDefinition.NO_ENTRY_VALUE;
		ChronicleCache cache = ChronicleCache.getInstance();
		Excerpt ex = null;
		UnsafeMetricDefinition nameMatch = null, opaqueMatch = null;
		long ngid =  IMetricDefinition.NO_ENTRY_VALUE;
		long ogid =  IMetricDefinition.NO_ENTRY_VALUE;
		if(name!=null) {
			ngid = cache.getChronicleIndex(cache.getNameCache().get(name));
		}
		if(opaqueKey!=null) {
			ogid = cache.getChronicleIndex(cache.getOpaqueCache().get(opaqueKey));
		}
		try {
			if(ngid==IMetricDefinition.NO_ENTRY_VALUE && ogid==IMetricDefinition.NO_ENTRY_VALUE) {
				final UnsafeMetricDefinition newDef = new UnsafeMetricDefinition(ngid, name, opaqueKey);
				return cache.executeWriteTask(new WriteTask<Long>(){
					@Override
					public Long call(Excerpt writer) throws Exception {
						newDef.writeMarshallable(writer);
						return newDef.getId();
					}
				});
			}
			ex = cache.newExcerpt();
			if(ngid!=IMetricDefinition.NO_ENTRY_VALUE) {
				if(!ex.index(ngid)) {
					throw new RuntimeException("Failed to set index to [" + ngid + "]");
				}
				UnsafeMetricDefinition umd = read(ex);
				
			} else {
				
			}
		} finally {
			if(ex!=null) try { ex.close(); } catch (Exception x) {/* No Op */}
		}
		
		return -1L;
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.EnumeratedMarshaller#classMarshaled()
	 */
	@Override
	public Class<UnsafeMetricDefinition> classMarshaled() {
		return UnsafeMetricDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.EnumeratedMarshaller#write(com.higherfrequencytrading.chronicle.Excerpt, java.lang.Object)
	 */
	@Override
	public void write(Excerpt excerpt, UnsafeMetricDefinition e) {
		e.writeMarshallable(excerpt);
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.EnumeratedMarshaller#read(com.higherfrequencytrading.chronicle.Excerpt)
	 */
	@Override
	public UnsafeMetricDefinition read(Excerpt excerpt) {
		return new UnsafeMetricDefinition(excerpt);
	}

	/**
	 * {@inheritDoc}
	 * @see com.higherfrequencytrading.chronicle.EnumeratedMarshaller#parse(com.higherfrequencytrading.chronicle.Excerpt, com.higherfrequencytrading.chronicle.StopCharTester)
	 */
	@Override
	public UnsafeMetricDefinition parse(Excerpt excerpt, StopCharTester tester) {
		return read(excerpt);
	}

}
