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

import java.io.Serializable;
import java.util.ArrayList;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * Interface for different BSI report types that will be exported to OpenOffice.
 * 
 * @author koderman@sernet.de
 *
 */
public interface IBSIReport extends Serializable {
	
	public static String PROPERTY_FILE = "reports.properties";
	
	/**
	 * Get all items to include in the report as flat list,
	 * regardless of category.
	 * 
	 * @return
	 */
	public ArrayList<CnATreeElement> getItems();

	
	/**
	 * Get the title for this report. 
	 */
	public String getTitle();
	
	/**
	 * Get the complete report as data source for OpenOffice Calc export.
	 * 
	 * @param shownPropertyTypes
	 * @return
	 */
	public ArrayList<IOOTableRow> getReport(PropertySelection shownPropertyTypes);
		
	/**
	 * Which columns should be included in this report?
	 * 
	 * @param property_id
	 * @return
	 */
	public boolean isDefaultColumn(String property_id);


	
}
