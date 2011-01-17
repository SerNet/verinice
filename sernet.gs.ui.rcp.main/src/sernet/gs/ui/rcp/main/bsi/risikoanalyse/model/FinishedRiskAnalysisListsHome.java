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
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveGenericElement;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.FindRiskAnalysisListsByParentID;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;

public class FinishedRiskAnalysisListsHome {
	
	
	private static FinishedRiskAnalysisListsHome instance;

	private FinishedRiskAnalysisListsHome() {
	}
	
	public synchronized static FinishedRiskAnalysisListsHome getInstance() {
		if (instance == null)
			instance = new FinishedRiskAnalysisListsHome();
		return instance;
	}
	
	public void saveNew(FinishedRiskAnalysisLists list) throws Exception {
		SaveElement<FinishedRiskAnalysisLists> command = new SaveElement<FinishedRiskAnalysisLists>(list);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
	}

	public FinishedRiskAnalysisLists update(FinishedRiskAnalysisLists list) throws Exception {
		SaveElement<FinishedRiskAnalysisLists> command = new SaveElement<FinishedRiskAnalysisLists>(list);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getElement();
	}
	
	public void remove(FinishedRiskAnalysisLists list) throws Exception {
		RemoveGenericElement<FinishedRiskAnalysisLists> command = new RemoveGenericElement<FinishedRiskAnalysisLists>(list);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
	}
	
	public FinishedRiskAnalysisLists loadById(int id) throws CommandException {
		FindRiskAnalysisListsByParentID command = new FindRiskAnalysisListsByParentID(id);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getFoundLists();
	}
}
