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
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.iso27k.service.IRiskAnalysisService;
import sernet.verinice.iso27k.service.RiskAnalysisServiceGraph;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

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
    
    private transient Logger log = Logger.getLogger(RunRiskAnalysisCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(RunRiskAnalysisCommand.class);
        }
        return log;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {      
        IBaseDao<CnALink, Serializable> cnaLinkDao = getDaoFactory().getDAO(CnALink.class);
        VeriniceGraph graph = loadGraph();       
        IRiskAnalysisService ra = new RiskAnalysisServiceGraph(graph, cnaLinkDao);
        
        // update asset values (business impact, CIA):
        // done on every save, no need to do it here
        Set<CnATreeElement> scenarios = graph.getElements(IncidentScenario.TYPE_ID);
        for (CnATreeElement scenario : scenarios) {
            if(getLog().isDebugEnabled()){
                getLog().debug("Determine Probability for Scenario:\t" 
                        + scenario.getTitle());
            }
            ra.determineProbability((IncidentScenario) scenario);
        }
     
        Set<CnATreeElement> assets = graph.getElements(Asset.TYPE_ID);
        for (CnATreeElement asset : assets) {
            if(getLog().isDebugEnabled()){
                getLog().debug("Resetting Risk for Asset:\t" + asset.getTitle());
            }
            ra.resetRisks((Asset) asset);
        }

        // determine risk originating from scenarios for all linked assets:
        for (CnATreeElement scenario : scenarios) {
            if(getLog().isDebugEnabled()){
                getLog().debug("Determine Risk for Scenario:\t" + scenario.getTitle());
            }            
            try {
                ra.determineRisks((IncidentScenario) scenario);
            } catch (CommandException e) {
                getLog().error("Error while determine risk", e);
            }
        }
        
        
    }

    private VeriniceGraph loadGraph() {
        try {
            GraphCommand graphCommand = new GraphCommand();
            IGraphElementLoader loader = new GraphElementLoader();
            loader.setTypeIds(new String[]{Asset.TYPE_ID, IncidentScenario.TYPE_ID, Control.TYPE_ID, Threat.TYPE_ID, Vulnerability.TYPE_ID});
            graphCommand.addLoader(loader);
            graphCommand = getCommandService().executeCommand(graphCommand);      
        return graphCommand.getGraph();
        } catch (CommandException e) {
            getLog().error("Error while loading graph", e);
            throw new RuntimeCommandException(e);
        }
    }
    
   
    

}


