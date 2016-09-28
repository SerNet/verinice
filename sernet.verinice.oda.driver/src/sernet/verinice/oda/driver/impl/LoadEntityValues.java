/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <rschuster[at]tarent[dot]de>.
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
 *     Robert Schuster <rschuster[at]tarent[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.oda.driver.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Generic command to retrieve arbitrary cna tree elements and their property values.
 * 
 * <p>The command is to be used by reports who wish to access elements and their property values.</p>
 * 
 * @author Robert Schuster
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class LoadEntityValues extends GenericCommand {

	private List<List<Object>> result;
	
	private String typeId;
	private String[] propertyTypes;
	private Class<?>[] classes;
	
	public LoadEntityValues(String typeId, String[] propertyTypes) {
		this (typeId, propertyTypes, new Class[0]);
	}
	
	public LoadEntityValues(String typeId, String[] propertyTypes, Class<?>[] classes) {
		this.typeId = typeId;
		this.propertyTypes = (propertyTypes != null) ? propertyTypes.clone() : null;
		this.classes = (classes != null) ? classes.clone() : null;
	}
	
	
    @Override
    @SuppressWarnings("unchecked")
	public void execute() {
		IBaseDao<CnATreeElement, Serializable> dao = (IBaseDao<CnATreeElement, Serializable>) getDaoFactory().getDAO(typeId);
		List<CnATreeElement> elements = dao.findAll();
		
		result = new ArrayList<List<Object>>(elements.size());
		for (CnATreeElement element : elements)
		{
			Entity e = element.getEntity();
			result.add(retrievePropertyValues(e, propertyTypes, classes, false));
		}
	}
	
	/**
	 * Retrieves the values of the properties of the given entity and returns them as a list of strings.
	 * 
	 * <p>Note: The method is purposely written in a way that it can be reused from other parts of the application.</p>
	 */
	public static List<Object> retrievePropertyValues(Entity e, String[] propertyTypes, Class<?>[] classes)
	{
		return retrievePropertyValues(e, propertyTypes, classes, false);
	}
	
	public static List<Object> convertValuesToList(Entity entity, String[] propertyTypes) {
	    ArrayList<Object> values = new ArrayList<Object>(propertyTypes.length);       
	    for (String propertyTypeId : propertyTypes) {
	        PropertyType pType = HUITypeFactory.getInstance().getPropertyType(entity.getEntityType(), propertyTypeId);
	        if(pType.getInputName().equals("numericoption"))
            {
                values.add(entity.getNumericValue(propertyTypeId));
            } else {
                values.add(entity.getSimpleValue(propertyTypeId));
            }
	    }       
	    return values;
	}
	
	public static List<Object> retrievePropertyValues(Entity entity, String[] propertyTypes, Class<?>[] classes, boolean mapNumericalOptionValues){
        ArrayList<Object> values = new ArrayList<Object>(propertyTypes.length);
        
        int i = 0;
        for (String name : propertyTypes)
        {
            PropertyType pType = HUITypeFactory.getInstance().getPropertyType(entity.getEntityType(), name);
            if(pType==null) {
                continue;
            }
            
            Class<?> c = (i >= classes.length ? null : classes[i]);
            if ((c == null || c == String.class) && (!mapNumericalOptionValues || !pType.getInputName().equals("numericoption"))){
                if(pType.getInputName().equals("numericoption"))
                {
                    values.add(entity.getNumericValue(name));
                } else {
                    values.add(entity.getSimpleValue(name));
                }
            } else if(pType.getInputName().equals("numericoption") && mapNumericalOptionValues){
                values.add(pType.getNameForValue(Integer.parseInt(entity.getValue(name))));
            } else if (c == Integer.class){
                values.add(String.valueOf(entity.getInt(name)));
            } else {
                throw new IllegalArgumentException("Invalid class for propertyType '" + name + "'.");
            }
            i++;
        }
        
        return values;	    
	}
	
	public List<List<Object>> getResult()
	{
		return result;
	}

}
