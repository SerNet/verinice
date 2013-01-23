/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.hui.common.connect;

import java.io.Serializable;

/**
 * DependsType maps values from complexType "dependstype" of hitro.xsd.
 * 
 * Example from SNCA.xml
 * <huiproperty id="asset_description" inputtype="text" >
 *   <depends option="asset_type" value="asset_type_software" inverse="false"/>
 * </huiproperty>
 * <huiproperty id="asset_type" inputtype="singleoption">
 *   <option id="asset_type_software" />
 *   ..
 * </huiproperty>
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DependsType implements Serializable {
    
    private static final long serialVersionUID = -5000673649161596789L;

    private String propertyId;
    
    private String propertyValue;
    
    private boolean inverse = false;

    public DependsType(String propertyId, String propertyValue) {
        super();
        this.propertyId = propertyId;
        this.propertyValue = propertyValue;
    }
    
    
    public DependsType(String propertyId, String propertyValue, boolean inverse) {
        super();
        this.propertyId = propertyId;
        this.propertyValue = propertyValue;
        this.inverse = inverse;
    }



    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((propertyId == null) ? 0 : propertyId.hashCode());
        result = prime * result + ((propertyValue == null) ? 0 : propertyValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        DependsType other = (DependsType) obj;
        if (propertyId == null) {
            if (other.propertyId != null){
                return false;
            }
        } else if (!propertyId.equals(other.propertyId)){
            return false;
        }
        if (propertyValue == null) {
            if (other.propertyValue != null){
                return false;
            }
        } else if (!propertyValue.equals(other.propertyValue)){
            return false;
        }
        return true;
    }
    
    
    
    
}
