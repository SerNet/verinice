/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
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
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.BsiReport;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;

public class ReportGetItemsCommand extends GenericCommand {

	private BsiReport report;
	private ArrayList<CnATreeElement> items;
	private Integer itverbundDbId;

	public ReportGetItemsCommand(BsiReport report) {
		this.report = report;
		itverbundDbId = report.getItverbund().getDbId();
		
	}
	
	public void execute() {
		try {

			LoadCnAElementById command = new LoadCnAElementById(ITVerbund.class, itverbundDbId);
			command = getCommandService().executeCommand(command);
			ITVerbund itverbund = (ITVerbund) command.getFound();
			
			// replace report's itverbund with the DB-connected instance: 
			((BsiReport)report).setItverbund(itverbund);
			
			IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(ITVerbund.class);
			
			items = ((IBSIReport)report).getItems();
			HydratorUtil.hydrateElements(dao, items, false);
			
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		
		report = null;
	}


	public ArrayList<CnATreeElement> getItems() {
		return items;
	}

}
