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

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.search.IJsonBuilder;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.search.Occurence;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.model.search.VeriniceSearchResultTable;
import sernet.verinice.search.IElementSearchDao;
import sernet.verinice.search.Indexer;

public class SearchService implements ISearchService {

    private static final Logger LOG = Logger.getLogger(SearchService.class);

    @Resource(name = "searchIndexer")
    protected Indexer searchIndexer;

    @Resource(name = "searchElementDao")
    protected IElementSearchDao searchDao;

    @Resource(name = "jsonBuilder")
    protected IJsonBuilder jsonBuilder;

    private volatile boolean reindexRunning = false;

    /**
     * Should be used by client to pass a query to the service in future
     * releases the method should decide which kind of query must be send to es.
     *
     * @see sernet.verinice.interfaces.search.ISearchService#query(sernet.verinice.model.search.VeriniceQuery)
     */
    @Override
    public VeriniceSearchResult query(VeriniceQuery veriniceQuery) {
        ServerInitializer.inheritVeriniceContextState();
        IAuthService authService = (IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE);
        /* query returns false by default */
        if (authService.isPermissionHandlingNeeded()) {
            veriniceQuery.setScopeOnly(authService.isScopeOnly());
        }
        return query(veriniceQuery, null);
    }

    /**
     * Uses the es querybuilder API to build a query that could be parameterized
     * to search on given fields only, and adding filters for rights management
     * and type-filtered results.
     *
     * @see sernet.verinice.interfaces.search.ISearchService#query(sernet.verinice.model.search.VeriniceQuery,
     *      java.lang.String)
     */
    @Override
    public VeriniceSearchResult query(VeriniceQuery query, String elementTypeId) {
        long startTime = System.currentTimeMillis();
        ServerInitializer.inheritVeriniceContextState();
        VeriniceSearchResult results = new VeriniceSearchResult();
        if (StringUtils.isNotEmpty(elementTypeId)) {
            results.addVeriniceSearchTable(processSearchResponse(elementTypeId,
                    searchDao.find(elementTypeId, query), query.getLimit()));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Time for executing query( " + query.getQuery() + ", " + elementTypeId
                        + "):\t" + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
            }
        } else {
            for (EntityType type : HUITypeFactory.getInstance().getAllEntityTypes()) {
                results.addVeriniceSearchTable(processSearchResponse(type.getId(),
                        searchDao.find(type.getId(), query), query.getLimit()));
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Time for executing query( " + query.getQuery() + ", <allTypeIds>):\t"
                        + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
            }
        }
        return results;
    }

    private String getEntityName(String typeID) {
        ServerInitializer.inheritVeriniceContextState();
        return HUITypeFactory.getInstance().getEntityType(typeID).getName();
    }

    private VeriniceSearchResultTable processSearchResponse(String elementTypeId,
            MultiSearchResponse msr, int limit) {
        long startTime = System.currentTimeMillis();
        List<SearchHit> hitList = createHitList(msr, limit);
        String identifier = "";
        VeriniceSearchResultTable results = new VeriniceSearchResultTable(elementTypeId,
                getEntityName(elementTypeId), getPropertyIds(elementTypeId));
        results.setLimit(limit);
        for (SearchHit hit : hitList) {
            identifier = hit.getId();
            Occurence occurence = createOccurence(elementTypeId, hit);
            VeriniceSearchResultRow result = new VeriniceSearchResultRow(results, identifier,
                    occurence);

            for (Entry<String, Object> e : hit.getSource().entrySet()) {
                if (e.getValue() != null) {
                    result.addProperty(e.getKey(), e.getValue().toString());
                }
            }
            results.addVeriniceSearchResultRow(result);

        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Time for executing processSearchResponse:\t"
                    + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
        }
        return results;
    }

    private Occurence createOccurence(String elementTypeId, SearchHit hit) {
        Occurence occurence = new Occurence();
        for (Entry<String, HighlightField> entry : hit.getHighlightFields().entrySet()) {
            String translatedFieldName = getHuiTranslation(entry.getKey(), elementTypeId);
            for (Text textFragment : entry.getValue().fragments()) {
                occurence.addFragment(entry.getKey(), translatedFieldName, textFragment.toString());
            }
        }
        return occurence;
    }

    private List<SearchHit> createHitList(MultiSearchResponse msr, int limit) {

        Stream<Item> nonEmptyResponses = Stream.of(msr.getResponses()).filter(item -> item != null
                && item.getResponse() != null && item.getResponse().getHits() != null);

        Stream<SearchHit> hits = nonEmptyResponses
                .flatMap(item -> Stream.of(item.getResponse().getHits().getHits()));

        if (limit > 0) {
            hits = hits.limit(limit);
        }
        return hits.collect(Collectors.toList());
    }

    private String getHuiTranslation(String id, String entityType) {
        for (PropertyType type : HUITypeFactory.getInstance().getEntityType(entityType)
                .getAllPropertyTypes()) {
            if (type.getId().equals(id)) {
                return type.getName();
            }
        }
        LOG.warn("No i18n found for id:\t" + id + "\t of type:\t" + entityType);
        return id;
    }

    private String[] getPropertyIds(String typeID) {
        ServerInitializer.inheritVeriniceContextState();
        return HUITypeFactory.getInstance().getEntityType(typeID).getAllPropertyTypeIds();
    }

    /*
     * @see sernet.verinice.interfaces.search.ISearchService#index()
     */
    @Override
    public void index() {
        searchIndexer.nonBlockingIndexing();
    }

    /*
     * @see sernet.verinice.interfaces.search.ISearchService#reindex()
     */
    @Override
    public void reindex() {
        searchDao.clear();
        searchIndexer.blockingIndexing();
    }

    /*
     * @see
     * sernet.verinice.interfaces.search.ISearchService#removeFromIndex(sernet
     * .verinice.model.common.CnATreeElement)
     */
    @Override
    public void remove(CnATreeElement element) {
        searchDao.delete(element.getUuid());
    }

    /*
     * @see sernet.verinice.interfaces.search.ISearchService#addToIndex(sernet.
     * verinice .model.common.CnATreeElement)
     */
    @Override
    public void add(CnATreeElement element) {
        searchDao.index(element.getUuid(), getJsonBuilder().getJson(element));
    }

    /*
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

    public boolean isReindexRunning() {
        return reindexRunning;
    }

    public void setReindexRunning(boolean running) {
        this.reindexRunning = running;
    }

    @Override
    public int getImplementationtype() {
        return ISearchService.ES_IMPLEMENTATION_TYPE_REAL;
    }

}
