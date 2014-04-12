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
package org.helios.rindle.store.redis;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.rindle.util.SystemClock;
import org.helios.rindle.util.SystemClock.ElapsedTime;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Pipeline;

/**
 * <p>Title: RedisTest</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.redis.RedisTest</code></p>
 */

public class RedisTest {
	private static final Logger log = LogManager.getLogger(RedisTest.class);
	
	static final int KEYS = 1000;
	static boolean PIPE = true;
	
	static final byte[] GID = "GID".getBytes();
	static final byte[] NAME = "NAME".getBytes();
	static final byte[] OPAQUE = "OP".getBytes();
	static final byte[] star = "*".getBytes();
	static final byte[] GIDCOUNTER = "gidcounter".getBytes();
	
	
	/** Redis client */
	protected BinaryJedis jedis;
	
	int loops = 100;
	
	protected final ThreadLocal<ByteBuffer> longConverter = new ThreadLocal<ByteBuffer>() {
		@Override
		protected ByteBuffer initialValue() {
			return ByteBuffer.allocate(8);
		}
	};
	
	protected byte[] longToBytes(long key) {
		return longConverter.get().putLong(0, key).array();
	}
	
	private static class ByteArrayHolder {
		final static Random R = new Random(System.currentTimeMillis());
		final byte[] op = new byte[32];
		ByteArrayHolder() {
			R.nextBytes(op);
		}
		/**
		 * {@inheritDoc}
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(op);
			return result;
		}
		/**
		 * {@inheritDoc}
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ByteArrayHolder other = (ByteArrayHolder) obj;
			if (!Arrays.equals(op, other.op))
				return false;
			return true;
		}
		
		
	}
	
	final Charset CHARSET = Charset.defaultCharset();
	
	/**
	 * Creates a new RedisTest
	 */
	public RedisTest(String host, int port) {
		log.info("Redis Test");
		jedis = new BinaryJedis(host, port);
		log.info("Connected");
		jedis.flushDB();
		
		Set<String> uuids = new HashSet<String>(KEYS);
		Set<ByteArrayHolder> opaques = new HashSet<ByteArrayHolder>(KEYS);
		byte[] opa = new byte[32];
		for(int i = 0; i < KEYS; i++) {
			uuids.add(UUID.randomUUID().toString());	
			opaques.add(new ByteArrayHolder());
		}
		log.info("Opaque Key Count:" + opaques.size());
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		byte[] buff = new byte[1024];
		int bytesRead = -1;
		try {
//			is = getClass().getClassLoader().getResourceAsStream("scripts/lua/processNameOpaque.lua");
			is = new FileInputStream("/home/nwhitehead/hprojects/Rindle/pag-core/src/main/resources/scripts/lua/processNameOpaque.lua");
			baos = new ByteArrayOutputStream(is.available());
			while((bytesRead = is.read(buff))!=-1) {
				baos.write(buff, 0, bytesRead);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Failed to read lua",ex);
		} finally {
			if(is!=null) try { is.close(); } catch (Exception ex) {}
		}
		byte[] script = baos.toByteArray();
		log.info("Bytes Read: {}", script.length);
		MessageDigest crypt = null;
		try {
			crypt = MessageDigest.getInstance("SHA1");
		} catch (Exception ex) {
			throw new RuntimeException("Failed to SHA1T !",ex);
		}
		crypt.reset();
		crypt.update(script);
		byte[] sha1 = crypt.digest();
		Formatter formatter = new Formatter();
		for(byte b: sha1) {
			formatter.format("%02x", b);
		}
		final String shaStr = formatter.toString();
		formatter.close();
		Pipeline pipe = jedis.pipelined();
		jedis.eval(script);
		jedis.evalsha(shaStr.getBytes());
		final byte[] shaBytes = shaStr.getBytes();
		log.info("Sha: {}", shaStr);
		ElapsedTime et = SystemClock.startClock();
		Iterator<ByteArrayHolder> iter = opaques.iterator();
		List<String> EMPTY = Collections.emptyList();
		for(String s: uuids) {
			byte[] m = s.getBytes();
			byte[] o = iter.next().op;
//			byte[] o = null;
//			jedis.evalsha(shaBytes, Arrays.asList(m, o), null);
//			jedis.evalsha(shaBytes, Arrays.asList(m), null);
			pipe.evalsha(shaStr, Arrays.asList(new String(m, CHARSET), new String(o, CHARSET)), EMPTY);			
		}
		pipe.sync();
		
		log.info("Keys: {}\n\t{}", jedis.dbSize(), et.printAvg("Inserts", uuids.size()));
//		Map<byte[], byte[]> hash = new HashMap<byte[], byte[]>(2);
//		
//		log.info("Starting Warmup");
//		Pipeline pipe = jedis.pipelined();
//		
//		for(int i = 0 ; i < loops; i++) {			
//			long key = -1L;
//			if(PIPE) {
//				Response<Long> rez = pipe.incrBy(GIDCOUNTER, KEYS);
//				pipe.sync();
//				key = rez.get();
//			} else {
//				key = 0;
//			}
//			Iterator<ByteArrayHolder> iter = opaques.iterator();
//			for(String s: uuids) {
//				key = PIPE ? --key : jedis.incr(GIDCOUNTER);
//				byte[] bkey = longToBytes(key);
//				byte[] nkey = s.getBytes(CHARSET);
//				byte[] opak = iter.next().op;
//				
//				
//				hash.put(NAME, nkey);
//				hash.put(OPAQUE, opak);
//				if(PIPE) {
//					pipe.hmset(bkey, hash);
//					pipe.set(opak, bkey);
//					pipe.set(nkey, bkey);
//				} else {
//					jedis.hmset(bkey, hash);
//					jedis.set(opak, bkey);
//					jedis.set(nkey, bkey);
//				}
//			}
//			if(PIPE) {
//				pipe.sync();
//			}
//			if(PIPE) {
//				pipe.flushDB(); pipe.sync();
//			} else {
//				jedis.flushDB();
//			}
//			if(i%10==0) log.info("Warmup loop #{}", i);
//			
//		}
//		
//		
//		
//		
//		
//		log.info("Starting RindleMain Key Insert");
//		ElapsedTime et = SystemClock.startClock();
//		long key = -1L;
//		if(PIPE) {
//			Response<Long> rez = pipe.incrBy(GIDCOUNTER, KEYS);
//			pipe.sync();
//			key = rez.get();
//		} else {
//			key = 0;
//		}
//		final long TOPKEY = key;
//		log.info("Starting Key: {}", key);
//		Iterator<ByteArrayHolder> iter = opaques.iterator();
//		for(String s: uuids) {
//			key = PIPE ? --key : jedis.incr(GIDCOUNTER);			
//			byte[] bkey = longToBytes(key);
//			byte[] nkey = s.getBytes(CHARSET);
//			byte[] opak = iter.next().op;
//			
//			
//			hash.put(NAME, nkey);
//			hash.put(OPAQUE, opak);
//			if(PIPE) {
//				pipe.hmset(bkey, hash);
//				pipe.set(opak, bkey);
//				pipe.set(nkey, bkey);
//			} else {
//				jedis.hmset(bkey, hash);
//				jedis.set(opak, bkey);
//				jedis.set(nkey, bkey);
//			}
//		}
//		if(PIPE) {
//			pipe.sync();
//		}
//		
//		iter = opaques.iterator();
//		int misses = 0, hits = 0;
//		key=0;
//		for(String s: uuids) {
//			byte[] bkey = longToBytes(key);
//			byte[] nkey = s.getBytes(CHARSET);
//			byte[] opak = iter.next().op;
//
//			if(jedis.exists(bkey)) hits++;
//			else misses++;
//
//			
//			if(jedis.exists(nkey)) hits++;
//			else misses++;
//			
//			if(jedis.exists(opak)) hits++;
//			else misses++;
//			
//			key++;
//		}
//		
//		log.info("Verification. Hits: {}, Misses: {}", hits, misses);
////		if(PIPE) {
////			pipe.flushDB(); pipe.sync();
////		} else {
////			jedis.flushDB();
////		}		
//		log.info("Complete.(Pipeline: {}) Last Key: {}\n\t {}", PIPE, key, et.printAvg("Key Inserts", KEYS));
	}
	
	public void close() {
		try { jedis.close(); } catch (Exception x) {}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RedisTest test = null;
		try {
//			test = new RedisTest("10.12.114.48", 6379);
			test = new RedisTest("localhost", 6379);
		} finally {
			if(test!=null) test.close();
		}

	}
	
	/*
	 * Lua Master Processor
	 * ====================
	 * ARGS: metricName, opaqueKey
	 * 	if(metricName!=null) mid = get(metricName)
	 * 	if(opaqueKey!=null) oid = get(opaqueKey)
	 * 
	 * 	Conditions:
	 * 		both null: error
	 * 		one null:
	 * 			id found: return
	 * 			else save, return new id
	 *  	neither null:
	 *  		both ids found:
	 *  			ids equal: return id
	 *  			ids diff: <???>
	 *  		neither id found:  save both under new id and return new id
	 *  		one id found:
	 *  			update for missing, return id
	 */

}
