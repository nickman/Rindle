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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	/** Instance logger */
	protected final Logger log = LogManager.getLogger(getClass());
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
		long ngid =  IMetricDefinition.NO_ENTRY_VALUE, ncid =  IMetricDefinition.NO_ENTRY_VALUE;
		long ogid =  IMetricDefinition.NO_ENTRY_VALUE, ocid =  IMetricDefinition.NO_ENTRY_VALUE;
		if(name!=null) {
			ngid = cache.getNameCache().get(name);
			if(ngid!=IMetricDefinition.NO_ENTRY_VALUE) {
				ncid = cache.getChronicleIndex(ngid);
				if(ncid==IMetricDefinition.NO_ENTRY_VALUE) {
					throw new RuntimeException("Name id found [" + ngid + "] but no chronicle index found");
				}				
			}
		}
		if(opaqueKey!=null) {
			ogid = cache.getOpaqueCache().get(opaqueKey);
			if(ogid!=IMetricDefinition.NO_ENTRY_VALUE) {
				ocid = cache.getChronicleIndex(ogid);
				if(ocid==IMetricDefinition.NO_ENTRY_VALUE) {
					throw new RuntimeException("Opaque id found [" + ogid + "] but no chronicle index found");
				}				
			}
		}
		try {
			if(ngid==IMetricDefinition.NO_ENTRY_VALUE && ngid==IMetricDefinition.NO_ENTRY_VALUE) {
				// ========================================================================================
				//	Neither name or opque were found. Inserting new metric
				// ========================================================================================
				return writeNewMetric(name, opaqueKey);
			} else if(ngid!=IMetricDefinition.NO_ENTRY_VALUE && ogid!=IMetricDefinition.NO_ENTRY_VALUE) {
				// ========================================================================================
				//	Both name and opque were found. 
				// ========================================================================================
				if(ngid==ogid) {
					// ========================================================================================
					//	name and opque were in the same metric. Optimal ! 
					// ========================================================================================
					return ngid;
				} else {
					// ========================================================================================
					//	name and opque were in different metrics. Merge. 
					// ========================================================================================
					long newGid = writeNewMetric(name, opaqueKey);
					deleteMetricsByCID(ncid, ocid);
					return newGid;
				}
			}
			// ========================================================================================
			//	Found the name only, or the opaque only, so just return the GID of the one found
			// ========================================================================================
			return ngid==IMetricDefinition.NO_ENTRY_VALUE ? ngid : ogid;
		} catch (Exception ex) {
			log.error("createOrUpdate(name:{}, opaque:{} (bytes)) Failed", name, opaqueKey==null ? null : opaqueKey.length, ex);
			throw new RuntimeException("createOrUpdate failure", ex);
		}
	}
	
	/**
	 * Attempts to locate an existing stored metric with the name. If found, will execute a merge if the accompanying value has changed.
	 * @param name The metric name
	 * @return the global ID of the resulting metric
	 */
	public long createOrUpdate(String name) {
		return createOrUpdate(name, null);
	}
	
	/**
	 * Attempts to locate an existing stored metric with the name. If found, will execute a merge if the accompanying value has changed.
	 * @param opaqueKey The metric opaque key
	 * @return the global ID of the resulting metric
	 */
	public long createOrUpdate(byte[] opaqueKey) {
		return createOrUpdate(null, opaqueKey);
	}
	
	/**
	 * Writes a new metric to the chronicle cache
	 * @param name The metric name
	 * @param opaqueKey The opaque key
	 * @return the assigned global id
	 */
	protected long writeNewMetric(String name, byte[] opaqueKey) {
		final UnsafeMetricDefinition newDef = new UnsafeMetricDefinition(IMetricDefinition.NO_ENTRY_VALUE, name, opaqueKey);
		long gid = ChronicleCache.getInstance().executeWriteTask(new WriteTask<Long>(){
			@Override
			public Long call(Excerpt writer) throws Exception {
				write(writer, newDef);
				return newDef.getId();
			}
		});
		ChronicleCache.getInstance().addNewMetricToCache(gid, name, opaqueKey);
		return gid;
		
	}
	
	/**
	 * Deletes stored metrics with the passed globa ids
	 * @param globalIds The global ids of metrics to delete
	 */
	public void deleteMetricsByGID(long...globalIds) {
		if(globalIds==null || globalIds.length==0) return;
		long[] indexes = new long[globalIds.length];
		StringBuilder b = new StringBuilder();
		ChronicleCache cache = ChronicleCache.getInstance();
		for(int i = 0; i < globalIds.length; i++) {
			indexes[i] = cache.getChronicleIndex(globalIds[i]);
			if(indexes[i]==IMetricDefinition.NO_ENTRY_VALUE) {
				b.append("\n\tNo Chronicle Index for GID:").append(globalIds[i]);
			}
		}
		if(b.length()>0) {
			log.warn("Deletion By GID Failures: {}", b.toString());
		}
		deleteMetricsByCID(indexes);
	}
	
	/**
	 * Deletes metrics at the passed Chronicle indexes and cleans the core caches of deleted entries
	 * @param indexes the Chronicle indexes to delete metrics at
	 */
	public void deleteMetricsByCID(final long...indexes) {
		if(indexes==null || indexes.length==0) return;
		final ChronicleCache cache = ChronicleCache.getInstance();
		cache.executeWriteTask(new WriteTask<Void>() {
			@Override
			public Void call(Excerpt ex) throws Exception {
				StringBuilder b = new StringBuilder();
				int dCount = 0;
				for(int i = 0; i < indexes.length; i++) {
					if(indexes[i]==IMetricDefinition.NO_ENTRY_VALUE) continue;
					if(!ex.index(indexes[i])) {
						b.append("\n\tFailed to set index to CID [").append(indexes[i]).append("]");
						continue;
					}
					try {
						ex.position(0);
						UnsafeMetricDefinition umd = read(ex);						
						cache.processDeleteCacheClean(umd);
						ex.position(0);
						ex.writeByte(IMetricDefinition.DELETE_FLAG);
						ex.finish();
						dCount++;
					} catch (Exception e) {
						b.append("\n\tFailed to delete for CID [").append(indexes[i]).append("]:").append(e);
					}					
				}				
				if(b.length()>0) {
					log.warn("Deletion By CID Failures: {}", b.toString());
				}
				//log.info("Deleted {} Metric Chronicle Entries", dCount);
				return null;
			}			
		});
		
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
