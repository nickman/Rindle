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
package org.helios.rindle.subscription;

import org.helios.rindle.subscription.criteria.DataContent;
import org.helios.rindle.subscription.criteria.Delivery;
import org.helios.rindle.subscription.criteria.Format;
import org.helios.rindle.subscription.criteria.Interval;
import org.helios.rindle.subscription.criteria.Retention;

/**
 * <p>Title: ISubscription</p>
 * <p>Description: Defines a subscription to rindle data feeds and events</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.rindle.subscription.ISubscription</code></p>
 */

public interface ISubscription {
	public Retention getRetention();
	public Format getFormat();
	public Interval getInterval();
	public Delivery getDelivery();
	public DataContent[] getDataContents();
	
	public long[] getSpecifiedGlobalIds();
	public long[] getAllGlobalIds();
	public String[] getPatterns();
}
