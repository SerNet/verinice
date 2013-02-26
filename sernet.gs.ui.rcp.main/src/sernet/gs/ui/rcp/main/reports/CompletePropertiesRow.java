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

import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Row to print out all properties of a given item.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class CompletePropertiesRow implements IOOTableRow, ICnaItemRow {

	private List<String> columns = new ArrayList<String>();
	
	public CompletePropertiesRow(CnATreeElement item) {
		addAllProperties(item);
	}

	/**
	 * @param item
	 * @return
	 */
	private void addAllProperties(CnATreeElement item) {
		if (item.getEntityType() == null){
			return;
		}
		columns.add(item.getEntityType().getName());
		columns.add(item.getUuid());
		columns.add(item.getParent().getUuid());
		
		List<PropertyType> propertyTypes = item.getEntityType().getPropertyTypes();
		for (PropertyType propertyType : propertyTypes) {
			columns.add(item.getEntity().getSimpleValue(propertyType.getId()));
		}
		
		List<PropertyGroup> groups = item.getEntityType().getPropertyGroups();
		for (PropertyGroup propertyGroup : groups) {
			propertyTypes = propertyGroup.getPropertyTypes();
			for (PropertyType propertyType : propertyTypes) {
				columns.add(item.getEntity().getSimpleValue(propertyType.getId()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.office.IOOTableRow#getCellAsDouble(int)
	 */
	public double getCellAsDouble(int column) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.office.IOOTableRow#getCellAsString(int)
	 */
	public String getCellAsString(int column) {
		return this.columns.get(column);
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.office.IOOTableRow#getCellType(int)
	 */
	public int getCellType(int column) {
		return IOOTableRow.CELL_TYPE_STRING;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.office.IOOTableRow#getNumColumns()
	 */
	public int getNumColumns() {
		return this.columns.size();
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.office.IOOTableRow#getRowStyle()
	 */
	public String getRowStyle() {
		return IOOTableRow.ROW_STYLE_ELEMENT;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.reports.ICnaItemRow#getItem()
	 */
	public CnATreeElement getItem() {
		return null;
	}

	
	
}
