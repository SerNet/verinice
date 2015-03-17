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
import java.util.HashMap;

import sernet.hui.common.connect.PropertyType;

/**
 *
 */
public interface ISearchService {
    
    /**
    * returns preconfigured set of default columns for a given propertytype
    **/
    String[] getDefaultColumns(PropertyType type);

    /**
    * executes a fulltextsearch with a given query / searchitem and a given propertytype
    * TODO: define result class
    * @param 
    **/
    Object executeSimpleSearchQuery(String searchItem, PropertyType type);

    /**
    * returns only the count of a result of a query to show in propertytype dropdownlist
    * @param a search query (does not have to be instanceof {@link String}
    * @return result count by {@link PropertyType}
    **/
     HashMap<PropertyType, Integer> getSimpleSearchQueryResultCount(String query);

    /**
    * executes a search result to a csv file
    * (e.g. JsonObject=>CnATreeElement=>CSV-Entry), discuss if cnatreeelement is needed here, inspect existing csv importer
    * TODO: define result class 
    * @return file that contains the csv
    * @param a search result
    **/
    File exportSearchResultToCsv(Object result);

    /**
    * loads Result of PropertyType with the largest ResultCount (default Result)
    * TODO: define result class
    * @return result for default (most results in proptype-search) selection 
    **/
    Object loadDefaultResult(PropertyType type);

    /**
    * adds filter to given searchquery, so that only a limited set (200 by default) rows are returned with the resultset
    * TODO: define queryClass
    * @param unfiltered query
    * @return filtered query
    **/ 
    Object addResultCountReduceFilter(Object query);

}
