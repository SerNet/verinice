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

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementDao extends BaseDao implements IElementSearchDao {

    public static final String TYPE_NAME = "element";

    /* (non-Javadoc)
     * @see sernet.verinice.search.ISearchDao#getType()
     */
    @Override
    public String getType() {
        return TYPE_NAME;
    }
    
    public SearchResponse findAndGroupByType(String term) {
        return  getClient().prepareSearch(getIndex()).setTypes(getType())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .addAggregation(AggregationBuilders.terms("byType").field("element-type"))
                .setQuery(QueryBuilders.matchQuery("_all", term))
                .execute()
                .actionGet();
    }



}
