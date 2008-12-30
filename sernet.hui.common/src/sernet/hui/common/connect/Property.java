
package sernet.hui.common.connect;

import java.io.Serializable;



/**
 * @author prack
 * @version $Id: Property.java,v 1.4 2006/06/15 15:47:07 aprack Exp $
 */
public class Property implements Serializable {

	private Integer dbId;
	private String propertyType;
	private String propertyValue;
	private Entity parent;

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
