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
package redis.clients.nedis.netty;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * <p>Title: QueueFactory</p>
 * <p>Description: This factory should be used to create the "optimal" {@link BlockingQueue} instance for the running JVM.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>redis.clients.nedis.netty.QueueFactory</code></p>
 */

public class QueueFactory {

    /**
     * Create a new unbound {@link BlockingQueue} 
     * @param itemClass  the {@link Class} type which will be used as {@link BlockingQueue} items
     * @return queue     the {@link BlockingQueue} implementation
     */
    public static <T> BlockingQueue<T> createQueue(Class<T> itemClass) {
            return new LinkedTransferQueue<T>();
    }
    
    /**
     * Create a new unbound {@link BlockingQueue} 
     * @param collection  the collection which should get copied to the newly created {@link BlockingQueue}
     * @param itemClass   the {@link Class} type which will be used as {@link BlockingQueue} items
     * @return queue      the {@link BlockingQueue} implementation
     */
    public static <T> BlockingQueue<T> createQueue(Collection<? extends T> collection, Class<T> itemClass) {
        return new LinkedTransferQueue<T>(collection);
    }    
    
	
	/**
	 * Creates a new QueueFactory
	 */
	private QueueFactory() {
	}

}
