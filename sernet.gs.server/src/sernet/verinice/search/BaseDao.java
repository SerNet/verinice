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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MatchQueryBuilder.Operator;
import org.elasticsearch.index.query.QueryBuilders;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class BaseDao implements ISearchDao {

    private static final Logger LOG = Logger.getLogger(BaseDao.class);
    
    public static final String INDEX_NAME = "verinice";
    
    private ElasticsearchClientFactory clientFactory;
    
  
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#updateOrIndex(java.lang.String, java.lang.String)
     */
    @Override
    public ActionResponse updateOrIndex(String id, String json) {
        return update(id, json);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#index(java.lang.String)
     */
    @Override
    public ActionResponse update(String id, String json) { 
        try {
            UpdateResponse response = getClient().prepareUpdate(getIndex(),getType(),id).setRefresh(true)             
                    .setDoc(json).execute().actionGet();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Index updated, uuid: " + response.getId() + ", version: " + response.getVersion());
            }
            return response;
        } catch (DocumentMissingException e) {          
            return index(id, json);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error while indexing", e);
            throw new RuntimeException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#index(java.lang.String)
     */
    @Override
    public IndexResponse index(String id, String json) {  
        IndexResponse response = getClient().prepareIndex(getIndex(), getType(), id)
        .setSource(json)
        .execute()
        .actionGet();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Index created, uuid: " + response.getId());
        }
        return response;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#delete(java.lang.String)
     */
    @Override
    public DeleteResponse delete(String id) {
        DeleteResponse response = getClient().prepareDelete(getIndex(), getType(), id)
        .setRefresh(true) 
        .execute()
        .actionGet();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Index removed, uuid: " + id);
        }
        return response;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String)
     */
    @Override
    public SearchResponse findAll() {
        return getClient().prepareSearch(getIndex()).setTypes(getType())
                .setQuery(QueryBuilders.matchAllQuery())
                .execute()
                .actionGet();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String)
     */
    @Override
    public SearchResponse find(String title) {
        return find(title, Operator.OR);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String)
     */
    @Override
    public SearchResponse find(String title, Operator operator) {
        return getClient().prepareSearch(getIndex()).setTypes(getType())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchQuery("_all", title).operator(operator))
                .execute()
                .actionGet();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String)
     */
    @Override
    public SearchResponse findByPhrase(String title) {
        Set<String> highlightProperties = new HashSet<String>(0);
            for(EntityType type : HUITypeFactory.getInstance().getAllEntityTypes()){
                 for(String propertyTypeId : type.getAllPropertyTypeIds()){
                     highlightProperties.add(propertyTypeId);
                 }
             }
        SearchRequestBuilder srb = getClient().prepareSearch(getIndex()).setTypes(getType())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchPhraseQuery("_all", title));
        for(String s : highlightProperties){
            srb.addHighlightedField(s);
        }
        
        return srb.execute()
                .actionGet();
    }
    
    @Override 
    public SearchResponse findByPhrase(String phrase, String entityType){
        Set<String> highlightProperties = new HashSet<String>(0);
        for(EntityType type : HUITypeFactory.getInstance().getAllEntityTypes()){
            for(String propertyTypeId : type.getAllPropertyTypeIds()){
                highlightProperties.add(propertyTypeId);
            }
        }
        SearchRequestBuilder srb = getClient().prepareSearch(getIndex()).setTypes(getType())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchPhraseQuery("_all", phrase));
        for(String s : highlightProperties){
            srb.addHighlightedField(s);
        }

        srb.setPostFilter(FilterBuilders.boolFilter().must(FilterBuilders.termFilter("element-type", entityType)));
        return srb.execute()
                .actionGet();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String)
     */
    @Override
    public SearchResponse find(String property, String title) {
        return find(property, title, Operator.OR);
    }
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String)
     */
    @Override
    public SearchResponse find(String property, String title, Operator operator) {
        Set<String> highlightProperties = new HashSet<String>(0);
       if("_all".equals(property)){
           for(EntityType type : HUITypeFactory.getInstance().getAllEntityTypes()){
                for(String propertyTypeId : type.getAllPropertyTypeIds()){
                    highlightProperties.add(propertyTypeId);
                }
            }
        } else {
            highlightProperties.add(property);
        }
        SearchRequestBuilder srb = getClient().prepareSearch(getIndex()).setTypes(getType())
                .setQuery(QueryBuilders.matchQuery(property, title).operator(operator))
                .setSize(20);
                
        for(String s : highlightProperties){
            srb.addHighlightedField(s);
        }
        
                return srb.execute()
                .actionGet();
    }
    
    public DeleteByQueryResponse clear() {
        return getClient().prepareDeleteByQuery(getIndex())
                .setQuery(QueryBuilders.termsQuery("_type", getType()))
                .execute()
                .actionGet();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#getIndex()
     */
    @Override
    public String getIndex() {
        return INDEX_NAME;
    }
    
    public Client getClient() {
        return getClientFactory().getClient();
    }

    public ElasticsearchClientFactory getClientFactory() {
        return clientFactory;
    }

    public void setClientFactory(ElasticsearchClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
}
