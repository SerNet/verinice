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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.index.query.TermsFilterBuilder;
import org.elasticsearch.indices.IndexMissingException;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.search.Occurence;
import sernet.verinice.model.search.VeriniceQuery;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class BaseDao implements ISearchDao {

    private static final Logger LOG = Logger.getLogger(BaseDao.class);
    
    private ElasticsearchClientFactory clientFactory;
    
    private IConfigurationService configurationService;
    private IAuthService authService;
    
    /**
     * {@link ISearchService.ES_FIELD_UUID} and {@link ISearchService.ES_FIELD_PERMISSION_ROLES} missing here, since they should not be searchable 
     */
    private final List<String> EXTRA_FIELDS = Arrays.asList(new String[]{
            ISearchService.ES_FIELD_UUID,
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
        long startTime = System.currentTimeMillis();
        SearchResponse response = getClient().prepareSearch(getIndex()).setTypes(getType())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchQuery("_all", title).operator(operator))
                .execute()
                .actionGet();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Time for executing find():\t" + String.valueOf((System.currentTimeMillis() - startTime) / 1000) + " seconds");
        }

        return response;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String)
     */
    @Override
    public SearchResponse findByPhrase(String title) {
        SearchRequestBuilder requestBuilder = getClient().prepareSearch(getIndex()).setTypes(getType())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchPhraseQuery("_all", title))
                .setHighlighterPostTags(Occurence.HTML_CLOSING_TAG)
                .setHighlighterPreTags(Occurence.HTML_OPEN_TAG);

        requestBuilder = HighlightFieldAdder.addAll(requestBuilder);       
        return requestBuilder.execute().actionGet();
    }
    
    @Override
    public MultiSearchResponse find(String typeId, VeriniceQuery query) {
        MultiSearchRequestBuilder request = prepareQueryWithAllFields(typeId, query, getAuthService().getUsername());
        return executeMultiSearch(request);
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
        MultiSearchRequestBuilder requestBuilder = getClient().prepareMultiSearch();
        // only 1 call per query, instead of calling
        // isPermissionHandlingNeeded() from within for-loop
        boolean permissionHandlingNeeded = isPermissionHandlingNeeded();
        for(String field : map.keySet()){
            String value = map.get(field);
            SearchRequestBuilder searchBuilder = getClient().prepareSearch(getIndex())
                    .setTypes(getType())
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setHighlighterPostTags(Occurence.HTML_CLOSING_TAG)
                    .setHighlighterPreTags(Occurence.HTML_OPEN_TAG);

            if(value != null && !value.isEmpty()){
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchPhraseQuery(field, value);
                searchBuilder = searchBuilder.setQuery(matchQueryBuilder);
            } else {
                // fires if search phrase is empty
                MatchAllQueryBuilder matchQueryBuilder = QueryBuilders.matchAllQuery();
                searchBuilder = searchBuilder.setQuery(matchQueryBuilder);
            }
                   
            searchBuilder = HighlightFieldAdder.add(field, searchBuilder);
            TermsFilterBuilder typeBuilder = FilterBuilders.inFilter(ISearchService.ES_FIELD_ELEMENT_TYPE, new String[]{typeId});
            AndFilterBuilder andBuilder = FilterBuilders.andFilter(typeBuilder);
            
            if (permissionHandlingNeeded) {
                andBuilder = andBuilder.add(createPermissionFilter(username));              
                if (query.isScopeOnly()) { // scopeOnly is not needed if no
                                           // permission handling is needed
                    andBuilder = andBuilder.add(createScopeOnlyFilter(username));
                }
            }
            
            if(query.getScopeId() != -1){
                // vermutlich besser als suchkriterium als als filter anwenden
                andBuilder = andBuilder.add(createScopeIdFilter(query.getScopeId()));
            }

            searchBuilder = searchBuilder.setPostFilter(andBuilder);
            
            searchBuilder = searchBuilder.setFrom(0);
            if(query.getLimit() > 0){
                searchBuilder = searchBuilder.setSize(query.getLimit());
            }

            searchBuilder.setExplain(true);

            requestBuilder = requestBuilder.add(searchBuilder);
        }


        return requestBuilder;
    }

    private boolean isPermissionHandlingNeeded() {
        return getAuthService()!=null 
                && getAuthService().isPermissionHandlingNeeded() 
                && !hasAdminRole(getAuthService().getRoles());
    }
    
    private boolean hasAdminRole(String[] roles) {
        if(roles!=null) {
            for (String r : roles) {
                if (ApplicationRoles.ROLE_ADMIN.equals(r))
                    return true;
            }   
        }
        return false;
    }
    
    private TermFilterBuilder createScopeOnlyFilter(String username) {
        return FilterBuilders.termFilter(ISearchService.ES_FIELD_SCOPE_ID, getConfigurationService().getScopeId(username));
    }

    private TermsFilterBuilder createPermissionFilter(String username) {
        return FilterBuilders.inFilter(ISearchService.ES_FIELD_PERMISSION_ROLES + "." + ISearchService.ES_FIELD_PERMISSION_NAME, getRoleString(username).toArray());
    }
    
    private TermFilterBuilder createScopeIdFilter(int scopeId){
        return FilterBuilders.termFilter(ISearchService.ES_FIELD_SCOPE_ID, scopeId);
    }
    
  
    
    @Override
    public MultiSearchResponse executeMultiSearch (MultiSearchRequestBuilder srb){
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
        SearchRequestBuilder requestBuilder = getClient().prepareSearch(getIndex()).setTypes(getType())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchPhraseQuery("_all", phrase))
                .setHighlighterPostTags(Occurence.HTML_CLOSING_TAG)
                .setHighlighterPreTags(Occurence.HTML_OPEN_TAG);

        requestBuilder = HighlightFieldAdder.addAll(requestBuilder);
        return requestBuilder.execute().actionGet();
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
        SearchRequestBuilder requestBuilder = getClient().prepareSearch(getIndex())
                .setTypes(getType())
                .setQuery(QueryBuilders.matchQuery(property, title)
                .operator(operator))
                .setSize(20)
                .setHighlighterPostTags(Occurence.HTML_CLOSING_TAG)
                .setHighlighterPreTags(Occurence.HTML_OPEN_TAG);

        if("_all".equals(property)){
            requestBuilder = HighlightFieldAdder.addAll(requestBuilder);
         } else {
             requestBuilder.addHighlightedField(property);
         }
        return requestBuilder.execute().actionGet();
    }
    
    @Override
    public void clear() {
        try {
           getClient().prepareDeleteByQuery(getIndex())
               .setQuery(QueryBuilders.termsQuery("_type", getType()))
               .execute()
               .actionGet();

        } catch (IndexMissingException ex){
            LOG.error("error occurred while deleting index: \"" + getIndex() + "\". This index seems not to exists so it is no problem to ignore this error", ex);
        }


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
        List<String> applicationRoles = Arrays.asList(new String[] { ApplicationRoles.ROLE_ADMIN, ApplicationRoles.ROLE_LOCAL_ADMIN,
              ApplicationRoles.ROLE_GUEST,
              ApplicationRoles.ROLE_LDAPUSER, 
              ApplicationRoles.ROLE_USER,
              ApplicationRoles.ROLE_WEB});
      List<String> userRoles = new ArrayList<String>(0);
      String[] roles = getConfigurationService().getRoles(username);
      for(int i = 0; i < roles.length; i++){
          if(!applicationRoles.contains(roles[i])){
              userRoles.add(roles[i].toLowerCase());
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
    
    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }
    
}
