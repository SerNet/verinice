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

import java.util.ArrayList;
import java.util.List;

import sernet.gs.model.Gefaehrdung;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUtil;
import sernet.verinice.model.common.HydratorUtil;

/**
 * Remove a threat instance from the list of associated threat instances for an object.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class DisassociateGefaehrdungsUmsetzung extends GenericCommand {

	private Gefaehrdung currentGefaehrdung;
	private Integer listDbId;
	private FinishedRiskAnalysisLists finishedRiskLists;
	private FinishedRiskAnalysis finishedRiskAnalysis;

	/**
	 * @param finishedRiskAnalysis 
	 * @param dbId id of the risk list to disassociate from
	 * @param currentGefaehrdung the threat instance to remove from the DB and the list
	 */
	public DisassociateGefaehrdungsUmsetzung(FinishedRiskAnalysis finishedRiskAnalysis, Integer dbId,
			Gefaehrdung currentGefaehrdung) {
		this.currentGefaehrdung = currentGefaehrdung;
		this.listDbId = dbId;
		this.finishedRiskAnalysis = finishedRiskAnalysis;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		finishedRiskLists = getDaoFactory().getDAO(FinishedRiskAnalysisLists.class).findById(listDbId);
		
		getDaoFactory().getDAO(FinishedRiskAnalysis.class)
			.reload(finishedRiskAnalysis, finishedRiskAnalysis.getDbId());
		
		List<GefaehrdungsUmsetzung> found = new ArrayList<GefaehrdungsUmsetzung>();
		List<GefaehrdungsUmsetzung> toRemove = GefaehrdungsUtil.removeBySameId(finishedRiskLists
				.getAssociatedGefaehrdungen(), currentGefaehrdung);
		found.addAll(toRemove);
		
		toRemove = GefaehrdungsUtil.removeBySameId(finishedRiskLists
				.getAllGefaehrdungsUmsetzungen(), currentGefaehrdung);
		found.addAll(toRemove);
		
		toRemove = GefaehrdungsUtil.removeBySameId(finishedRiskLists
				.getNotOKGefaehrdungsUmsetzungen(), currentGefaehrdung);
		found.addAll(toRemove);

		for (GefaehrdungsUmsetzung removeMe : toRemove) {
			finishedRiskAnalysis.removeChild(removeMe);
			removeMe.remove();
		}
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		// initialize lists properly before returning to client:
		HydratorUtil.hydrateElement(getDaoFactory().getDAO(FinishedRiskAnalysisLists.class), finishedRiskLists);
	}

	public FinishedRiskAnalysis getFinishedRiskAnalysis() {
		return finishedRiskAnalysis;
	}

	public FinishedRiskAnalysisLists getFinishedRiskLists() {
		return finishedRiskLists;
	}

}
