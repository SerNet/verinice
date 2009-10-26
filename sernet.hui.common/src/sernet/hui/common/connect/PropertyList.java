/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.common.connect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * All properties of a given type for one entity.
 * 
 * @author koderman@sernet.de
 *
 */
public class PropertyList implements Serializable  {
	private List<Property> properties;
	
	private Integer dbId;
	
	private String uuid;
	
	
	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public PropertyList(int size) {
		this();
		properties = new ArrayList<Property>(size);
	}

	public PropertyList() {
		uuid = UUID.randomUUID().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this == obj
				|| (obj instanceof PropertyList
					&& this.uuid.equals(((PropertyList)obj).getUuid())
					)
				);
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public void add(Property prop) {
		properties.add(prop);
	}
	
	
	public Property getProperty(int idx)  {
			if (properties != null && properties.size() > 0)
				return properties.get(idx);
			else
				return null;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
