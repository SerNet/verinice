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

import org.apache.log4j.Logger;

import sernet.gs.server.security.DummyAuthenticatorCallable;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.search.IJsonBuilder;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndexThread extends DummyAuthenticatorCallable<CnATreeElement> {

    private static final Logger LOG = Logger.getLogger(IndexThread.class);

    private static final RetrieveInfo RI = RetrieveInfo.getPropertyInstance().setPermissions(true);

    private IBaseDao<CnATreeElement, Integer> elementDao;
    private ISearchDao searchDao;
    private ISearchService searchService;
    private CnATreeElement element;
    private String uuid;
    private IJsonBuilder jsonBuilder;

    public IndexThread(){}

    /*
     * @see sernet.verinice.search.DummyAuthenticatorCallable#doCall()
     */
     @Override
     public CnATreeElement doCall() {
         String json = null;

         ServerInitializer.inheritVeriniceContextState();
         json = getJsonBuilder().getJson(getElement());

         if (json != null) {
             getSearchDao().updateOrIndex(element.getUuid(), json);
         }

         return element;
     }
    
    public CnATreeElement getElement() {
        if (element == null) {
            element = loadElement();
        }
        return element;
    }

    private CnATreeElement loadElement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading element with uuid: " + getUuid() + "...");
        }

        if (getUuid() != null) {
            element = loadElementByDao(getUuid());
        }

        return element;
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

}
