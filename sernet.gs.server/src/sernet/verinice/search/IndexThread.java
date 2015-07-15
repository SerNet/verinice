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

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionResponse;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.server.security.DummyAuthentication;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.search.IJsonBuilder;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndexThread implements Callable<CnATreeElement> {

    private static final Logger LOG = Logger.getLogger(IndexThread.class);
    
    private static final RetrieveInfo RI = RetrieveInfo.getPropertyInstance().setPermissions(true);
    
    private IBaseDao<CnATreeElement, Integer> elementDao;
    private ISearchDao searchDao;  
    private ISearchService searchService;
    private CnATreeElement element;
    private String uuid;
    private IJsonBuilder jsonBuilder;
    
    private DummyAuthentication authentication = new DummyAuthentication();
    private SecurityContext ctx;
    private boolean dummyAuthAdded;
   
    public IndexThread() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public CnATreeElement call() throws Exception {
        String json = null;

        ServerInitializer.inheritVeriniceContextState();
        json = getJsonBuilder().getJson(getElement());
        ActionResponse response = null;
        if (json != null) {
            response = getSearchDao().updateOrIndex(element.getUuid(), json);
        }
        return element;

    }
    
    public CnATreeElement getElement() {
        if(element==null) {            
            element = loadElement();
        }
        return element;
    }


    private CnATreeElement loadElement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading element with uuid: " + getUuid() + "...");
        }       
        try {
            initializeSecurityIndex();
            if(getUuid()!=null) {
                element = loadElementByDao(getUuid());
            }        
            return element;
        } finally {
            removeDummyAuthentication();
        }
    }

    private CnATreeElement loadElementByDao(String uuid) {
        return getElementDao().findByUuid(getUuid(), RI);
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
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
    
    private void initializeSecurityIndex() {
        dummyAuthAdded = false;
        ctx = SecurityContextHolder.getContext();

        if (ctx.getAuthentication() == null) {
            ctx.setAuthentication(authentication);
            dummyAuthAdded = true;
        }
    }
    
    private void removeDummyAuthentication() {
        if (dummyAuthAdded) {
            ctx.setAuthentication(null);
            dummyAuthAdded = false;
        }
    }


}
