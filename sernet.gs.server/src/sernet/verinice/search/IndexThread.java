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

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionResponse;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.server.security.DummyAuthentication;
import sernet.gs.service.ServerInitializer;
import sernet.gs.service.TimeFormatter;
import sernet.verinice.interfaces.search.IJsonBuilder;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;
import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.model.search.VeriniceSearchResultRow;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndexThread implements Callable<ActionResponse> {

    private static final Logger LOG = Logger.getLogger(IndexThread.class);
    
    private ISearchDao searchDao;  
    private ISearchService searchService;
    private CnATreeElement element;  
    private IJsonBuilder jsonBuilder;
   
    public IndexThread() {
        super();
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ActionResponse call() throws Exception {
        try { 
            ServerInitializer.inheritVeriniceContextState();
            String json = getJsonBuilder().getJson(element);
                   
            ActionResponse response = getSearchDao().updateOrIndex(element.getUuid(), json);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Element indexed: " + element.getUuid());
            } 
            return response;
        } catch(Exception e ) {
            String uuid = (element!=null) ? element.getUuid() : null;
            LOG.error("Error while indexing element: " + uuid, e);
            return null;
        } 
     }
    
    public CnATreeElement getElement() {
        return element;
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public ISearchDao getSearchDao() {
        return searchDao;
    }

    public void setSearchDao(ISearchDao searchDao) {
        this.searchDao = searchDao;
    }

    public ISearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(ISearchService searchService) {
        this.searchService = searchService;
    }

    public IJsonBuilder getJsonBuilder() {
        return jsonBuilder;
    }

    public void setJsonBuilder(IJsonBuilder jsonBuilder) {
        this.jsonBuilder = jsonBuilder;
    }


}
