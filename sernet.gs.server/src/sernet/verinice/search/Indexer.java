/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.search;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.server.security.DummyAuthentication;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.gs.service.TimeFormatter;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class Indexer {

    private static final Logger LOG = Logger.getLogger(Indexer.class);
    
    private static final int DEFAULT_NUMBER_OF_THREADS = 8;
    private static final int SHUTDOWN_TIMEOUT_IN_SECONDS = 60;
    
    private IBaseDao<CnATreeElement, Integer> elementDao;

    private ExecutorService taskExecutor;
    private CompletionService<ActionResponse> completionService;
    
    private DummyAuthentication authentication = new DummyAuthentication(); 
    
    /**
     * Factory to create {@link IndexThread} instances
     * configured in veriniceserver-search.xml
     */
    private ObjectFactory indexThreadFactory;
    
    public void init() {
        index();
    }
    
    public void index() {
        long start = System.currentTimeMillis();
        taskExecutor = createExecutor();
        completionService = new ExecutorCompletionService<ActionResponse>(taskExecutor);
        boolean dummyAuthAdded = false;
        SecurityContext ctx = SecurityContextHolder.getContext(); 
        try {
            if(ctx.getAuthentication()==null) {
                ctx.setAuthentication(authentication);
                dummyAuthAdded = true;
            }
            ServerInitializer.inheritVeriniceContextState();
            
            doIndex();
            if (LOG.isInfoEnabled()) {
                long ms = System.currentTimeMillis() - start;
                LOG.info("Import finished, runtime: " + TimeFormatter.getHumanRedableTime(ms));
            }
        } catch (Exception e) {
            LOG.error("Error while indexing elements.", e);           
        } finally {
            if(dummyAuthAdded) {
                ctx.setAuthentication(null);
                dummyAuthAdded = false;
            }
            shutdownAndAwaitTermination(taskExecutor);
            if (LOG.isInfoEnabled()) {
                long ms = System.currentTimeMillis() - start;
                LOG.info("Import finished, runtime: " + TimeFormatter.getHumanRedableTime(ms));
            }
        } 
    }

    private void doIndex() throws InterruptedException, ExecutionException {
        List<CnATreeElement> elementList = getElementDao().findAll(RetrieveInfo.getPropertyInstance().setPermissions(true));
        if (LOG.isInfoEnabled()) {
            LOG.info("Elements: " + elementList.size() + ", start indexing...");
        }
        int n = 0;
        for (CnATreeElement element : elementList) {
            if(element!=null) {
                IndexThread thread = (IndexThread) indexThreadFactory.getObject();
                thread.setElement(element);
                completionService.submit(thread);
                n++;
            }
        }
        waitForObjectResults(n);
    }
    
    private void waitForObjectResults(int n) throws InterruptedException, ExecutionException {
        for (int i = 0; i < n; ++i) {
            if (LOG.isDebugEnabled()) {
                logVersion();
            }
            
        }
    }

    private void logVersion() throws InterruptedException, ExecutionException {
        ActionResponse response = completionService.take().get();
        long version = 0;
        String id = "unknown";
        if(response instanceof IndexResponse) {
            version = ((IndexResponse)response).getVersion();
            id = ((IndexResponse)response).getId();
        }
        if(response instanceof UpdateResponse) {
            version = ((UpdateResponse)response).getVersion();
            id = ((UpdateResponse)response).getId();
        }
        if(version>1) {
            LOG.debug("Version " + version + " for id: " + id);
        }
    }
    
    private ExecutorService createExecutor() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of threads: " + getMaxNumberOfThreads());
        }
        return Executors.newFixedThreadPool(getMaxNumberOfThreads());
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(getShutdownTimeoutInSeconds(), TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(getShutdownTimeoutInSeconds(), TimeUnit.SECONDS))
                    LOG.error("Thread pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private int getMaxNumberOfThreads() {
        return DEFAULT_NUMBER_OF_THREADS;
    }
    
    private int getShutdownTimeoutInSeconds() {
        return SHUTDOWN_TIMEOUT_IN_SECONDS;
    }
    
    public ObjectFactory getIndexThreadFactory() {
        return indexThreadFactory;
    }

    public void setIndexThreadFactory(ObjectFactory indexThreadFactory) {
        this.indexThreadFactory = indexThreadFactory;
    }

    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }
    
    
    
}
