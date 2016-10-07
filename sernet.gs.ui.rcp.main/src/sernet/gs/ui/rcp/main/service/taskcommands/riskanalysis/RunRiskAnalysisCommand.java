/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.iso27k.service.IRiskAnalysisService;
import sernet.verinice.iso27k.service.RiskAnalysisServiceImpl;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IncidentScenario;

/**
 * 
 * Run ISO 27005-style risk analysis.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RunRiskAnalysisCommand extends GenericCommand implements INoAccessControl {
    
    private static final Logger LOG = Logger.getLogger(RunRiskAnalysisCommand.class);

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        
        RetrieveInfo ri = new RetrieveInfo();
        ri.setProperties(true).setLinksDown(true).setLinksUp(true);
        IRiskAnalysisService ra = new RiskAnalysisServiceImpl();
        
        // update asset values (business impact, CIA):
        // done on every save, no need to do it here
        
        // determine scenario probabilities:
        IBaseDao<IncidentScenario, Serializable> scenarioDAO 
            = getDaoFactory().getDAO(IncidentScenario.UNSECURE_TYPE_ID);
        
        List<IncidentScenario> scenarios = scenarioDAO.findAll(ri);
        for (IncidentScenario scenario : scenarios) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Determine Probability for Scenario:\t" 
                        + scenario.getTitle());
            }
            ra.determineProbability(scenario);
        }
        
        // reset all assets' risk values:
        IBaseDao<Asset, Serializable> assetDAO 
            = getDaoFactory().getDAO(Asset.UNSECURE_TYPE_ID);
        List<Asset> assets = assetDAO.findAll(ri);
        for (Asset asset : assets) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Resetting Risk for Asset:\t" + asset.getTitle());
            }
            ra.resetRisks(asset);
        }

        // determine risk originating from scenarios for all linked assets:
        for (IncidentScenario scenario : scenarios) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Determine Risk for Scenario:\t" + scenario.getTitle());
            }            
            ra.determineRisks(scenario);
        }
        
        
    }

}


