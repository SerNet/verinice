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
		if (fireChange && parent != null)
			parent.firePropertyChanged(this, source);
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
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
}
