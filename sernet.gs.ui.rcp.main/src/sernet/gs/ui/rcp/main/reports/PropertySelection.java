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
package sernet.gs.ui.rcp.main.reports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Which properties of an item should be output as columns?
 * The selection made by the user or programattically is
 * saved here.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class PropertySelection implements Serializable {
	private Map<String, List<String>> shownPropertyTypes;
	
	public PropertySelection() {
		this.shownPropertyTypes = new HashMap<String, List<String>>();
	}

	public List<String> get(String entityType) {
		List<String> fieldList = shownPropertyTypes.get(entityType);
		return fieldList != null ? fieldList : new ArrayList<String>();
	}


	public void add(String entityTypeId, String propertyTypeId) {
		List<String> properties = shownPropertyTypes.get(entityTypeId);
		if (properties == null) {
			properties = new ArrayList<String>();
			shownPropertyTypes.put(entityTypeId, properties);
		}
		if (! properties.contains(propertyTypeId)){
			properties.add(propertyTypeId);
		}
	}
}
