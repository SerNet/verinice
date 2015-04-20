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
 *     Benjamin Weißenfels <bw@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import sernet.hui.common.connect.HUITypeFactory;

@SuppressWarnings("serial")
public class VeriniceSearchResultObject implements Serializable {


    private List<VeriniceSearchResultRow> results;

    private String typeId;

    private int hits = 0;

    public VeriniceSearchResultObject(String typeId){
        this.typeId = typeId;
        this.results = new ArrayList<VeriniceSearchResultRow>(0);
    }

    public void addSearchResult(VeriniceSearchResultRow result){
        results.add(result);
        hits++;
    }

    public int getHits(){
        return hits;
    }

    public List<VeriniceSearchResultRow> getAllResults(){
        return results;
    }

    public VeriniceSearchResultRow getSearchResultByUUID(String uuid){
        for(VeriniceSearchResultRow result : results){
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

    public String getEntityTypeId(){
        return typeId;
    }

    public List<VeriniceSearchResultRow> getRows(){
        return results;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hits;
        result = prime * result + ((results == null) ? 0 : results.hashCode());
        result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VeriniceSearchResultObject other = (VeriniceSearchResultObject) obj;
        if (hits != other.hits)
            return false;
        if (results == null) {
            if (other.results != null)
                return false;
        } else if (!results.equals(other.results))
            return false;
        if (typeId == null) {
            if (other.typeId != null)
                return false;
        } else if (!typeId.equals(other.typeId))
            return false;
        return true;
    }

}
