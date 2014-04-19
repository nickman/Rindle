/**
 * 
 */
package org.helios.rindle.submit;

/**
 * <p>Title: ISubmit</p>
 * <p>Description:  Defines the datapoint submission interface to the Rindle core/p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.rindle.submit.ISubmit</code></b>
 */

public interface ISubmit {
	/**
	 * Submits an unidentified metric value
	 * @param name The metric name
	 * @param opaqueKey The opaque key
	 * @param value The value
	 * @return the global id
	 */
	public long submit(String name, byte[] opaqueKey, long value);
	/**
	 * Submits an unidentified metric value
	 * @param name The metric name
	 * @param opaqueKey The opaque key
	 * @param value The value
	 * @return the global id
	 */
	public long submit(String name, byte[] opaqueKey, double value);
	
	/**
	 * Submits an unidentified metric value
	 * @param name The metric name
	 * @param value The value
	 * @return the global id
	 */
	public long submit(String name, long value);
	
	/**
	 * Submits an unidentified metric value
	 * @param name The metric name
	 * @param value The value
	 * @return the global id
	 */
	public long submit(String name, double value);
	
	/**
	 * Submits an unidentified metric value
	 * @param opaqueKey The opaque key
	 * @param value The value
	 * @return the global id
	 */
	public long submit(byte[] opaqueKey, long value);
	
	/**
	 * Submits an unidentified metric value
	 * @param opaqueKey The opaque key
	 * @param value The value
	 * @return the global id
	 */
	public long submit(byte[] opaqueKey, double value);
	
	
	/**
	 * Submits an identified metric value
	 * @param globalId The global id of the metric
	 * @param value The value
	 * @return the global id
	 */
	public void submit(long globalId, long value);
	/**
	 * Submits an identified metric value
	 * @param globalId The global id of the metric
	 * @param value The value
	 * @return the global id
	 */
	public void submit(long globalId, double value);
}
