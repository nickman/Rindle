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

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.rindle.Constants;
import org.helios.rindle.util.ConfigurationHelper;
import org.helios.rindle.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: ChronicleConfiguration</p>
 * <p>Description: General chronicle store configuration</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.ChronicleConfiguration</code></p>
 */

public class ChronicleConfiguration {
	/** The config property name to specify the chronicle data directory */
	public static final String CHRONICLE_DIR = "helios.pag.store.chronicle.dir";
	/** The default chronicle data directory */
	public static final String DEFAULT_CHRONICLE_DIR =  String.format("%s%srindle%s%sstore", System.getProperty("java.io.tmpdir"), File.separator, File.separator, File.separator);

	/** The config property name to specify if chronicle should use unsafe excerpts */
	public static final String CHRONICLE_UNSAFE_PROP = "helios.pag.store.chronicle.unsafe";
    /** The default chronicle unsafe excerpt setting */
    public static final boolean DEFAULT_CHRONICLE_UNSAFE = true;
    
    /** Static class logger */
    private static final Logger LOG = LogManager.getLogger(ChronicleConfiguration.class);

	
	/** The store directory */
	public final File dataDir;
	/** The unsafe option for chronicles */
	public final boolean unsafe;
	
	/** The initial capacity of the global ID cache fronting the chronicle cache */
	public final int idCacheInitialCapacity;
	/** The load factory of the global ID cache fronting the chronicle cache */
	public final float idCacheLoadFactor;
	
	/** The initial capacity of the name cache fronting the chronicle cache */
	public final int nameCacheInitialCapacity;
	/** The load factory of the name cache fronting the chronicle cache */
	public final float nameCacheLoadFactor;
	
	/** The chronicle data bit size hint */
	public final int dataBitSizeHint;
	
	
	/** The initial capacity of the opaque key cache fronting the chronicle cache */
	public final int opaqueCacheInitialCapacity;
	/** The load factory of the opaque key cache fronting the chronicle cache */
	public final float opaqueCacheLoadFactor;
	

	/**
	 * Creates a new ChronicleConfiguration
	 */
	public ChronicleConfiguration() {		 
		idCacheInitialCapacity = ConfigurationHelper.getIntSystemThenEnvProperty(Constants.CHRONICLE_ID_CACHE_INITIAL_CAPACITY, Constants.DEFAULT_CHRONICLE_ID_CACHE_INITIAL_CAPACITY);
		idCacheLoadFactor = ConfigurationHelper.getFloatSystemThenEnvProperty(Constants.CHRONICLE_ID_CACHE_LOAD_FACTOR, Constants.DEFAULT_CHRONICLE_ID_CACHE_LOAD_FACTOR);

		nameCacheInitialCapacity = ConfigurationHelper.getIntSystemThenEnvProperty(Constants.CHRONICLE_NAME_CACHE_INITIAL_CAPACITY, Constants.DEFAULT_CHRONICLE_NAME_CACHE_INITIAL_CAPACITY);
		nameCacheLoadFactor = ConfigurationHelper.getFloatSystemThenEnvProperty(Constants.CHRONICLE_NAME_CACHE_LOAD_FACTOR, Constants.DEFAULT_CHRONICLE_NAME_CACHE_LOAD_FACTOR);
		
		opaqueCacheInitialCapacity = ConfigurationHelper.getIntSystemThenEnvProperty(Constants.CHRONICLE_OPAQUE_CACHE_INITIAL_CAPACITY, Constants.DEFAULT_CHRONICLE_OPAQUE_CACHE_INITIAL_CAPACITY);
		opaqueCacheLoadFactor = ConfigurationHelper.getFloatSystemThenEnvProperty(Constants.CHRONICLE_OPAQUE_CACHE_LOAD_FACTOR, Constants.DEFAULT_CHRONICLE_OPAQUE_CACHE_LOAD_FACTOR);
		dataBitSizeHint = UnsafeAdapter.findNextPositivePowerOfTwo(ConfigurationHelper.getIntSystemThenEnvProperty(Constants.CHRONICLE_DATASIZE_PROP, Constants.DEFAULT_CHRONICLE_DATASIZE));
		// Chronicle direct won't work unless UnsafeAdapter.FIVE_COPY is true.
		unsafe = UnsafeAdapter.FIVE_COPY ? ConfigurationHelper.getBooleanSystemThenEnvProperty(Constants.CHRONICLE_UNSAFE_PROP, Constants.DEFAULT_CHRONICLE_UNSAFE ) : false;
		dataDir = new File(ConfigurationHelper.getSystemThenEnvProperty(Constants.CHRONICLE_DIR, Constants.DEFAULT_CHRONICLE_DIR));
		if(!dataDir.exists()) {
			dataDir.mkdirs();
		}
		if(!dataDir.isDirectory()) {
			throw new IllegalArgumentException("The directory [" + dataDir + "] is not valid");
		}
		LOG.info("Chronicle Configuration:\n\tDirectory: {}\n\tDataBitSizeHint: {}\n\tUnsafe:{}\n", dataDir, dataBitSizeHint, unsafe);
	}

}
