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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.ArrayList;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.ICnaItemRow;
import sernet.gs.ui.rcp.main.reports.PropertySelection;
import sernet.gs.ui.rcp.main.reports.Report;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.office.IOOTableRow;

public class ReportGetRowsCommand extends GenericCommand {

	private IBSIReport report;
	private PropertySelection shownPropertyTypes;
	private ArrayList<IOOTableRow> rows;


	public ReportGetRowsCommand(IBSIReport report,
			PropertySelection shownPropertyTypes) {
		this.report = report;
		this.shownPropertyTypes = shownPropertyTypes;
	}

	public void execute() {
		try {
			LoadBSIModel command = new LoadBSIModel();
			getCommandService().executeCommand(command);
			BSIModel model = command.getModel();
			((Report)report).setModel(model);
			
			// initialize items: 
			report.getItems();
			
			// convert to report rows:
			rows = report.getReport(shownPropertyTypes);
			
			// hydrate row element:
			for (IOOTableRow row : rows) {
				if (row instanceof ICnaItemRow) {
					CnATreeElement item = ((ICnaItemRow)row).getItem();
					HydratorUtil.hydrateElement(getDaoFactory().getDAOForObject(item), item, false);
				}
			}
			
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		report = null;
		shownPropertyTypes = null;
	}

	public ArrayList<IOOTableRow> getRows() {
		return rows;
	}

	

}
