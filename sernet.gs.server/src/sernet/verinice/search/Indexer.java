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
import java.util.concurrent.Callable;
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
import sernet.verinice.interfaces.IElementTitleCache;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class Indexer {

    private static final Logger LOG = Logger.getLogger(Indexer.class);
    
    private static final int DEFAULT_NUMBER_OF_THREADS = 8;
    private static final int SHUTDOWN_TIMEOUT_IN_SECONDS = 60;
    
    private IBaseDao<CnATreeElement, Integer> elementDao;
    private IElementTitleCache titleCache;

    private ExecutorService taskExecutor;
    private CompletionService<ActionResponse> completionService;
    
    private DummyAuthentication authentication = new DummyAuthentication(); 
    
    /**
     * Factory to create {@link IndexThread} instances
     * configured in veriniceserver-search.xml
     */
    private ObjectFactory indexThreadFactory;

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
            
            doIndex(start);
            if (LOG.isDebugEnabled()) {
                long ms = System.currentTimeMillis() - start;
                LOG.debug("All threads created, runtime: " + TimeFormatter.getHumanRedableTime(ms));
            }
        } catch (Exception e) {
            LOG.error("Error while indexing elements.", e);           
        } finally {
            if(dummyAuthAdded) {
                ctx.setAuthentication(null);
                dummyAuthAdded = false;
            }
            taskExecutor.shutdown();
        } 
    }

    private void doIndex(Long startTime) throws InterruptedException, ExecutionException {
        List<CnATreeElement> elementList = getElementDao().findAll(RetrieveInfo.getPropertyInstance().setPermissions(true));
        if (LOG.isInfoEnabled()) {
            LOG.info("Elements: " + elementList.size() + ", start indexing...");
        }
        getTitleCache().load(new String[] {ITVerbund.TYPE_ID_HIBERNATE, Organization.TYPE_ID});
        LastThread lastThread = new LastThread(startTime);
        int n = 0;
        for (CnATreeElement element : elementList) {
            if(element!=null) {
                IndexThread thread = (IndexThread) indexThreadFactory.getObject();
                thread.setElement(element);
                completionService.submit(thread);
                n++;
            }
        } 
        completionService.submit(lastThread);
    }

    private ExecutorService createExecutor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of threads: " + getMaxNumberOfThreads());
        }
        return Executors.newFixedThreadPool(getMaxNumberOfThreads());
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
    
    public IElementTitleCache getTitleCache() {
        return titleCache;
    }

    public void setTitleCache(IElementTitleCache titleCache) {
        this.titleCache = titleCache;
    }


    class LastThread implements Callable<ActionResponse> {

        long startTime;

        private LastThread(long startTime) {
            super();
            this.startTime = startTime;
        }

        /* (non-Javadoc)
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public ActionResponse call() throws Exception {            
            if (LOG.isInfoEnabled()) {
                long ms = System.currentTimeMillis() - startTime;
                String message = "Indexing finished, runtime: " + TimeFormatter.getHumanRedableTime(ms);
                LOG.info(message);
            }
            return null;
        }
        
    }
    
}
