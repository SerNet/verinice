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
package sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis;

import java.util.List;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.GefaehrdungsUtil;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class SelectRiskTreatment extends GenericCommand {

	private String gefaehrdungAlternative;
	private GefaehrdungsUmsetzung gefaehrdungsUmsetzung;
	private Integer listsDbId;
	private FinishedRiskAnalysisLists finishedRiskLists;
	private CnATreeElement riskAnalysis;

	/**
	 * @param dbId
	 * @param cnATreeElement 
	 * @param gefaehrdung
	 * @param gefaehrdungAlternativeA
	 */
	public SelectRiskTreatment(Integer dbId, CnATreeElement cnATreeElement, GefaehrdungsUmsetzung gefaehrdung,
			String gefaehrdungAlternative) {
		this.gefaehrdungAlternative = gefaehrdungAlternative;
		this.gefaehrdungsUmsetzung = gefaehrdung;
		this.listsDbId = dbId;
		this.riskAnalysis = cnATreeElement;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		// attach objects:
		finishedRiskLists = getDaoFactory().getDAO(FinishedRiskAnalysisLists.class)
			.findById(listsDbId);
	
		gefaehrdungsUmsetzung = (GefaehrdungsUmsetzung) getDaoFactory().getDAOForObject(gefaehrdungsUmsetzung)
			.merge(gefaehrdungsUmsetzung);
		
		getDaoFactory().getDAOForObject(riskAnalysis).reload(riskAnalysis, riskAnalysis.getDbId());
		
		gefaehrdungsUmsetzung.setAlternative(this.gefaehrdungAlternative);
		
		if (gefaehrdungAlternative.equals(GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_A)) {
			// add to items that need controls:
			if (!finishedRiskLists.getNotOKGefaehrdungsUmsetzungen()
					.contains(gefaehrdungsUmsetzung)) {
				finishedRiskLists.getNotOKGefaehrdungsUmsetzungen().add(gefaehrdungsUmsetzung);
			}
		}
		else {
			// remove from items that need controls:
			if (finishedRiskLists.getNotOKGefaehrdungsUmsetzungen()
					.contains(gefaehrdungsUmsetzung)) {
				List<GefaehrdungsUmsetzung> found = GefaehrdungsUtil
					.removeBySameId(finishedRiskLists.getNotOKGefaehrdungsUmsetzungen(), gefaehrdungsUmsetzung);
				
				for (GefaehrdungsUmsetzung foundItem : found) {
					riskAnalysis.removeChild(foundItem);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		HydratorUtil.hydrateElement(getDaoFactory().getDAO(FinishedRiskAnalysisLists.class), finishedRiskLists);
	
	}

	public FinishedRiskAnalysisLists getFinishedRiskLists() {
		return finishedRiskLists;
	}

}
