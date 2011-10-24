/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web;

import java.io.Serializable;

import sernet.verinice.interfaces.iso27k.ILink;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class LinkInformation implements ILink,Comparable<LinkInformation>,Serializable {

    String targetName, type, targetUuid;

    private Integer dependantId;
    private Integer dependencyId;
    private String typeId;
    
    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String target) {
        this.targetName = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetUuid() {
        return targetUuid;
    }

    public void setTargetUuid(String targetUuid) {
        this.targetUuid = targetUuid;
    }

    public Integer getDependantId() {
        return dependantId;
    }

    public void setDependantId(Integer dependantId) {
        this.dependantId = dependantId;
    }

    public Integer getDependencyId() {
        return dependencyId;
    }

    public void setDependencyId(Integer dependencyId) {
        this.dependencyId = dependencyId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(LinkInformation o) {
        final int THIS_IS_LESS = -1;
        final int EQUAL = 0;
        final int THIS_IS_GREATER = 1;
        int result = THIS_IS_LESS;
        if(o!=null) {
            if(o.getType()!=null) {
                if(this.getType()!=null) {
                    result = this.getType().compareTo(o.getType());
                } else {
                    result = THIS_IS_GREATER;
                }           
            } else if(this.getType()==null){
                result = EQUAL;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dependantId == null) ? 0 : dependantId.hashCode());
        result = prime * result + ((dependencyId == null) ? 0 : dependencyId.hashCode());
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
        LinkInformation other = (LinkInformation) obj;
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
        if (typeId == null) {
            if (other.typeId != null)
                return false;
        } else if (!typeId.equals(other.typeId))
            return false;
        return true;
    }
    
   
   
}
