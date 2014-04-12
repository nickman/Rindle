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
package test.client;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.ConcurrentHashMap;

import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.helios.rindle.util.unsafe.UnsafeAdapter;

/**
 * <p>Title: TriCache</p>
 * <p>Description: Emulates the cache that an OpenTSDB Rindle client will use to create a tri-directional map association between:<ul>
 * 	<li>The 64bit int metric global id</li>
 * 	<li>The fully qualified metric name</li>
 * 	<li>The tsuid byte array that represents the time-series</li>
 * </ul></p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.client.TriCache</code></p>
 */

public class TriCache {

	protected final NonBlockingHashMapLong<String> idToName;
	protected final NonBlockingHashMapLong<ByteBuffer> idToTsuid;
	protected final ConcurrentHashMap<ByteBuffer, Long> tsuidToId;
	protected final ConcurrentHashMap<CharBuffer, Long> nameToId;
	
	/**
	 * Creates a new TriCache
	 */
	public TriCache() {
		idToName = new NonBlockingHashMapLong<String>();
		idToTsuid = new NonBlockingHashMapLong<ByteBuffer>();
		tsuidToId = new ConcurrentHashMap<ByteBuffer, Long>();
		nameToId = new ConcurrentHashMap<CharBuffer, Long>();
	}
	
	static class A {
		private static final Long l = 94L;
	}
	
	static class B {
		private static final long[] l = new long[]{94L};
	}
	
	
	public static void main(String[] args) {		
		log("Long: ais:%s", UnsafeAdapter.arrayIndexScale(A.class));
		log("long[]: ais:%s", UnsafeAdapter.arrayIndexScale(B.class));
	}

	public static void log(Object format, Object...args) {
		System.out.println(String.format(format.toString(), args));
	}
}
