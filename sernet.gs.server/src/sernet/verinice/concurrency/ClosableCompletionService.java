/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.concurrency;

import java.util.concurrent.CompletionService;

/**
 * Provides some convenient methods for rejecting new task within completion
 * service.
 * 
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public interface ClosableCompletionService<V> extends CompletionService<V> {

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are
     * executed, but no new tasks will be accepted. Invocation has no additional
     * effect if already shut down.
     *
     * @throws SecurityException
     *             if a security manager exists and shutting down this
     *             ExecutorService may manipulate threads that the caller is not
     *             permitted to modify because it does not hold
     *             {@link java.lang.RuntimePermission}<tt>("modifyThread")</tt>,
     *             or the security manager's <tt>checkAccess</tt> method denies
     *             access.
     */
    public void shutDown();

    /**
     * Is true if all tasks were finished after @link {@link #shutDown()} was
     * executed at least one time.
     * 
     */
    public boolean isClosed();
}
