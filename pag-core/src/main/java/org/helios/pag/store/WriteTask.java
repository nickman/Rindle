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
package org.helios.pag.store;

import com.higherfrequencytrading.chronicle.Excerpt;

/**
 * <p>Title: WriteTask</p>
 * <p>Description: A bit like a callable but task is passed a Chronicle {@link Excerpt} to execute the write task with.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.pag.store.WriteTask</code></p>
 * @param <T> The return type of the task
 */

public interface WriteTask<T> {
    /**
     * Executes a write task with the passed excerpt and returns the result, or throws an exception if unable to do so.
     * @param ex The excerpt to write with
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    T call(Excerpt ex) throws Exception;
}
