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

import java.util.List;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadGenericElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveGenericElement;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;

public class OwnGefaehrdungHome {
	
	private ICommandService commandService;
	private static OwnGefaehrdungHome instance;

	private OwnGefaehrdungHome() {
		commandService = ServiceFactory.lookupCommandService();
	}
	
	public synchronized static OwnGefaehrdungHome getInstance() {
		if (instance == null)
			instance = new OwnGefaehrdungHome();
		return instance;
	}
	
	public void save(OwnGefaehrdung gef) throws Exception {
		SaveElement command = new SaveElement(gef);
		command = commandService.executeCommand(command);
	}
	
	public void remove(OwnGefaehrdung gef) throws Exception {
		RemoveGenericElement<OwnGefaehrdung> command = new RemoveGenericElement<OwnGefaehrdung>(gef);
		command = commandService.executeCommand(command);
	}
	
	public List<OwnGefaehrdung> loadAll() throws Exception {
		LoadGenericElementByType<OwnGefaehrdung> command
			= new LoadGenericElementByType<OwnGefaehrdung>(OwnGefaehrdung.class);
		command = commandService.executeCommand(command);
		return command.getElements();
	}
	
	
}
