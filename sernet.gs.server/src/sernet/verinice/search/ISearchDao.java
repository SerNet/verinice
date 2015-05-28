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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - fine-tuning for communication with {@link ISearchService}
 ******************************************************************************/
package sernet.verinice.search;

import java.util.Map;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchQueryBuilder.Operator;

import sernet.verinice.model.search.VeriniceQuery;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ISearchDao {
    
    static final String FIELD_PERMISSION = "permission-roles";
    static final String INDEX_NAME = "verinice";
    static final String PATTERN_IS_READ_ALLOWED = "^[\\w]+\\(r+.*\\)";
    static final String PATTERN_IS_WRITE_ALLOWED = "^[\\w]+\\(.*w+\\)";
    
    public String getIndex();
    
    public String getType();

    public ActionResponse updateOrIndex(String id, String json);
    
    public ActionResponse update(String id, String json);
    
    public IndexResponse index(String id, String json);
    
    public DeleteResponse delete(String id);

    public void clear();
    
    public SearchResponse findAll();
    
    public SearchResponse find(String title);
    
    public SearchResponse find(String title, Operator operator);
    
    public SearchResponse findByPhrase(String title);
    
    public SearchResponse findByPhrase(String phrase, String entityType);

    public SearchResponse find(String property, String title);

    public SearchResponse find(String property, String title, Operator operator);
    
    public MultiSearchRequestBuilder prepareQueryWithAllFields(String typeId, VeriniceQuery query, String username);
    
    public MultiSearchRequestBuilder prepareQueryWithSpecializedFields(Map<String, String> fieldmap, String typeId, String username);
    
    public MultiSearchResponse executeMultiSearch(MultiSearchRequestBuilder srb);

    /**
     * @param typeId
     * @param query
     * @return
     */
    MultiSearchResponse find(String typeId, VeriniceQuery query);
}
