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
package sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.GefaehrdungsUtil;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * Mark a threat as "OK" which menas it does not need further evaluation
 * during risk analysis.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class PositiveEstimateGefaehrdung extends GenericCommand {

	private Integer listsDbId;
	private GefaehrdungsUmsetzung gefaehrdungsUmsetzung;
	private CnATreeElement finishedRiskAnalysis;
	private FinishedRiskAnalysisLists finishedRiskLists;

	/**
	 * @param dbId
	 * @param gefaehrdungsUmsetzung
	 * @param finishedRiskAnalysis
	 */
	public PositiveEstimateGefaehrdung(Integer dbId,
			GefaehrdungsUmsetzung gefaehrdungsUmsetzung,
			CnATreeElement finishedRiskAnalysis) {
		this.finishedRiskAnalysis = finishedRiskAnalysis;
		this.gefaehrdungsUmsetzung = gefaehrdungsUmsetzung;
		this.listsDbId = dbId;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		// attach objects:
		finishedRiskLists = getDaoFactory().getDAO(FinishedRiskAnalysisLists.class)
			.findById(listsDbId);
	
		getDaoFactory().getDAOForObject(finishedRiskAnalysis)
			.reload(finishedRiskAnalysis, finishedRiskAnalysis.getDbId());
		
		gefaehrdungsUmsetzung = (GefaehrdungsUmsetzung) getDaoFactory().getDAOForObject(gefaehrdungsUmsetzung)
			.merge(gefaehrdungsUmsetzung);
		
		gefaehrdungsUmsetzung.setOkay(true);
		finishedRiskAnalysis.removeChild(gefaehrdungsUmsetzung);

		GefaehrdungsUtil.removeBySameId(finishedRiskLists
				.getAllGefaehrdungsUmsetzungen(), gefaehrdungsUmsetzung);
		
		GefaehrdungsUtil.removeBySameId(finishedRiskLists
				.getNotOKGefaehrdungsUmsetzungen(), gefaehrdungsUmsetzung);
		
		
	}

	public GefaehrdungsUmsetzung getGefaehrdungsUmsetzung() {
		return gefaehrdungsUmsetzung;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		finishedRiskAnalysis = null;
		HydratorUtil.hydrateElement(getDaoFactory().getDAO(FinishedRiskAnalysisLists.class), finishedRiskLists);
	
	}

	public FinishedRiskAnalysisLists getLists() {
		return finishedRiskLists;
	}
}
