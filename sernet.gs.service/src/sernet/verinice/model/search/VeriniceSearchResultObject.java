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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.hui.common.connect.EntityType;


/**
 * Represents a table for the search view.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class VeriniceSearchResultObject implements Serializable {

    private Set<VeriniceSearchResultRow> results;

    private String id;

    private String name;

    private int hits = 0;

    private String[] propertyTypeIds;

    /**
     * Represents a pojo for the searchview.
     *
     * @param typeId
     *            Is the return value of: {@link EntityType#getId()}
     * @param name
     *            This is the human readable name of the entity.
     * @param propertyTypeIds
     *            Is the return value of:
     *            {@link EntityType#getAllPropertyTypeIds()}
     */
    public VeriniceSearchResultObject(String typeId, String name, String[] propertyTypeIds) {
        this.id = typeId;
        this.results = new HashSet<VeriniceSearchResultRow>(0);
        this.name = name;
        this.propertyTypeIds = propertyTypeIds;
    }

    public void addSearchResult(VeriniceSearchResultRow result) {
        results.add(result);
        hits = results.size();
    }

    public int getHits() {
        return hits;
    }

    public Set<VeriniceSearchResultRow> getAllResults() {
        return results;
    }

    public VeriniceSearchResultRow getSearchResultByUUID(String uuid) {
        for (VeriniceSearchResultRow result : results) {
            if (uuid.equals(result.getIdentifier())) {
                return result;
            }
        }
        return null;
    }

    public String[] getAllColumns() {
        return propertyTypeIds;
    }

    public String getEntityTypeId() {
        return id;
    }

    public String getEntityName() {
        return name;
    }

    public Set<VeriniceSearchResultRow> getRows() {
        return results;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + hits;
        result = prime * result + ((results == null) ? 0 : results.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
