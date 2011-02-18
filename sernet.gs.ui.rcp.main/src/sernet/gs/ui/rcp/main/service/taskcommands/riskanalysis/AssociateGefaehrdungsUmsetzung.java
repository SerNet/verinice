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

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.common.Permission;

/**
 * Assign a threat to a risk analysis: create a new threat instance.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class AssociateGefaehrdungsUmsetzung extends GenericCommand implements IAuthAwareCommand {

	private Gefaehrdung currentGefaehrdung;
	private GefaehrdungsUmsetzung gefaehrdungsUmsetzung;
	private Integer listDbId;
	private FinishedRiskAnalysisLists finishedRiskLists;
	private Integer riskAnalysisDbId;
	
	private transient IAuthService authService;

	
	public IAuthService getAuthService() {
		return authService;
	}

	public void setAuthService(IAuthService service) {
		this.authService = service;
	}
	
	/**
	 * @param finishedRiskLists 
	 * @param currentGefaehrdung
	 * @param integer 
	 * @param finishedRiskAnalysis 
	 */
	public AssociateGefaehrdungsUmsetzung(Integer listDbId, Gefaehrdung currentGefaehrdung, Integer riskAnalysisDbId) {
		this.currentGefaehrdung = currentGefaehrdung;
		this.listDbId = listDbId;
		this.riskAnalysisDbId = riskAnalysisDbId;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		FinishedRiskAnalysis riskAnalysis = getDaoFactory().getDAO(FinishedRiskAnalysis.class).findById(riskAnalysisDbId);
		finishedRiskLists = getDaoFactory().getDAO(FinishedRiskAnalysisLists.class).findById(listDbId);
		
		gefaehrdungsUmsetzung = GefaehrdungsUmsetzungFactory.build(null, currentGefaehrdung);
		getDaoFactory().getDAO(GefaehrdungsUmsetzung.class).saveOrUpdate(gefaehrdungsUmsetzung);
		
		if (authService.isPermissionHandlingNeeded())
		{
			gefaehrdungsUmsetzung.setPermissions(
				Permission.clonePermissionSet(
						gefaehrdungsUmsetzung,
						riskAnalysis.getPermissions()));
		}
		
		finishedRiskLists.getAssociatedGefaehrdungen().add(gefaehrdungsUmsetzung);
	}

	public GefaehrdungsUmsetzung getGefaehrdungsUmsetzung() {
		return gefaehrdungsUmsetzung;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		// initialize lists properly before returning to client:
		HydratorUtil.hydrateElement(getDaoFactory().getDAO(FinishedRiskAnalysisLists.class), finishedRiskLists);
	}

	public FinishedRiskAnalysisLists getFinishedRiskLists() {
		return finishedRiskLists;
	}

}
