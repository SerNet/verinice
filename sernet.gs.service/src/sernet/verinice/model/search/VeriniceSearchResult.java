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
import java.util.Map;
import java.util.Set;

import sernet.verinice.model.common.CnATreeElement;

/**
 *
 */
public class VeriniceSearchResult implements Serializable{
    
    private static final long serialVersionUID = 201503181427L;
    
    private Map<String, String> properties;
    
    /**
     * should be uuid of corresponding {@link CnATreeElement} 
     **/
    private String identifier;
    
    private String fieldOfOccurence;
    

    public VeriniceSearchResult(String identifier, String occurence){
        this.properties = new HashMap<String, String>(0);
        this.identifier = identifier;
        this.fieldOfOccurence = occurence;
    }
    
    public int getColumnCount(){
        return properties.size();
    }
    
    public Set<String> getResultColumns(){
        return properties.keySet();
    }
    
    public String getValueFromResultString(String propertyType){
        if(properties.containsKey(propertyType)){
            return properties.get(propertyType);
        }
        return "";
    }
    
    public void addProperty(String propertyType, String value){
        properties.put(propertyType, value);
    }
    
    public String getIdentifier(){
        return identifier;
    }
    
    public String getFieldOfOccurence(){
        return fieldOfOccurence;
    }

}
