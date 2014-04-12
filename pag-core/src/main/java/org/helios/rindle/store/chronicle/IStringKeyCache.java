package org.helios.rindle.store.chronicle;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * <p>Title: IStringKeyCache</p>
 * <p>Description: Defines the String key cache used to decode a string metric name (String) to the global id (long).</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.store.IStringKeyCache</code></p>
 */
public interface IStringKeyCache extends IKeyCache {
	
	/** The default platform charset */
	public static final Charset CHARSET = Charset.defaultCharset();
	
	/**
	 * Determines if the cache contains the passed stringy value
	 * @param key The stringy to check for
	 * @return true if found, false otherwise
	 * @see gnu.trove.map.hash.TObjectLongHashMap#containsKey(java.lang.Object)
	 */
	public boolean containsKey(CharSequence key);


	/**
	 * Retrieves the long keyed by the passed stringy
	 * @param key The stringy key
	 * @return the located long or {@link #NO_ENTRY_VALUE}  if not found
	 * @see gnu.trove.map.hash.TObjectLongHashMap#get(java.lang.Object)
	 */
	public long get(CharSequence key);

	/**
	 * Inserts the passed key/value into the cache
	 * @param key The stringy key
	 * @param value The value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#put(java.lang.Object, long)
	 */
	public long put(CharSequence key, long value);

	/**
	 * Inserts the passed key/value into the cache if the key is not already bound
	 * @param key The stringy key
	 * @param value The value
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#putIfAbsent(java.lang.Object, long)
	 */
	public long putIfAbsent(CharSequence key, long value);

	/**
	 * Removes the mapping for a key from this map if it is present 
	 * @param key The stringy key
	 * @return the previous value associated with they key or {@link #NO_ENTRY_VALUE} if there was no mapping for the key.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#remove(java.lang.Object)
	 */
	public long remove(CharSequence key);

	/**
	 * Inserts the passed map of values into the cache
	 * @param map a map of stringy keys and long values
	 * @see gnu.trove.map.hash.TObjectLongHashMap#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends CharSequence, ? extends Long> map);
	
	/**
	 * Adjusts the primitive value mapped to the key if the key is present in the map.
	 * @param key The stringy key
	 * @param value The value
	 * @return true if a mapping was found and modified.
	 * @see gnu.trove.map.hash.TObjectLongHashMap#adjustValue(java.lang.Object, long)
	 */
	public boolean adjustValue(CharSequence key, long value);
	
}