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
package sernet.verinice.model.search;

import java.util.List;

import org.elasticsearch.search.SearchHits;

/**
 *
 */
public class VeriniceSearchResult {
    
    private SearchHits hits;
    private String typeId;
    private List<String> allColumns;
    private List<String> defaultColumns;

    public VeriniceSearchResult(SearchHits hits, String typpeId){
        this.hits = hits;
        this.typeId = typpeId;
    }
    
    int getResultCount(){return 1;};
    String getValueFromResult(String property_type, String uuid){return "";};
    String[] getDefaultColumns(){return defaultColumns.toArray(new String[defaultColumns.size()]);};
    String[] getAllColumns(){return allColumns.toArray(new String[allColumns.size()]);};
    String getTypeID(){return typeId;};

}
