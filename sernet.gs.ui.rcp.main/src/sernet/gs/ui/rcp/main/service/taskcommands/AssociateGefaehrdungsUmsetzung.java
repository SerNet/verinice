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

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class AssociateGefaehrdungsUmsetzung extends GenericCommand {

	private Gefaehrdung currentGefaehrdung;
	private GefaehrdungsUmsetzung gefaehrdungsUmsetzung;
	private Integer listDbId;
	private FinishedRiskAnalysisLists finishedRiskLists;

	/**
	 * @param finishedRiskLists 
	 * @param currentGefaehrdung
	 */
	public AssociateGefaehrdungsUmsetzung(Integer listDbId, Gefaehrdung currentGefaehrdung) {
		this.currentGefaehrdung = currentGefaehrdung;
		this.listDbId = listDbId;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		finishedRiskLists = getDaoFactory().getDAO(FinishedRiskAnalysisLists.class).findById(listDbId);
		
		gefaehrdungsUmsetzung = GefaehrdungsUmsetzungFactory.build(null, currentGefaehrdung);
		getDaoFactory().getDAO(GefaehrdungsUmsetzung.class).saveOrUpdate(gefaehrdungsUmsetzung);
		finishedRiskLists.getAssociatedGefaehrdungen().add(gefaehrdungsUmsetzung);
	}

	public GefaehrdungsUmsetzung getGefaehrdungsUmsetzung() {
		return gefaehrdungsUmsetzung;
	}

	public FinishedRiskAnalysisLists getFinishedRiskLists() {
		return finishedRiskLists;
	}

}
