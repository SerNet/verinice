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
import java.util.ArrayList;
import java.util.List;

import sernet.hui.common.connect.HUITypeFactory;

/**
 *
 */
public class VeriniceSearchResults implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 2015031428L;

    private List<VeriniceSearchResult> results;
    
    private String typeId;
    
    private int count = 0;
    
    private String[] defaultColumns;
    
    
    public VeriniceSearchResults(String typeId){
        this.typeId = typeId;
        this.results = new ArrayList<VeriniceSearchResult>(0);
    }
    
    public void addSearchResult(VeriniceSearchResult result){
        results.add(result);
        count++;
    }
    
    public int getSearchCount(){
        return count;
    }
    
    public List<VeriniceSearchResult> getAllResults(){
        return results;
    }
    
    public VeriniceSearchResult getSearchResultByUUID(String uuid){
        for(VeriniceSearchResult result : results){
            if(uuid.equals(result.getIdentifier())){
                return result;
            }
        }
        return null;
    }
    
    public String[] getDefaultColumns(String typeId){
        // TODO get default columns for objecttype from somewhere
        return new String[]{};
    }
    public String[] getAllColumns(){
        return HUITypeFactory.getInstance().getEntityType(typeId).getAllPropertyTypeIds();
    }
    
    public String getTypeId(){
        return typeId;
    }
    
    

}
