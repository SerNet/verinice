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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.wizards.IHIbernateReport;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class HibernateReportGetRowsCommand extends GenericCommand {

	private IHIbernateReport report;
	private ArrayList<IOOTableRow> rows;

	/**
	 * @param report
	 */
	public HibernateReportGetRowsCommand(IHIbernateReport report) {
		this.report = report;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		String hqlQuery = report.getQuery();
		Object[] values = report.getValues();
		IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
		//dao.findByCallback(hcb)
		// FIXME akoderman finish self assessment report
	}

	/**
	 * @return
	 */
	public ArrayList<IOOTableRow> getRows() {
		return rows;
	}

}
