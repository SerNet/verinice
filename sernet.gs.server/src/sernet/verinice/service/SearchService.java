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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;
import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.search.IElementSearchDao;
import sernet.verinice.search.Indexer;
import sernet.verinice.search.JsonBuilder;

/**
 *
 */
public class SearchService implements ISearchService {

    private static final Logger LOG = Logger.getLogger(SearchService.class);

    @Resource(name = "searchIndexer")
    protected Indexer searchIndexer;

    @Resource(name = "searchElementDao")
    protected IElementSearchDao searchDao;
    
    @Resource(name ="configurationService")
    protected IConfigurationService configurationService;
    
    @Resource(name = "authService")
    protected IAuthService authenticationService;

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#query(sernet.verinice
     * .model.search.VeriniceQuery)
     */
    @Override
    public VeriniceSearchResult query(VeriniceQuery veriniceQuery) {
        ServerInitializer.inheritVeriniceContextState();
        // return executeSimpleQuery(veriniceQuery.getQuery());
        return getSearchResultsByQueryBuilder(veriniceQuery, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#exportSearchResultToCsv
     * (sernet.verinice.model.search.VeriniceSearchResultRow)
     */
    @Override
    public File exportSearchResultToCsv(VeriniceSearchResultObject result) {
        return null;
    }

    @Override
    public VeriniceSearchResult executeSimpleQuery(String query) {
        ServerInitializer.inheritVeriniceContextState();
        VeriniceSearchResult veriniceSearchResult = new VeriniceSearchResult();
        for (EntityType type : HUITypeFactory.getInstance().getAllEntityTypes()) {

            String typeId = type.getId();
            VeriniceSearchResultObject result = getSearchResults(query, typeId);

            if (result.getHits() > 0) {
                veriniceSearchResult.addVeriniceSearchObject(result);
            }
        }

        return veriniceSearchResult;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#getSearchResults(java
     * .lang.String, java.lang.String)
     */
    @Override
    public VeriniceSearchResultObject getSearchResults(String query, String typeID) {
        ServerInitializer.inheritVeriniceContextState();
        SearchHits hits = searchDao.findByPhrase(query, typeID).getHits();
        String identifier = "";
        VeriniceSearchResultObject results = new VeriniceSearchResultObject(typeID, getEntityName(typeID), getPropertyIds(typeID));
        for (SearchHit hit : hits.getHits()) {
            identifier = hit.getId();
            StringBuilder occurence = new StringBuilder();
            Iterator<Entry<String, HighlightField>> iter = hit.getHighlightFields().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, HighlightField> entry = iter.next();
                occurence.append("[" + entry.getKey() + "]");
                occurence.append("\t").append(entry.getValue().fragments()[0]);
                if (iter.hasNext()) {
                    occurence.append("\n\n\n");
                }
            }

            VeriniceSearchResultRow result = new VeriniceSearchResultRow(results, identifier, occurence.toString());
            for (String key : hit.getSource().keySet()) {
                if (hit.getSource().get(key) != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("adding <" + key + ", " + hit.getSource().get(key).toString() + "> to properties of result");
                    }
                    result.addProperty(key, hit.getSource().get(key).toString());
                }
            }
            results.addSearchResult(result);

        }
        return results;
    }


    private String getEntityName(String typeID) {
        ServerInitializer.inheritVeriniceContextState();
        return HUITypeFactory.getInstance().getEntityType(typeID).getName();
    }


    /**
     * method to experiment with different query builders
     *
     * @param query
     * @param typeID
     * @return
     */
    @Override
    public VeriniceSearchResult getSearchResultsByQueryBuilder(VeriniceQuery query, String typeID) {
        VeriniceSearchResult results = new VeriniceSearchResult();
        if (StringUtils.isNotEmpty(typeID)) {
            addVeriniceSearchResultObject(query, typeID, results);
        } else {
            for (EntityType type : HUITypeFactory.getInstance().getAllEntityTypes()) {
                addVeriniceSearchResultObject(query, type.getId(), results);
            }
        }
        return results;
    }

    private void addVeriniceSearchResultObject(VeriniceQuery query, String typeID, VeriniceSearchResult results) {
        VeriniceSearchResultObject veriniceSearchResultObject = processMultiSearchRequest(typeID, searchDao.prepareQueryWithAllFields(typeID, query, getAuthenticationService().getUsername()));
        if (veriniceSearchResultObject.getHits() > 0) {
            results.addVeriniceSearchObject(veriniceSearchResultObject);
        }
    }

    /**
     * @param typeID
     * @param msrb
     * @return
     */
    private VeriniceSearchResultObject processMultiSearchRequest(String typeID, MultiSearchRequestBuilder msrb) {
        List<SearchHit> hitList = new ArrayList<SearchHit>(0);

        MultiSearchResponse msr = searchDao.executeMultiSearch(msrb);
        for (MultiSearchResponse.Item i : msr.getResponses()) {
            if(i!=null && i.getResponse()!=null && i.getResponse().getHits()!=null) {
                for (SearchHit hit : i.getResponse().getHits().getHits()) {
                    hitList.add(hit);
                }
            }
        }
        String identifier = "";
        // VeriniceSearchResultObject results = new
        // VeriniceSearchResultObject(getTypeIDTranslation(typeID));
        VeriniceSearchResultObject results = new VeriniceSearchResultObject(typeID, getEntityName(typeID), getPropertyIds(typeID));
        for (SearchHit hit : hitList) {
            identifier = hit.getId();
            StringBuilder occurence = new StringBuilder();
            Iterator<Entry<String, HighlightField>> iter = hit.getHighlightFields().entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, HighlightField> entry = iter.next();
                occurence.append("[" + entry.getKey() + "]");
                occurence.append("\t").append(entry.getValue().fragments()[0]);
                if (iter.hasNext()) {
                    occurence.append("\n\n\n");
                }
            }

            VeriniceSearchResultRow result = new VeriniceSearchResultRow(results, identifier, occurence.toString());

            for (String key : hit.getSource().keySet()) {
                if (hit.getSource().get(key) != null) {
                    result.addProperty(key, hit.getSource().get(key).toString());
                }
            }
            results.addSearchResult(result);

        }
        return results;
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
    public void removeFromIndex(CnATreeElement element) {
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
    public void addToIndex(CnATreeElement element) {
        searchDao.index(element.getUuid(), convertElementToJson(element));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#updateOnIndex(sernet
     * .verinice.model.common.CnATreeElement)
     */
    @Override
    public void updateOnIndex(CnATreeElement element) {
        searchDao.update(element.getUuid(), convertElementToJson(element));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#convertElementToJson
     * (sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public String convertElementToJson(CnATreeElement element) {
        return JsonBuilder.getJson(element);
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

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#getNumericalValues(java
     * .lang.String)
     */
    @Override
    public Map<String, String> getNumericalValues(String input) {
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        for (EntityType entityType : HUITypeFactory.getInstance().getAllEntityTypes()) {
            for (PropertyType type : entityType.getAllPropertyTypes()) {
                if (type.isNumericSelect()) {
                    for (int i = type.getMinValue(); i <= type.getMaxValue(); i++) {
                        if (input.equals(type.getNameForValue(i))) {
                            map.put(type.getId(), String.valueOf(i));
                        }
                    }
                }
            }
        }
        return map;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#getInternationalReplacements
     * (java.lang.String)
     */
    @Override
    public String[] getInternationalReplacements(String input) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#addResultCountReduceFilter
     * (org.elasticsearch.action.search.SearchRequestBuilder)
     */
    @Override
    public MultiSearchRequestBuilder addResultCountReduceFilter(MultiSearchRequestBuilder srb) {
        return srb;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.search.ISearchService#addAccessFilter(org.
     * elasticsearch.action.search.SearchRequestBuilder)
     */
    @Override
    public MultiSearchRequestBuilder addAccessFilter(MultiSearchRequestBuilder srb) {
        return srb;
    }

    /**
     * @return the authenticationService
     */
    public IAuthService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * @param authenticationService the authenticationService to set
     */
    public void setAuthenticationService(IAuthService authenticationService) {
        this.authenticationService = authenticationService;
    }

}
