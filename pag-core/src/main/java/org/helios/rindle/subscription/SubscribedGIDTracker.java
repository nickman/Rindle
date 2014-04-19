/**
 * 
 */
package org.helios.rindle.subscription;

import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.helios.rindle.util.ExtendedCounter;

/**
 * <p>Title: SubscribedGIDTracker</p>
 * <p>Description:  Tracks the subscribed global Ids that are subscribed to and how many subscribers are interested</p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.rindle.subscription.SubscribedGIDTracker</code></b>
 */

public class SubscribedGIDTracker {
	private static final ExtendedCounter DEFAULT = new ExtendedCounter();
	
	static {
		DEFAULT.decrement();
	}
	
	/** A map of the number of subscriptions to a global id keyed by the global id */
	protected final NonBlockingHashMapLong<ExtendedCounter> globalSubs = new NonBlockingHashMapLong<ExtendedCounter>(1028);
	
	/**
	 * Decrements the number of subscribers to the passed global id, removing it from tracking if it dropped to zero
	 * @param globalId The global id to track
	 */
	public void decrementGlobal(long globalId) {
		ExtendedCounter ec = globalSubs.get(globalId);
		if(ec!=null) {
			if(ec.decr().get()==0) {
				globalSubs.remove(globalId);
			}
		}
	}
	
	/**
	 * Increments the number of subscribers to the passed global id, adding it to tracking if it was zero
	 * @param globalId The global id to track
	 */
	public void incrementGlobal(long globalId) {
		if(globalSubs.putIfAbsent(globalId, DEFAULT)==null) {
			ExtendedCounter ec = new ExtendedCounter();
			globalSubs.replace(globalId, ec);
			ec.increment();
		}
	}
	
}
