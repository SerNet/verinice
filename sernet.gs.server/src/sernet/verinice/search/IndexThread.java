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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.server.security.DummyAuthenticatorCallable;
import sernet.gs.service.ServerInitializer;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.search.IJsonBuilder;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndexThread extends DummyAuthenticatorCallable<List<IndexedElementDetails>> {

    private IBaseDao<CnATreeElement, Integer> elementDao;
    private ISearchDao searchDao;
    private ISearchService searchService;
    private List<String> uuids;
    private IJsonBuilder jsonBuilder;
    private boolean logIndexedElementDetails = false;

    /*
     * @see sernet.verinice.search.DummyAuthenticatorCallable#doCall()
     */
    @Override
    public List<IndexedElementDetails> doCall() {
        String json = null;

        ServerInitializer.inheritVeriniceContextState();
        List<CnATreeElement> elements = loadElements();
        List<IndexedElementDetails> result = logIndexedElementDetails
                ? new ArrayList<>(elements.size())
                : null;
        Map<String, String> updateDetails = new HashMap<>(elements.size());
        for (CnATreeElement cnATreeElement : elements) {
            json = getJsonBuilder().getJson(cnATreeElement);

            if (json != null) {
                updateDetails.put(cnATreeElement.getUuid(), json);
                if (logIndexedElementDetails) {
                    result.add(new IndexedElementDetails(cnATreeElement.getUuid(),
                            cnATreeElement.getTitle()));
                }
            }
        }
        getSearchDao().updateOrIndex(updateDetails);
        return result;
    }

    private List<CnATreeElement> loadElements() {
        DetachedCriteria criteria = DetachedCriteria.forClass(CnATreeElement.class);
        criteria.add(Restrictions.in("uuid", uuids));
        criteria.setFetchMode("permissions", FetchMode.JOIN);
        criteria.setFetchMode("entity", FetchMode.JOIN);
        criteria.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
        criteria.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        return getElementDao().findByCriteria(criteria);
    }

    public void setUuids(List<String> uuids) {
        this.uuids = uuids;
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

    public void setReturnIndexedElementDetails(boolean logIndexedElementDetails) {
        this.logIndexedElementDetails = logIndexedElementDetails;

    }

}
