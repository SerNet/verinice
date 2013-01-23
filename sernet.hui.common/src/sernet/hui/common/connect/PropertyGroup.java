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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropertyGroup implements IEntityElement {
	private String id;
	private String name;
	private List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
	private Set<DependsType> dependencies = new HashSet<DependsType>();
    private String tags;
	
	public void addPropertyType(PropertyType prop) {
		propertyTypes.add(prop);
	}
	
	@Override
    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<PropertyType> getPropertyTypes() {
		return propertyTypes;
	}
	public void setPropertyTypes(List<PropertyType>  propertyTypes) {
		this.propertyTypes = propertyTypes;
	}
	
	public PropertyType getPropertyType(String id) {
		for (PropertyType type : propertyTypes) {
			if (type.getId().equals(id)){
				return type;
			}
		}
		return null;
	}

	public void setDependencies(Set<DependsType> set) {
		this.dependencies = set;
	}

    /**
     * @param attribute
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return tags;
    }
}
