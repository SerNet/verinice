/*******************************************************************************
 * Copyright (c) 2015 benjamin.
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
 *     benjamin <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wraps a verinice search result.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class VeriniceSearchResult implements Serializable {

    private VeriniceQuery veriniceQuery;

    private Map<String, VeriniceSearchResultObject> entityTypeIdToSearchResult;
    
    private int hits;
    
    public VeriniceSearchResult() {
        entityTypeIdToSearchResult = new HashMap<String, VeriniceSearchResultObject>();
    }
    
    public void addVeriniceSearchObject(VeriniceSearchResultObject veriniceSearchResultObject){
        entityTypeIdToSearchResult.put(veriniceSearchResultObject.getEntityTypeId(), veriniceSearchResultObject);
        hits += veriniceSearchResultObject.getHits();
    }
    
    public VeriniceSearchResultObject getVeriniceSearchObject(String entityTypeId){
        return entityTypeIdToSearchResult.get(entityTypeId);
    }
    
    public Set<VeriniceSearchResultObject> getAllVeriniceSearchObjects(){
        return new HashSet<VeriniceSearchResultObject>(entityTypeIdToSearchResult.values());
    }
    
    public int getHits(){
        return hits;
    }

    public VeriniceQuery getVeriniceQuery() {
        return veriniceQuery;
    }

    public void setVeriniceQuery(VeriniceQuery veriniceQuery) {
        this.veriniceQuery = veriniceQuery;
    }
}
