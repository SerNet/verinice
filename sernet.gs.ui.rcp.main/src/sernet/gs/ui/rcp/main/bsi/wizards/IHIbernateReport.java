/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.bsi.wizards;

import java.io.Serializable;
import java.util.ArrayList;

import sernet.gs.ui.rcp.main.reports.PropertySelection;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public interface IHIbernateReport extends Serializable {
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
	 * Get parameters for hibernate query
	 * @return
	 */
	public Object[] getValues();

	/**
	 * Get query used in hibernate callback.
	 * @return
	 */
	public String getQuery();
}
