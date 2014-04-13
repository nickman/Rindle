/**
 * 
 */
package org.helios.rindle.store.redis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.helios.rindle.RindleService;

import com.google.common.util.concurrent.AbstractService;

/**
 * <p>Title: ScriptControl</p>
 * <p>Description: Loads and manages Redis lua scripts for the Rindle service.</p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.rindle.store.redis.ScriptControl</code></b>
 */

public class ScriptControl extends AbstractService implements RindleService {
	
	/** The resource root for the util lua scripts */
	public static final String UTIL_SCRIPT_BASE = "scripts/lua/util";
	/** The resource root for the lua scripts */
	public static final String SCRIPT_BASE = "scripts/lua/";
	/** The format specifier for formatting the SHA1 bytes */
	public static final String SHA_FORMATTER = "%02x";
	/** The SHA1 message digest */
	private static final MessageDigest SHA_CRYPT;
	
	/** A map of script content keyed by the unqualified resource name (the file name) */
	protected Map<String, byte[]> scripts = new ConcurrentHashMap<String, byte[]>();
	/** A map of script content SHA1 hashes keyed by the unqualified resource name (the file name) */
	protected Map<String, byte[]> sha1s = new ConcurrentHashMap<String, byte[]>();
	/** A map of script content SHA1 hashes in String format (for pipeline calls) keyed by the unqualified resource name (the file name) */
	protected Map<String, String> sha1strs = new ConcurrentHashMap<String, String>();
	
	/** The redis connection pool for loading and invoking the redis lua scripts */
	protected final RedisConnectionPool connectionPool;
	
	/** Instance logger */
	protected final Logger log = LogManager.getLogger(getClass());
	
	
	
	/**
	 * Creates a new ScriptControl for Redis Lua scripts
	 * @param connectionPool the connection pool
	 */
	public ScriptControl(RedisConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
	}

	/**
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStart()
	 */
	@Override
	protected void doStart() {
		log.info("Starting ScriptControl....");
		loadScriptsFrom(UTIL_SCRIPT_BASE);
		loadScriptsFrom(SCRIPT_BASE);
		List<Long> scriptCheck = connectionPool.redisTask(new RedisTask<List<Long>>(){
			@Override
			public List<Long> redisTask(ExtendedJedis jedis) throws Exception {
				return jedis.scriptExists(sha1s.values().toArray(new byte[sha1s.size()][]));
			}
		});		
		Long[] checkResults = new Long[scriptCheck.size()];
		checkResults = scriptCheck.toArray(checkResults);
		Arrays.sort(checkResults);
		if(checkResults[0]<1) {
			log.warn("Script Check Failed: {}", Arrays.toString(checkResults));
		}
		notifyStarted();
		log.info("ScriptControl Started");
	}
	
	/**
	 * Loads lua scripts from the specified resource base
	 * @param base The resource base to load from
	 */
	protected void loadScriptsFrom(String base) {
		log.info("Loading Script Base [{}]", base);
		String[] scriptNames = getResourceListing(getClass(), base);
		if(log.isDebugEnabled()) log.debug("Loading scripts: {}", Arrays.toString(scriptNames));
		for(final String scriptName: scriptNames) {
			if(!scriptName.toLowerCase().endsWith(".lua")) continue;
			final byte[] scriptBytes = loadScript(base, scriptName);
			final String shaStr = formatSha(shaDigest(scriptBytes));
			final byte[] sha = shaStr.getBytes();
			scripts.put(scriptName, scriptBytes);
			sha1strs.put(scriptName, shaStr);
			sha1s.put(scriptName, sha);
			connectionPool.redisTask(new RedisTask<Void>(){
				@Override
				public Void redisTask(ExtendedJedis jedis) throws Exception {
					try {
						jedis.scriptLoad(scriptBytes);
						jedis.evalsha(sha);
						return null;
					} catch (Exception ex) {
						log.error("Failed to load script [{}]. Script follows.\n{}\n=====END SCRIPT======", scriptName, new String(scriptBytes));
						throw ex;
					}
				}
			});
			log.info("Loaded script: {}", scriptName);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see com.google.common.util.concurrent.AbstractService#doStop()
	 */
	@Override
	protected void doStop() {
		/* Nothing to do */
	}
	
	
	static {
		try {
			SHA_CRYPT = MessageDigest.getInstance("SHA1");
		} catch (Exception ex) {
			throw new RuntimeException("Failed to SHA1T !",ex);
		}
		
	}
	
	public static void main(String[] args) {
		log("Script List Test");
		String[] scripts = getResourceListing(ScriptControl.class, SCRIPT_BASE);
		log("Scripts:" + Arrays.toString(scripts));
	}
	
	/**
	 * Hex formats a SHA1 digest
	 * @param digest The SHA1 digest to format
	 * @return the formatted string 
	 */
	public static String formatSha(byte[] digested) {
		Formatter formatter = null;
		try {
			formatter = new Formatter();
			for(byte b: digested) {
				formatter.format("%02x", b);
			}
			return formatter.toString();
		} finally {
			if(formatter!=null) try { formatter.close(); } catch (Exception x) {/* No Op */}
		}
	}
	
	/**
	 * Hashes the passed script content bytes
	 * @param scriptBytes The Lua script bytes
	 * @return the hashed bytes
	 */
	public static synchronized byte[] shaDigest(byte[] scriptBytes) {
		SHA_CRYPT.reset();
		SHA_CRYPT.update(scriptBytes);
		return SHA_CRYPT.digest();
	}
	
		
	/**
	 * Loads the named script and returns its content in a byte array
	 * @param base The resource base to load from
	 * @param name The resource name (file name) of the lua script to load
	 * @return the bytes of the script
	 */
	public byte[] loadScript(String base, String name) {
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		try {
			byte[] buff = new byte[1024];
			int bytesRead = -1;
			try {
				is = getClass().getClassLoader().getResourceAsStream(String.format("%s/%s", base, name));
				baos = new ByteArrayOutputStream(is.available());
				while((bytesRead = is.read(buff))!=-1) {
					baos.write(buff, 0, bytesRead);
				}
			} catch (Exception ex) {
				throw new RuntimeException("Failed to read lua",ex);
			} finally {
				if(is!=null) try { is.close(); } catch (Exception ex) {}
			}
			return baos.toByteArray();			
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load script [" + name + "]", ex);
		} finally {
			if(baos!=null) try { baos.close(); } catch (Exception x) {/* No Op */}
			if(is!=null) try { is.close(); } catch (Exception x) {/* No Op */}
		}
	}
	
	public static void log(Object msg) { System.out.println(msg); }
	  /**
	   * List directory contents for a resource folder. Not recursive.
	   * This is basically a brute-force implementation.
	   * Works for regular files and also JARs.
	   * From <a href="http://www.uofr.net/~greg/java/get-resource-listing.html">Greg Briggs</a> 
	   * @author Greg Briggs
	   * @param clazz Any java class that lives in the same place as the resources you want.
	   * @param path Should end with "/", but not start with one.
	   * @return Just the name of each member item, not the full paths.
	   * @throws URISyntaxException 
	   * @throws IOException 
	   */
	  public static String[] getResourceListing(Class<?> clazz, String path) {
		  try {
		      URL dirURL = clazz.getClassLoader().getResource(path);
		      if (dirURL != null && dirURL.getProtocol().equals("file")) {
		        /* A file path: easy enough */
		        return new File(dirURL.toURI()).list();
		      } 
	
		      if (dirURL == null) {
		        /* 
		         * In case of a jar file, we can't actually find a directory.
		         * Have to assume the same jar as clazz.
		         */
		        String me = clazz.getName().replace(".", "/")+".class";
		        dirURL = clazz.getClassLoader().getResource(me);
		      }
		      
		      if (dirURL.getProtocol().equals("jar")) {
		        /* A JAR path */
		        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
		        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
		        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
		        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
		        while(entries.hasMoreElements()) {
		          String name = entries.nextElement().getName();
		          if (name.startsWith(path)) { //filter according to the path
		            String entry = name.substring(path.length());
		            int checkSubdir = entry.indexOf("/");
		            if (checkSubdir >= 0) {
		              // if it is a subdirectory, we just return the directory name
		              entry = entry.substring(0, checkSubdir);
		            }
		            result.add(entry);
		          }
		        }
		        return result.toArray(new String[result.size()]);
		      } 
		        
		      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
		  } catch (Exception ex) {
			  throw new RuntimeException("Failed to list scripts",ex);
		  }
	  }

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.RindleService#getDependentServices()
	 */
	@Override
	public Collection<RindleService> getDependentServices() {
		return Collections.emptyList();
	}

}
