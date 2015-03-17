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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class VeriniceSearchResult implements Serializable{
    
    private static final long serialVersionUID = 201503171549L;
    
    private String typeId;
    private List<String> allColumns;
    private List<String> defaultColumns;
    
    private Map<String, Map<String, String>> results;

    public VeriniceSearchResult(Map<String, Map<String, String>> results, String typeId){
        this.results = results;
        this.typeId = typeId;
    }
    
    public int getResultCount(){
        return results.size();
    }
    public String getValueFromResult(String uuid, String property_type){
        if(results.containsKey(uuid) && results.get(uuid).containsKey(property_type)){
            return results.get(uuid).get(property_type);
        }
        return null;
    }
    public String[] getDefaultColumns(){
        return defaultColumns.toArray(new String[defaultColumns.size()]);
    }
    public String[] getAllColumns(){
        return allColumns.toArray(new String[allColumns.size()]);
    }
    public String getTypeID(){
        return typeId;
    }
    Map<String, String> getResult(String uuid){
        if(results.containsKey(uuid)){
            return results.get(uuid);
        } else {
            return new HashMap<String, String>(0);
        }
    }

}
