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
package sernet.verinice.interfaces.search;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.MultiSearchRequestBuilder;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.search.VeriniceSearchResult;
import sernet.verinice.model.search.VeriniceSearchResultObject;

/**
 *
 */
public interface ISearchService {
    
    /**
    * executes a search result to a csv file
    * (e.g. JsonObject=>CnATreeElement=>CSV-Entry), discuss if {@link CnATreeElement} is needed here, inspect existing csv importer
    * @return file that contains the csv
    * @param a search result
    **/
    File exportSearchResultToCsv(VeriniceSearchResultObject result);
    
    /**
     * adds filter to query that reduces size of resultset to preconfigurable size
     * @param query
     * @return filtered-query
     */
    MultiSearchRequestBuilder addResultCountReduceFilter(MultiSearchRequestBuilder srb);
    
    /**
     * adds filter to query that considers verinice access-management 
     * @param query
     * @return filtered-quers
     */
    MultiSearchRequestBuilder addAccessFilter(MultiSearchRequestBuilder srb);
    
    /**
     * executes query 
     * if typeID equals <code>null</code>, result will not be restricted on objecttype
     * (only use this for initial search) 
     * @param query
     * @param typeID
     * @return List SearchResult-Objects
     */
    VeriniceSearchResultObject getSearchResults(String query, String typeID);
    
    /**
     * executs a query without a given typeId, so all object types are queried
     * to fill the typeId-Combobox in the searchView
     */
    VeriniceSearchResult executeSimpleQuery(String query);
    
    
    /**
     * triggers job to create the search index initially
     */
    void index();
    
    /**
     * rebuilds the search-index
     */
    void reindex();
    
    /**
     * removes a given SearchHit from the index
     * @param hit
     */
//    void removeFromIndex(SearchHit hit);
    
    /**
     * removes a given {@link CnATreeElement} from the index
     * @param element
     */
    void removeFromIndex(CnATreeElement element);
    
    /**
     * adds a given {@link CnATreeElement} to the index
     * @param hit
     */
    void addToIndex(CnATreeElement element);
    
    /**
     * updates an index entry on base of a given {@link CnATreeElement}
     * @param element
     */
    void updateOnIndex(CnATreeElement element);
    
    String convertElementToJson(CnATreeElement element);
    
    Map<String, String> getNumericalValues(String input);
    
    String[] getInternationalReplacements(String input);
    
    

}
