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

import java.util.List;

import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Returns the given property names as columns for OpenOffice export.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class PropertyHeadersRow implements IOOTableRow, ICnaItemRow {

	private CnATreeElement item;
	private List<String> properties;
	private String style;
	

	public PropertyHeadersRow(CnATreeElement item, List<String> properties, String style) {
		this.item = item;
		this.properties = properties;
		this.style = style;
	}
	
	public double getCellAsDouble(int column) {
		return 0;
	}

	public String getCellAsString(int column) {
		PropertyType type = HUITypeFactory.getInstance().getPropertyType(
				item.getEntity().getEntityType(),
				properties.get(column));
		return type != null ? type.getName() : "";
	}

	public int getCellType(int column) {
		return IOOTableRow.CELL_TYPE_STRING;
	}

	public int getNumColumns() {
		if (properties == null){
			return 0;
		}
		return properties.size();
	}
	
	public String getRowStyle() {
		return style;
	}

	public CnATreeElement getItem() {
		return item;
	}
	
}
