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

import java.util.Map;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchQueryBuilder.Operator;

import sernet.verinice.model.search.VeriniceQuery;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementDaoDummy implements IElementSearchDao {

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#getIndex()
     */
    @Override
    public String getIndex() {
        return INDEX_NAME;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#getType()
     */
    @Override
    public String getType() {
        return ElementDao.TYPE_NAME;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#updateOrIndex(java.lang.String, java.lang.String)
     */
    @Override
    public ActionResponse updateOrIndex(String id, String json) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#update(java.lang.String, java.lang.String)
     */
    @Override
    public ActionResponse update(String id, String json) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#index(java.lang.String, java.lang.String)
     */
    @Override
    public IndexResponse index(String id, String json) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#delete(java.lang.String)
     */
    @Override
    public DeleteResponse delete(String id) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#clear()
     */
    @Override
    public void clear() {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#findAll()
     */
    @Override
    public SearchResponse findAll() {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String)
     */
    @Override
    public SearchResponse find(String title) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String, org.elasticsearch.index.query.MatchQueryBuilder.Operator)
     */
    @Override
    public SearchResponse find(String title, Operator operator) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#findByPhrase(java.lang.String)
     */
    @Override
    public SearchResponse findByPhrase(String title) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#findByPhrase(java.lang.String, java.lang.String)
     */
    @Override
    public SearchResponse findByPhrase(String phrase, String entityType) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String, java.lang.String)
     */
    @Override
    public SearchResponse find(String property, String title) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String, java.lang.String, org.elasticsearch.index.query.MatchQueryBuilder.Operator)
     */
    @Override
    public SearchResponse find(String property, String title, Operator operator) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#prepareQueryWithAllFields(java.lang.String, sernet.verinice.model.search.VeriniceQuery, java.lang.String)
     */
    @Override
    public MultiSearchRequestBuilder prepareQueryWithAllFields(String typeId, VeriniceQuery query, String username) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#prepareQueryWithSpecializedFields(java.util.Map, java.lang.String, java.lang.String)
     */
    @Override
    public MultiSearchRequestBuilder prepareQueryWithSpecializedFields(Map<String, String> fieldmap, String typeId, String username) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#executeMultiSearch(org.elasticsearch.action.search.MultiSearchRequestBuilder)
     */
    @Override
    public MultiSearchResponse executeMultiSearch(MultiSearchRequestBuilder srb) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#find(java.lang.String, sernet.verinice.model.search.VeriniceQuery)
     */
    @Override
    public MultiSearchResponse find(String typeId, VeriniceQuery query) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.search.IElementSearchDao#findAndGroupByType(java.lang.String)
     */
    @Override
    public SearchResponse findAndGroupByType(String term) {
        return null;
    }

}
