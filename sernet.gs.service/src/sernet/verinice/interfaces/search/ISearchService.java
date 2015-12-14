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

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;

/**
 * Methods for searching and indexing verinice data.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public interface ISearchService {
       
    /*
     * fields of {@link CnATreeElement} not given by property of entity-type
     */  
    public static final String ES_FIELD_UUID = "uuid";
    public static final String ES_FIELD_DBID = "dbid";
    public static final String ES_FIELD_TITLE = "title";
    public static final String ES_FIELD_ELEMENT_TYPE = "element-type";
    public static final String ES_FIELD_EXT_ID = "ext-id";
    public static final String ES_FIELD_SOURCE_ID = "source-id";
    public static final String ES_FIELD_SCOPE_ID = "scope-id";
    public static final String ES_FIELD_SCOPE_TITLE = "scope-title";
    public static final String ES_FIELD_PARENT_ID = "parent-id";
    public static final String ES_FIELD_ICON_PATH = "icon-path";
    public static final String ES_FIELD_PERMISSION_ROLES = "permission-roles";
    public static final String ES_FIELD_PERMISSION_NAME = "p-name";
    public static final String ES_FIELD_PERMISSION_VALUE = "p-value";
    
    public static final short ES_IMPLEMENTATION_TYPE_DUMMY = 0;
    public static final short ES_IMPLEMENTATION_TYPE_REAL = 1;

    /**
     * Executes a query on all types of elements.
     * 
     * @param query 
     * @return The result of the query
     */
    VeriniceSearchResult query(VeriniceQuery query);
    
    /**
     * Executes a query on elements with type <code>elementTypeId</code>.
     * 
     * @param query
     * @param elementTypeId The id of a verinice element
     * @return  The result of the query
     */
    VeriniceSearchResult query(VeriniceQuery query, String elementTypeId); 
    
    /**
     * Creates the search index for all elements
     */
    void index();
    
    /**
     * Deletes and creates the search index for all elements
     */
    void reindex();
    
    /**
     * Removes a given {@link CnATreeElement} from the index
     * 
     * @param element
     */
    void remove(CnATreeElement element);
    
    /**
     * Adds a given {@link CnATreeElement} to the index
     * 
     * @param element
     */
    void add(CnATreeElement element);
    
    /**
     * Updates an index entry on base of a given {@link CnATreeElement}
     * 
     * @param element
     */
    void update(CnATreeElement element);
    
    /**
     * returns a constant that determines the type of the implementation (dummy or not)
     * @return
     */
    int getImplementationtype();

    boolean isReindexRunning();

    void setReindexRunning(boolean running);

}
