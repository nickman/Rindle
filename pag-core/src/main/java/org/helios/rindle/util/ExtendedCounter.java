/**
 * 
 */
package org.helios.rindle.util;

import org.cliffc.high_scale_lib.Counter;

/**
 * <p>Title: ExtendedCounter</p>
 * <p>Description:  Counter extension to return the counter on increment/decrement</p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.rindle.util.ExtendedCounter</code></b>
 */

public class ExtendedCounter extends Counter {

	/**  */
	private static final long serialVersionUID = -3298497673421399106L;

	/**
	 * Decrements the counter
	 * @return this counter
	 */
	public ExtendedCounter decr()   { 
		decrement();
		return this;
	}
	
	/**
	 * Increments the counter
	 * @return this counter
	 */
	public ExtendedCounter incr()   { 
		increment();
		return this;
	}
	
}
