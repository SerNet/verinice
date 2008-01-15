/*
 * Created on 03.05.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package sernet.hui.common.connect;



public class PropertyFactory {

    protected static Property create(PropertyType type, String value, Entity ent) {
		Property newProp = new Property(ent);
		newProp.setPropertyType(type.getId());
		newProp.setPropertyValue(value);
		return newProp;
    		
    }
}
