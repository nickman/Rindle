/**
 * 
 */
package org.helios.rindle.submit;

import org.helios.rindle.control.Registry;
import org.helios.rindle.store.IStore;

/**
 * <p>Title: SubmitImpl</p>
 * <p>Description: The default submit implementation</p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><b><code>org.helios.rindle.submit.SubmitImpl</code></b>
 */

public class SubmitImpl implements ISubmit {
	protected final Registry registry;
	protected final IStore istore;
	
	
	/**
	 * Creates a new SubmitImpl 
	 * @param registry the underlying registry
	 * @param istore The istore implementation
	 */
	public SubmitImpl(Registry registry, IStore istore) {
		this.registry = registry;
		this.istore = istore;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.submit.ISubmit#submit(java.lang.String, byte[], long)
	 */
	@Override
	public long submit(String name, byte[] opaqueKey, long value) {
		long globalId = istore.getGlobalId(name, opaqueKey);
		registry.processValue(globalId, value);
		return globalId;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.submit.ISubmit#submit(java.lang.String, byte[], double)
	 */
	@Override
	public long submit(String name, byte[] opaqueKey, double value) {
		long globalId = istore.getGlobalId(name, opaqueKey);
		registry.processValue(globalId, value);
		return globalId;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.submit.ISubmit#submit(java.lang.String, long)
	 */
	@Override
	public long submit(String name, long value) {
		long globalId = istore.getGlobalId(name);
		registry.processValue(globalId, value);
		return globalId;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.submit.ISubmit#submit(java.lang.String, double)
	 */
	@Override
	public long submit(String name, double value) {
		long globalId = istore.getGlobalId(name);
		registry.processValue(globalId, value);
		return globalId;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.submit.ISubmit#submit(byte[], long)
	 */
	@Override
	public long submit(byte[] opaqueKey, long value) {
		long globalId = istore.getGlobalId(opaqueKey);
		registry.processValue(globalId, value);
		return globalId;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.submit.ISubmit#submit(byte[], double)
	 */
	@Override
	public long submit(byte[] opaqueKey, double value) {
		long globalId = istore.getGlobalId(opaqueKey);
		registry.processValue(globalId, value);
		return globalId;
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.submit.ISubmit#submit(long, long)
	 */
	@Override
	public void submit(long globalId, long value) {
		registry.processValue(globalId, value);
	}

	/**
	 * {@inheritDoc}
	 * @see org.helios.rindle.submit.ISubmit#submit(long, double)
	 */
	@Override
	public void submit(long globalId, double value) {
		registry.processValue(globalId, value);
	}

}
