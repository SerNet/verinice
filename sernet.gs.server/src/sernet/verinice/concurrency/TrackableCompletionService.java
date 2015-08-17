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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Wraps a Provides a {@link CompletionService} and a {@link ThreadPoolExecutor} 
 * and provides convenient methods for handling tasks.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
final public class TrackableCompletionService<V> implements ClosableCompletionService<V> {

    private final CompletionService<V> completionService;

    private final ThreadPoolExecutor threadPoolExecutor;

    private TrackableCompletionService() {
        threadPoolExecutor = VeriniceThreadPoolExecutor.newInstance();
        completionService = new ExecutorCompletionService<V>(threadPoolExecutor);
    }

    private TrackableCompletionService(String name) {
        threadPoolExecutor = VeriniceThreadPoolExecutor.newInstance(name);
        completionService = new ExecutorCompletionService<V>(threadPoolExecutor);
    }

    /*
     * @see
     * java.util.concurrent.CompletionService#submit(java.util.concurrent.Callable
     * )
     */
    @Override
    public Future<V> submit(Callable<V> task) {
        return completionService.submit(task);
    }

    /*
     * @see java.util.concurrent.CompletionService#submit(java.lang.Runnable,
     * java.lang.Object)
     */
    @Override
    public Future<V> submit(Runnable task, V result) {
        return completionService.submit(task, result);
    }

    /*
     * @see java.util.concurrent.CompletionService#take()
     */
    @Override
    public Future<V> take() throws InterruptedException {
        return completionService.take();
    }

    /*
     * @see java.util.concurrent.CompletionService#poll()
     */
    @Override
    public Future<V> poll() {
        return completionService.poll();
    }

    /*
     * @see java.util.concurrent.CompletionService#poll(long,
     * java.util.concurrent.TimeUnit)
     */
    @Override
    public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return completionService.poll(timeout, unit);
    }

    /*
     * @see sernet.verinice.search.ClosableCompletionService#shutDown()
     */
    @Override
    public void shutDown() {
        threadPoolExecutor.shutdown();
    }

    /*
     * @see sernet.verinice.search.ClosableCompletionService#isClosed()
     */
    @Override
    public boolean isClosed() {
        return threadPoolExecutor.isTerminated();
    }

    public static <V> ClosableCompletionService<V> newInstance() {
        return new TrackableCompletionService<V>();
    }

    /**
     * Returns new {@link ClosableCompletionService}.
     * 
     * @param name
     *            all worker threads will have this as prefix.
     */
    public static <V> ClosableCompletionService<V> newInstance(String name) {
        if (name == null) {
            new TrackableCompletionService<>();
        }
        return new TrackableCompletionService<V>(name);
    }

}
