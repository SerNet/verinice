/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.gsm;

import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.service.commands.RemoveElement;

/**
 * Cleans up an organization after GSM process generation.
 * Cleaner finds {@link IncidentScenario}s without links to {@link Asset}s
 * deletes these scenarios and all linked Vulnerabilities and Controls.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class Cleaner {
    
    private static final Logger LOG = Logger.getLogger(Cleaner.class);
    
    private static final String[] TYPE_IDS = {Asset.TYPE_ID,IncidentScenario.TYPE_ID,Control.TYPE_ID,Vulnerability.TYPE_ID};
    private static final String[] RELATION_IDS = {IncidentScenario.REL_INCSCEN_ASSET,IncidentScenario.REL_INCSCEN_VULNERABILITY,Control.REL_CONTROL_INCSCEN};
    
    /**
     * Every instance of Cleaner has an exclusive instance of a IGraphService
     * Spring scope of graphService in veriniceserver-jbpm.xml is 'prototype'
     */
    private IGraphService graphService;
    
    private ICommandService commandService;

    /**
     * @param orgId
     */
    public void cleanUpOrganization(Integer orgId) {
        try {
            initGraph(orgId);
            Set<CnATreeElement> scenarioSet = getGraph().getElements(IncidentScenario.TYPE_ID);
            for (CnATreeElement scenario : scenarioSet) {
                int numberOfLinks = getGraph().getLinkTargets(scenario, IncidentScenario.REL_INCSCEN_ASSET).size();
                if(numberOfLinks==0) {
                    deleteScenarioAndControl(scenario);
                }
            }
        } catch (CommandException e) {
            LOG.error("Error while ceaning up organization.", e);
        }
    }
    

    private void deleteScenarioAndControl(CnATreeElement scenario) throws CommandException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting scenario (incl. linked control and vulnerability): " + scenario.getTitle() );
        }
        Set<CnATreeElement> controlSet = getGraph().getLinkTargets(scenario, Control.REL_CONTROL_INCSCEN);
        for (CnATreeElement control : controlSet) {
            RemoveElement<CnATreeElement> command = new RemoveElement<CnATreeElement>(control);
            command = getCommandService().executeCommand(command);
        }
        Set<CnATreeElement> vulnerabilitySet = getGraph().getLinkTargets(scenario, IncidentScenario.REL_INCSCEN_VULNERABILITY);
        for (CnATreeElement vulnerability : vulnerabilitySet) {
            RemoveElement<CnATreeElement> command = new RemoveElement<CnATreeElement>(vulnerability);
            command = getCommandService().executeCommand(command);
        }
        RemoveElement<CnATreeElement> command = new RemoveElement<CnATreeElement>(scenario);
        command = getCommandService().executeCommand(command);
    }

    private void initGraph(Integer orgId) {
        try {
            IGraphElementLoader loader = new GraphElementLoader();
            loader.setTypeIds(TYPE_IDS);
            loader.setScopeId(orgId);
            
            getGraphService().setLoader(loader);
            
            getGraphService().setRelationIds(RELATION_IDS);
            getGraphService().create();          
        } catch(Exception e) {
            LOG.error("Error while initialization", e);
        }
    }
    
    private VeriniceGraph getGraph() {
        return getGraphService().getGraph();
    }

    public IGraphService getGraphService() {
        return graphService;
    }

    public void setGraphService(IGraphService graphService) {
        this.graphService = graphService;
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }
    
}
