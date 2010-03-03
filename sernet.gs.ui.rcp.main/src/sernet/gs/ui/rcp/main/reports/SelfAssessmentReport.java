/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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
import java.util.Properties;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class SelfAssessmentReport extends BsiReport  {

	/**
	 * @param reportProperties
	 */
	public SelfAssessmentReport() {
		super(null);
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.reports.IBSIReport#getItems()
	 */
	public ArrayList<CnATreeElement> getItems() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.reports.IBSIReport#getReport(sernet.gs.ui.rcp.main.reports.PropertySelection)
	 */
	public ArrayList<IOOTableRow> getReport(PropertySelection shownPropertyTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.reports.IBSIReport#getTitle()
	 */
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
