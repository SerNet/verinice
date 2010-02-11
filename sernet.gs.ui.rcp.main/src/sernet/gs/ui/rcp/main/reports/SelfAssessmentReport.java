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
package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;

import sernet.gs.ui.rcp.main.bsi.wizards.IHIbernateReport;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class SelfAssessmentReport implements IHIbernateReport {

	private ArrayList<IOOTableRow> rows;

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.wizards.ISimpleReport#getReport(sernet.gs.ui.rcp.main.reports.PropertySelection)
	 */
	public ArrayList<IOOTableRow> getReport(PropertySelection shownPropertyTypes) {
		return rows;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.wizards.ISimpleReport#getTitle()
	 */
	public String getTitle() {
		return "Self Asessment";
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.wizards.IHIbernateReport#getQuery()
	 */
	public String getQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.wizards.IHIbernateReport#getValues()
	 */
	public Object[] getValues() {
		// TODO Auto-generated method stub
		return null;
	}


}
