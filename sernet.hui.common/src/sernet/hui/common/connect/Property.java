/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.common.connect;

import java.io.Serializable;



/**
 * @author prack
 * @version $Id: Property.java,v 1.4 2006/06/15 15:47:07 aprack Exp $
 */
public class Property implements Serializable, ITypedElement {

	private Integer dbId;
	private String propertyType;
	private String propertyValue;
	private Entity parent;
	
    public static final String TYPE_ID = "huiproperty";
    public static final int UNDEF = 0;

	public Property(Entity ent) {
		parent = ent;
	}
	
	Property() {
		// default constructor for hibernate
	}
	
	/**
	 * @return Returns the propertyValue.
	 */
	public String getPropertyValue() {
		return propertyValue;
	}
	
	public boolean isEmpty() {
	    return getPropertyValue()==null || getPropertyValue().isEmpty();
	}
	
	/**
	 * Returns the value as an integer.
	 * @return
	 */
	public int getNumericPropertyValue() {
	    try {
	        return Integer.parseInt(propertyValue);
        } catch (NumberFormatException e) {
            return UNDEF;
        }
	}
	
	 /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }
	
	public void setPropertyValue(String propertyValue, boolean fireChange, Object source) {
		this.propertyValue = propertyValue;
		if (fireChange && parent != null){
			parent.firePropertyChanged(this, source);
		}
	}
	
	public void setPropertyValue(String propertyValue, boolean fireChange) {
		setPropertyValue(propertyValue, fireChange, null);
	}
	
	/**
	 * @param propertyValue The propertyValue to set.
	 */
	public void setPropertyValue(String propertyValue) {
		setPropertyValue(propertyValue, true, null);
	}
	
    public String getPropertyTypeID(){
        return propertyType;
    }
    
    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

	public Entity getParent() {
		return parent;
	}

	public void setParent(Entity parent) {
		this.parent = parent;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	
	public Property copy(Entity parent) {
	    Property property = new Property(parent);
        property.setPropertyType(getPropertyType());
        property.setPropertyValue(getPropertyValue(), false);
        return property;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbId == null) ? 0 : dbId.hashCode());
        result = prime * result + ((propertyType == null) ? 0 : propertyType.hashCode());
        result = prime * result + ((propertyValue == null) ? 0 : propertyValue.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        Property other = (Property) obj;
        if (dbId == null) {
            if (other.dbId != null){
                return false;
            }
        } else if (!dbId.equals(other.dbId)){
            return false;
        }
        if (propertyType == null) {
            if (other.propertyType != null){
                return false;
            }
        } else if (!propertyType.equals(other.propertyType)){
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
