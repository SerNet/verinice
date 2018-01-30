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
package sernet.verinice.service.commands.risk;

import sernet.gs.model.Gefaehrdung;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.common.Permission;
import sernet.verinice.service.gstoolimport.GefaehrdungsUmsetzungFactory;

/**
 * Assign a threat to a risk analysis: create a new threat instance.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class AssociateGefaehrdungsUmsetzung extends GenericCommand implements IAuthAwareCommand {

    private Gefaehrdung currentGefaehrdung;
    private GefaehrdungsUmsetzung gefaehrdungsUmsetzung;
    private Integer listDbId;
    private FinishedRiskAnalysisLists finishedRiskLists;
    private Integer riskAnalysisDbId;

    private String language;

    private transient IAuthService authService;

    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    @Override
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }

	
    /**
     * @param finishedRiskLists
     * @param currentGefaehrdung
     * @param integer
     * @param finishedRiskAnalysis
     */
    public AssociateGefaehrdungsUmsetzung(Integer listDbId, Gefaehrdung currentGefaehrdung, Integer riskAnalysisDbId, String language) {
        this.currentGefaehrdung = currentGefaehrdung;
        this.listDbId = listDbId;
        this.riskAnalysisDbId = riskAnalysisDbId;
        this.language = language;
    }



    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        FinishedRiskAnalysis riskAnalysis = getDaoFactory().getDAO(FinishedRiskAnalysis.class).findById(riskAnalysisDbId);
        finishedRiskLists = getDaoFactory().getDAO(FinishedRiskAnalysisLists.class).findById(listDbId);

        gefaehrdungsUmsetzung = GefaehrdungsUmsetzungFactory.build(null, currentGefaehrdung, language);
        getDaoFactory().getDAO(GefaehrdungsUmsetzung.class).saveOrUpdate(gefaehrdungsUmsetzung);

        if (authService.isPermissionHandlingNeeded()) {
            gefaehrdungsUmsetzung.setPermissions(Permission.clonePermissionSet(gefaehrdungsUmsetzung, riskAnalysis.getPermissions()));
        }

        finishedRiskLists.getAssociatedGefaehrdungen().add(gefaehrdungsUmsetzung);
    }

    public GefaehrdungsUmsetzung getGefaehrdungsUmsetzung() {
        return gefaehrdungsUmsetzung;
    }

    /*
     * (non-Javadoc)
     * 
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
