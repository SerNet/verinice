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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;

public class LayerSummary extends CompletedLayerSummary {


	public void execute() {
		try {
			setSummary(getSchichtenSummary());
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}
	
	public Map<String, Integer> getSchichtenSummary() throws CommandException {
		Map<String, Integer> result = new HashMap<String, Integer>();
		ArrayList<BausteinUmsetzung> bausteine =getModel().getBausteine();
		for (BausteinUmsetzung baustein: bausteine) {
			Baustein baustein2 = getBaustein(baustein.getKapitel());
			if (baustein2 == null) {
				Logger.getLogger(this.getClass()).debug("Kein Baustein gefunden für ID" + baustein.getId());
				continue;
			}
			
			String schicht = Integer.toString(baustein2.getSchicht());

			if (result.get(schicht) == null)
				result.put(schicht, baustein.getMassnahmenUmsetzungen().size());
			else {
				Integer count = result.get(schicht);
				result.put(schicht, count + baustein.getMassnahmenUmsetzungen().size());
			}
		}
		return result;
	}

	
	
}
