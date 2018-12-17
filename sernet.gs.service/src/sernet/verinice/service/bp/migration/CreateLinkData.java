/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
 ******************************************************************************/
package sernet.verinice.service.bp.migration;

/**
 * This class holds data for creating a link
 */
public class CreateLinkData {

    private Integer dependantId;
    private Integer dependencyId;
    private String relationId;

    public CreateLinkData(Integer dependantId, Integer dependencyId, String relationId) {
        super();
        this.dependantId = dependantId;
        this.dependencyId = dependencyId;
        this.relationId = relationId;
    }

    public Integer getDependantId() {
        return dependantId;
    }

    public Integer getDependencyId() {
        return dependencyId;
    }

    public String getRelationId() {
        return relationId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dependantId == null) ? 0 : dependantId.hashCode());
        result = prime * result + ((dependencyId == null) ? 0 : dependencyId.hashCode());
        result = prime * result + ((relationId == null) ? 0 : relationId.hashCode());
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
        CreateLinkData other = (CreateLinkData) obj;
        if (dependantId == null) {
            if (other.dependantId != null)
                return false;
        } else if (!dependantId.equals(other.dependantId))
            return false;
        if (dependencyId == null) {
            if (other.dependencyId != null)
                return false;
        } else if (!dependencyId.equals(other.dependencyId))
            return false;
        if (relationId == null) {
            if (other.relationId != null)
                return false;
        } else if (!relationId.equals(other.relationId))
            return false;
        return true;
    }

}
