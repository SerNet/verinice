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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PropertyGroup implements IEntityElement {
    private String id;
    private String name;
    private Map<String, PropertyType> propertyTypesById = new LinkedHashMap<>();
    private Set<DependsType> dependencies = new HashSet<>();
    private String tags;

    public void addPropertyType(PropertyType prop) {
        propertyTypesById.put(prop.getId(), prop);
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

    public Collection<PropertyType> getPropertyTypes() {
        return propertyTypesById.values();
    }

    public PropertyType getPropertyType(String id) {
        return propertyTypesById.get(id);
    }

    public void setDependencies(Set<DependsType> set) {
        this.dependencies = set;
    }

    public Set<DependsType> getDependencies() {
        return dependencies;
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

    @Override
    public String toString() {
        return "PropertyGroup [id=" + id + ", name=" + name + "]";
    }
}
