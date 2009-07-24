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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;

public class CompletedLayerSummary extends MassnahmenSummary {


	private List<Baustein> bausteine;

	public void execute() {
		try {
			setSummary(getCompletedSchichtenSummary());
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}
	
	public Map<String, Integer> getCompletedSchichtenSummary() throws CommandException {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		ArrayList<BausteinUmsetzung> bausteine = getModel().getBausteine();
		for (BausteinUmsetzung baustein: bausteine) {
			Baustein baustein2 = getBaustein(baustein.getKapitel());
			if (baustein2 == null) {
				Logger.getLogger(this.getClass()).debug("Kein Baustein gefunden für ID" + baustein.getId());
				continue;
			}
			String schicht = Integer.toString(baustein2.getSchicht());
			int umgesetztSum = 0;
			for (MassnahmenUmsetzung ums: baustein.getMassnahmenUmsetzungen()) {
				if (ums.isCompleted())
					umgesetztSum++;
			}
			
			if (result.get(schicht) == null)
				result.put(schicht, umgesetztSum);
			else {
				Integer count = result.get(schicht);
				result.put(schicht, count + umgesetztSum);
			}
		}
		return result;
	}

	protected Baustein getBaustein(String kapitel) throws CommandException {
		if (bausteine == null) {
			LoadBausteine command = new LoadBausteine();
			command = getCommandService().executeCommand(command);
			this.bausteine = command.getBausteine();
		}
		for (Baustein baustein : bausteine) {
			if (baustein.getId().equals(kapitel))
				return baustein;
		}
		return null;
		
	}

	
	
}
