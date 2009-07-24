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
package sernet.gs.ui.rcp.main.service.statscommands;

import java.util.HashMap;
import java.util.Map;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;

public class UmsetzungSummary extends MassnahmenSummary {


	public void execute() {
		super.execute();
		setSummary(getUmsetzungenSummary());
	}
	
	public Map<String, Integer> getUmsetzungenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		LoadCnAElementByType<MassnahmenUmsetzung> command = new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class);
		try {
			command = getCommandService().executeCommand(command);
			for (MassnahmenUmsetzung ums : command.getElements()) {
				if (result.get(ums.getUmsetzung()) == null)
					result.put(ums.getUmsetzung(), 0);
				Integer count = result.get(ums.getUmsetzung());
				result.put(ums.getUmsetzung(), ++count);
			}
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		
		return result;
	}
	
	
}
