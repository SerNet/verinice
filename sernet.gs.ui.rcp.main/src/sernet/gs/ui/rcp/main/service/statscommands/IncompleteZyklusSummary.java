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
import java.util.List;
import java.util.Map;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;

@SuppressWarnings("serial")
public class IncompleteZyklusSummary extends MassnahmenSummary {


	public void execute() {
		setSummary(getNotCompletedZyklusSummary());
	}
	
	public Map<String, Integer> getNotCompletedZyklusSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		LoadCnAElementByType<MassnahmenUmsetzung> command =
			new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class,
					new LoadCnAElementByType.HydrateCallback<MassnahmenUmsetzung>()
			{
				public void hydrate(List<MassnahmenUmsetzung> elements)
				{
					for (MassnahmenUmsetzung mu : elements)
					{
						mu.isCompleted();
						mu.getStufe();
					}
				}
			});
		
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		
		for (MassnahmenUmsetzung ums : command.getElements()) {
			if (ums.isCompleted())
				continue;
			String lz = ums.getLebenszyklus();
			
			if (lz == null || lz.length() <5)
				lz = "sonstige";
			
			if (result.get(lz) == null)
				result.put(lz, 0);
			Integer count = result.get(lz);
			result.put(lz, ++count);
		}
		return result;
	}
	
	
}
