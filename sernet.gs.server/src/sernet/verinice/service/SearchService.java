/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.search.IJsonBuilder;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.search.Occurence;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;
import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.search.IElementSearchDao;
import sernet.verinice.search.Indexer;
import sernet.verinice.search.JsonBuilder;

public class SearchService implements ISearchService {

    private static final Logger LOG = Logger.getLogger(SearchService.class);

    @Resource(name = "searchIndexer")
    protected Indexer searchIndexer;

    @Resource(name = "searchElementDao")
    protected IElementSearchDao searchDao;
    
    @Resource(name = "jsonBuilder")
    protected IJsonBuilder jsonBuilder;

    /**
     * Should be used by client to pass a query to the service in future
     * releases the method should decide which kind of query must be send to es.
     *
     * @see sernet.verinice.interfaces.search.ISearchService#query(sernet.verinice.model.search.VeriniceQuery)
     */
    @Override
    public VeriniceSearchResult query(VeriniceQuery veriniceQuery) {
        ServerInitializer.inheritVeriniceContextState();
        return query(veriniceQuery, null);
    }
    
    
    
    /**
     * Uses the es querybuilder api to build a query that could be paramterized
     * to search on given fields only, and adding filters for rightmanagement
     * and type-filtered results.
     *
     * @see sernet.verinice.interfaces.search.ISearchService#query(sernet.verinice.model.search.VeriniceQuery, java.lang.String)
     */
    @Override
    public VeriniceSearchResult query(VeriniceQuery query, String elementTypeId) {
        ServerInitializer.inheritVeriniceContextState();
        VeriniceSearchResult results = new VeriniceSearchResult();
        if (StringUtils.isNotEmpty(elementTypeId)) {
            results.addVeriniceSearchObject(processSearchResponse(elementTypeId, searchDao.find(elementTypeId, query), query.getLimit()));
        } else {
            for (EntityType type : HUITypeFactory.getInstance().getAllEntityTypes()) {
                results.addVeriniceSearchObject(processSearchResponse(type.getId(), searchDao.find(type.getId(), query), query.getLimit()));
            }
        }
        return results;
    }

    private String getEntityName(String typeID) {
        ServerInitializer.inheritVeriniceContextState();
        return HUITypeFactory.getInstance().getEntityType(typeID).getName();
    }

    private VeriniceSearchResultObject processSearchResponse(String elementTypeId, MultiSearchResponse msr, int limit) {
        List<SearchHit> hitList = createHitList(msr, limit);
        String identifier = "";
        VeriniceSearchResultObject results = new VeriniceSearchResultObject(elementTypeId, getEntityName(elementTypeId), getPropertyIds(elementTypeId));
        for (SearchHit hit : hitList) {
            identifier = hit.getId();
            Occurence occurence = createOccurence(elementTypeId, hit);
            VeriniceSearchResultRow result = new VeriniceSearchResultRow(results, identifier, occurence);

            for (String key : hit.getSource().keySet()) {
                if (hit.getSource().get(key) != null) {
                    result.addProperty(key, hit.getSource().get(key).toString());
                }
            }
            results.addSearchResult(result);

        }
        return results;
    }
    
    private Occurence createOccurence(String elementTypeId, SearchHit hit) {
        Iterator<Entry<String, HighlightField>> iter = hit.getHighlightFields().entrySet().iterator();
        Occurence occurence = new Occurence();
        while (iter.hasNext()) {
            Entry<String, HighlightField> entry = iter.next();
            for (Text textFragment : entry.getValue().fragments()){
                occurence.addFragment(entry.getKey(), getHuiTranslation(entry.getKey(), elementTypeId), textFragment.toString());
            }
        }
        return occurence;
    }

    private List<SearchHit> createHitList(MultiSearchResponse msr, int limit) {
        List<SearchHit> hitList = new ArrayList<SearchHit>(0);
        for (MultiSearchResponse.Item i : msr.getResponses()) {
            if (i != null && i.getResponse() != null && i.getResponse().getHits() != null) {
                for (SearchHit hit : i.getResponse().getHits().getHits()) {
                    hitList.add(hit);
                }
            }
        }
        if(limit > 0 && limit < hitList.size()){
            return limitList(limit, hitList);
        }
        return hitList;
    }



    /**
     * reduce size of Hitlist to given limit 
     * (cuts of all elements after position $limit )
     * @return
     */
    private List<SearchHit> limitList(int limit, List<SearchHit> hitList) {
        if(LOG.isDebugEnabled()){
            LOG.debug("Reducing elastic search result list of size:\t" + hitList.size() + " to " + limit + " elements");
        }
        SearchHit[] limitedHits = new SearchHit[limit];
        System.arraycopy(hitList.toArray(new SearchHit[hitList.size()]), 0, limitedHits, 0, limit);
        return Arrays.asList(limitedHits);
    }

    private String getHuiTranslation(String id, String entityType) {
        for (PropertyType type : HUITypeFactory.getInstance().getEntityType(entityType).getAllPropertyTypes()) {
            if (type.getId().equals(id)) {
                return type.getName();
            }
        }
        LOG.warn("No i8ln found for id:\t" + id + "\t of type:\t" + entityType);
        return id;
    }

    private String[] getPropertyIds(String typeID) {
        ServerInitializer.inheritVeriniceContextState();
        return HUITypeFactory.getInstance().getEntityType(typeID).getAllPropertyTypeIds();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.interfaces.search.ISearchService#index()
     */
    @Override
    public void index() {
        searchIndexer.init();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.interfaces.search.ISearchService#reindex()
     */
    @Override
    public void reindex() {
        searchDao.clear();
        searchIndexer.index();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#removeFromIndex(sernet
     * .verinice.model.common.CnATreeElement)
     */
    @Override
    public void remove(CnATreeElement element) {
        searchDao.delete(element.getUuid());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#addToIndex(sernet.verinice
     * .model.common.CnATreeElement)
     */
    @Override
    public void add(CnATreeElement element) {
        searchDao.index(element.getUuid(), getJsonBuilder().getJson(element));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#updateOnIndex(sernet
     * .verinice.model.common.CnATreeElement)
     */
    @Override
    public void update(CnATreeElement element) {
        searchDao.update(element.getUuid(), getJsonBuilder().getJson(element));
    }

    /**
     * @return the searchIndexer
     */
    public Indexer getSearchIndexer() {
        return searchIndexer;
    }

    /**
     * @param searchIndexer
     *            the searchIndexer to set
     */
    public void setSearchIndexer(Indexer searchIndexer) {
        this.searchIndexer = searchIndexer;
    }

    /**
     * @return the searchDao
     */
    public IElementSearchDao getSearchDao() {
        return searchDao;
    }

    /**
     * @param searchDao
     *            the searchDao to set
     */
    public void setSearchDao(IElementSearchDao searchDao) {
        this.searchDao = searchDao;
    }

    public IJsonBuilder getJsonBuilder() {
        return jsonBuilder;
    }

    public void setJsonBuilder(IJsonBuilder jsonBuilder) {
        this.jsonBuilder = jsonBuilder;
    }


}
