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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BaseFilterBuilder;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MatchQueryBuilder.Operator;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsFilterBuilder;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.search.VeriniceQuery;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class BaseDao implements ISearchDao {

    private static final Logger LOG = Logger.getLogger(BaseDao.class);
    
    private ElasticsearchClientFactory clientFactory;
    
    private IConfigurationService configurationService;
    
    private final String preGroupPattern = ".*(";
    private final String postGroupPattern = "#r{1}w?#).*";
    
    /**
     * {@link ISearchService.ES_FIELD_UUID} and {@link ISearchService.ES_FIELD_PERMISSION_ROLES} missing here, since they should not be searchable 
     */
    private final List<String> EXTRA_FIELDS = Arrays.asList(new String[]{
            ISearchService.ES_FIELD_TITLE,
            ISearchService.ES_FIELD_ELEMENT_TYPE,
            ISearchService.ES_FIELD_ICON_PATH,
            ISearchService.ES_FIELD_DBID,
            ISearchService.ES_FIELD_EXT_ID,
            ISearchService.ES_FIELD_SOURCE_ID,
            ISearchService.ES_FIELD_SCOPE_ID,
            ISearchService.ES_FIELD_PARENT_ID});
    
  
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
//                LOG.debug("Index updated, uuid: " + response.getId() + ", version: " + response.getVersion());
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
    public MultiSearchRequestBuilder prepareQueryWithAllFields(String typeId, VeriniceQuery query, String username){
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        for(String property : HUITypeFactory.getInstance().getEntityType(typeId).getAllPropertyTypeIds()){
            map.put(property, query.getQuery());
        }
        for(String field : EXTRA_FIELDS){
            map.put(field, query.getQuery());
        }
//        return buildBooleanMultiFieldQuery(map, typeId, username, query);
        return buildQueryIterative(map, typeId, username, query);
        
    }
    
    @Override
    public MultiSearchRequestBuilder prepareQueryWithSpecializedFields(Map<String, String> fieldmap, String typeId, String username){
        MultiSearchRequestBuilder multiSearchBuilder = getClient().prepareMultiSearch();

        for(String field : fieldmap.keySet()){
            String value = null;
            if(fieldmap.containsKey(field)){
                value = fieldmap.get(field);
            }
            if(value != null){
                SearchRequestBuilder srb = getClient().prepareSearch(getIndex()).setTypes(getType()).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setQuery(QueryBuilders.matchPhraseQuery(field, value))
                        .setPostFilter(FilterBuilders.boolFilter().must(FilterBuilders.termFilter(ISearchService.ES_FIELD_ELEMENT_TYPE, typeId)));
                if(LOG.isDebugEnabled()){
                    LOG.debug("SingleSearchQuery for <" + field + ">:\t" + srb.toString());
                }
                multiSearchBuilder.add(srb); 
                        
            }
        }
        
        return multiSearchBuilder;
    }
    
    private MultiSearchRequestBuilder buildQueryIterative(Map<String, String> map, String typeId, String username, VeriniceQuery query){
        if(Asset.TYPE_ID.equals(typeId)){
            this.hashCode();
        }
        MultiSearchRequestBuilder requestBuilder = getClient().prepareMultiSearch();
        for(String field : map.keySet()){
            String value = map.get(field);
            if(value != null){
                BaseFilterBuilder permissionBuilder = createPermissionFilter(username);
                SearchRequestBuilder searchBuilder = getClient().prepareSearch(getIndex()).setTypes(getType()).setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
                searchBuilder = searchBuilder.setQuery(QueryBuilders.matchPhraseQuery(field, value));
                TermsFilterBuilder typeBuilder = FilterBuilders.inFilter(ISearchService.ES_FIELD_ELEMENT_TYPE, new String[]{typeId});
                AndFilterBuilder andBuilder = FilterBuilders.andFilter(typeBuilder);
                
                if(permissionBuilder != null){
                    andBuilder = andBuilder.add(permissionBuilder);
                } else {
                    // should only happen if superadmin is logged in
                    this.hashCode();
                }
                searchBuilder = searchBuilder.setPostFilter(andBuilder);
                
                searchBuilder = searchBuilder.setFrom(0);
                searchBuilder = searchBuilder.setSize(query.getLimit());
                requestBuilder = requestBuilder.add(searchBuilder);
            }
        }
        return requestBuilder;
    }

    private TermsFilterBuilder createPermissionFilter(String username) {
        return FilterBuilders.inFilter(ISearchService.ES_FIELD_PERMISSION_ROLES + "." + ISearchService.ES_FIELD_PERMISSION_NAME, getRoleString(username).toArray());
    }
    
  
    
    @Override
    public MultiSearchResponse executeMultiSearch (MultiSearchRequestBuilder srb){
        if(LOG.isDebugEnabled()){
            for(SearchRequest r : srb.request().requests()){
                try {
                    String source = XContentHelper.convertToJson(r.source(), true);
                    if(source.contains("asset_name")){
                        this.hashCode();
                        LOG.debug("Request:\t" + source);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        try{
            
            return srb.execute().actionGet();
        } catch (ActionRequestValidationException e){
            LOG.error("Request is not valid", e);
            
        } catch (Exception t){
            LOG.error("Something went wrong in executin multisearchrequest", t);
        }
        return null;
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
    
    private List<String> getRoleString(String username){
      List<String> applicationRoles = Arrays.asList(new String[]{ApplicationRoles.ROLE_ADMIN, 
              ApplicationRoles.ROLE_GUEST,
              ApplicationRoles.ROLE_LDAPUSER, 
              ApplicationRoles.ROLE_USER,
              ApplicationRoles.ROLE_WEB});
      List<String> userRoles = new ArrayList<String>(0);
      String[] roles = getConfigurationService().getRoles(username);
      for(int i = 0; i < roles.length; i++){
          if(!applicationRoles.contains(roles[i])){
              userRoles.add(roles[i]);
          }
      }
      return userRoles;
    }

    /**
     * @return the confService
     */
    public IConfigurationService getConfigurationService() {
        return configurationService;
    }

    /**
     * @param confService the confService to set
     */
    public void setConfigurationService(IConfigurationService confService) {
        this.configurationService = confService;
    }
    
    private String getGroupPattern (String groupname){
        StringBuilder sb = new StringBuilder();
        return sb.append(preGroupPattern).append(groupname).append(postGroupPattern).toString();
    }
    
}
