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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Inits as many workers as processors are available.
 *
 * <p>The max pooling size is <pre>2 * |procoessors|</pre></p>
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class VeriniceThreadPoolExecutor extends ThreadPoolExecutor {

    private static final int DEFAULT_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();

    private static final String VERINICE_THREADS_POOL = "verinice-thread-pool"; 

    public VeriniceThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new CustomNamedThreadGroupFactory(poolName));
    }

    static public ThreadPoolExecutor newInstance() {
        return new VeriniceThreadPoolExecutor(VERINICE_THREADS_POOL, DEFAULT_NUMBER_OF_THREADS, 2 * DEFAULT_NUMBER_OF_THREADS, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }
    
    static public ThreadPoolExecutor newInstance(String poolName) {
        return new VeriniceThreadPoolExecutor(poolName, DEFAULT_NUMBER_OF_THREADS, 2 * DEFAULT_NUMBER_OF_THREADS, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

}
