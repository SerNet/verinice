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
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.PropertyType;

/**
 * Represents a table of properties of several CnATreeElements for the search
 * view. The table is always bound to an {@link EntityType} via an internal id.
 * Every row represents an match and a column represents the
 * {@link PropertyType}.
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class VeriniceSearchResultTable implements Serializable {

    private Set<VeriniceSearchResultRow> results;

    private String id;

    private String name;

    private int hits = 0;

    private int limit = 0;

    private String[] propertyTypeIds;

    /**
     * Holds the parent wrapper object, with references to elements with
     * matches.
     */
    private VeriniceSearchResult parent;

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
    public VeriniceSearchResultTable(String typeId, String name, String[] propertyTypeIds) {
        this.id = typeId;
        this.results = new HashSet<VeriniceSearchResultRow>(0);
        this.name = name;
        this.propertyTypeIds = propertyTypeIds;
    }

    public void addVeriniceSearchResultRow(VeriniceSearchResultRow result) {
        if(results.contains(result)){
            addToExistingResult(result);
        } else {
            results.add(result);
        }
        hits = results.size();
    }

    /**
     * @param result
     */
    private void addToExistingResult(VeriniceSearchResultRow result) {
        VeriniceSearchResultRow existingRow = getSearchResultByUUID(result.getIdentifier());
        results.remove(existingRow);
        Occurence occurence = result.getOccurence();
        for(Entry<String, SortedSet<String>> entry : occurence.entries.entrySet()){
            if(!existingRow.getOccurence().getFragments(entry.getKey()).isEmpty()){
                for(String s : entry.getValue()){
                    existingRow.getOccurence().getFragments(entry.getKey()).add(s);
                }
            } else {
                existingRow.getOccurence().addFragment(entry.getKey(), occurence.getNameOfPropertyId(entry.getKey()), entry.getValue().first());
            }
        }
        results.add(existingRow);
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
        VeriniceSearchResultTable other = (VeriniceSearchResultTable) obj;
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

    public VeriniceSearchResult getParent() {
        return parent;
    }

    public void setParent(VeriniceSearchResult parent) {
        this.parent = parent;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
