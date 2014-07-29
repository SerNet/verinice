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
package sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUtil;
import sernet.verinice.model.common.CnATreeElement;

public class LoadAssociatedGefaehrdungen extends GenericCommand {

	private CnATreeElement cnaElement;
	private List<Baustein> alleBausteine;
	private List<GefaehrdungsUmsetzung> associatedGefaehrdungen;

	public LoadAssociatedGefaehrdungen(CnATreeElement cnaElement) {
		this.cnaElement = cnaElement;
	}

	public void execute() {
		associatedGefaehrdungen = new ArrayList<GefaehrdungsUmsetzung>();
		
		IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOforTypedElement(cnaElement);
		dao.reload(cnaElement, cnaElement.getDbId());
		
		Set<CnATreeElement> children = cnaElement.getChildren();
		for (CnATreeElement cnATreeElement : children) {
			if (!(cnATreeElement instanceof BausteinUmsetzung)){
				continue;
			}
			BausteinUmsetzung bausteinUmsetzung = (BausteinUmsetzung) cnATreeElement;
			Baustein baustein = findBausteinForId(bausteinUmsetzung.getKapitel());
			if (baustein == null){
				continue;
			}
			for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
				if (!GefaehrdungsUtil.listContainsById(associatedGefaehrdungen, gefaehrdung)) {
					associatedGefaehrdungen.add(
							GefaehrdungsUmsetzungFactory.build(
									null, gefaehrdung));
				}
			}
		}
		
	}

	private Baustein findBausteinForId(String id) {
		if (alleBausteine == null) {
			LoadBausteine bstsCommand = new LoadBausteine();
			try {
				bstsCommand = getCommandService().executeCommand(bstsCommand);
			} catch (CommandException e) {
				throw new RuntimeCommandException(e);
			}
			alleBausteine = bstsCommand.getBausteine();
		}
		
		if(alleBausteine != null){ 
    		for (Baustein baustein : alleBausteine) {
    			if (baustein.getId().equals(id)){
    				return baustein;
    			}
    		}
		}
		return null;
	}

	public void clear() {
		alleBausteine = null;
		cnaElement = null;
	}

	public List<GefaehrdungsUmsetzung> getAssociatedGefaehrdungen() {
		return associatedGefaehrdungen;
	}

}
