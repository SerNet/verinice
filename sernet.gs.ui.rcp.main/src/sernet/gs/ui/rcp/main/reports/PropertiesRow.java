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
package sernet.gs.ui.rcp.main.reports;

import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * Returns the given properties as columns for OpenOffice export.
 * 
 * @author koderman@sernet.de
 *
 */
public class PropertiesRow implements IOOTableRow, ICnaItemRow {

	private CnATreeElement item;
	private List<String> properties;
	private String style;
	

	public PropertiesRow(CnATreeElement item, List<String> properties, String style) {
		this.item = item;
		this.properties = properties;
		this.style = style;
	}
	
	public double getCellAsDouble(int column) {
		return 0;
	}

	public String getCellAsString(int column) {
		return item.getEntity().getSimpleValue(properties.get(column));
	}

	public int getCellType(int column) {
		return IOOTableRow.CELL_TYPE_STRING;
	}

	public int getNumColumns() {
		if (properties == null)
			return 0;
		return properties.size();
	}
	
	public String getRowStyle() {
		return style;
	}

	public CnATreeElement getItem() {
		return item;
	}
	
}
