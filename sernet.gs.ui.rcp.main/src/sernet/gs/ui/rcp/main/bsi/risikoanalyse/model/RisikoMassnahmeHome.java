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
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.List;

import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadGenericElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveGenericElement;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.taskcommands.FindRisikomassnahmeByNumber;

public class RisikoMassnahmeHome {
	
	private static RisikoMassnahmeHome instance;
	private ICommandService commandService;

	private RisikoMassnahmeHome() {
		commandService = ServiceFactory.lookupCommandService();
	}
	
	public synchronized static RisikoMassnahmeHome getInstance() {
		if (instance == null)
			instance = new RisikoMassnahmeHome();
		return instance;
	}
	
	
	public RisikoMassnahme save(RisikoMassnahme mn) throws Exception {
		SaveElement<RisikoMassnahme> command = new SaveElement<RisikoMassnahme>(mn);
		command = commandService.executeCommand(command);
		return command.getElement();
	}
	
	public void remove(RisikoMassnahme mn) throws Exception {
		RemoveGenericElement<RisikoMassnahme> command = new RemoveGenericElement<RisikoMassnahme>(mn);
		command = commandService.executeCommand(command);
	}
	
	public List<RisikoMassnahme> loadAll() throws Exception {
		LoadGenericElementByType<RisikoMassnahme> command 
			= new LoadGenericElementByType<RisikoMassnahme>(RisikoMassnahme.class);
		command = commandService.executeCommand(command);
		return command.getElements();
	}
	
	public RisikoMassnahme loadByNumber(String number) throws CommandException {
		FindRisikomassnahmeByNumber command = new FindRisikomassnahmeByNumber(number);
		command = commandService.executeCommand(command);
		return command.getMassnahme();
	}		
	
}
