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
package sernet.verinice.search;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.omg.IOP.ServiceContext;
import org.omg.IOP.ServiceContextHolder;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.context.SecurityContextImpl;

import sernet.gs.server.security.DummyAuthentication;
import sernet.gs.service.ServerInitializer;

/**
 * Sets dummy authentication if needed. Indexing is sometimes called from the
 * server without using the spring security chain. If this happens, the context
 * is set by hand and cleaned afterwards from this every executing threads in
 * order not to pollute the threads with inconsistent security informations.
 *
 * Also it is possible, that this thread is called by a thread which clears the
 * security context after this pool is created. That's why we have to check
 * before a thread is executed, if the security context is still available.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
class IndexThreadPoolExecutor extends ThreadPoolExecutor {

    private DummyAuthentication dummyAuthentication = new DummyAuthentication();

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        initializeSecurityIndex();
    };

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        removeDummyAuthentication();
    }

    private IndexThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new IndexThreadFactory());
    }

    static public ThreadPoolExecutor newInstance(int nThreads) {
        return new IndexThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    private void initializeSecurityIndex() {
        if (isNoAuthenticationAvailable()) {
            SecurityContext ctx = new SecurityContextImpl();
            ctx.setAuthentication(dummyAuthentication);
            SecurityContextHolder.setContext(ctx);
        }
    }

    private boolean isNoAuthenticationAvailable() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private void removeDummyAuthentication() {
        if (dummyAuthentication == SecurityContextHolder.getContext().getAuthentication()) {
            SecurityContextHolder.clearContext();
        }
    };
}