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
package sernet.verinice.report.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IncidentScenario;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AssetSzenarioDatamodel implements Serializable{

    private static final Logger LOG = LoggerFactory.getLogger(AssetSzenarioDatamodel.class);

    private static final String[] TYPE_IDS = new String[]{Asset.TYPE_ID, IncidentScenario.TYPE_ID};
    
    private ICommandService commandService;
    
    private Integer scopeId;
    
    private List<CnATreeElement> assetList;
    
    private Map<Integer,List<CnATreeElement>> scenarioMap;
    
    public void init() {
        try {
           doInit();            
        } catch( Exception e) {
            LOG.error("Error while creating data model", e);
        }       
    }
    
    private void doInit() throws CommandException {
        GraphCommand command = new GraphCommand();
        IGraphElementLoader loader = new GraphElementLoader();
        loader.setScopeId(getScopeId());
        loader.setTypeIds(TYPE_IDS);
        command.addLoader(loader);
        command.addRelationId(IncidentScenario.REL_INCSCEN_ASSET);
        command = getCommandService().executeCommand(command);          
        VeriniceGraph graph = command.getGraph();
        assetList = new ArrayList<CnATreeElement>(graph.getElements(Asset.TYPE_ID));
        scenarioMap = new Hashtable<Integer, List<CnATreeElement>>();
        for (CnATreeElement asset : assetList) {
            List<CnATreeElement> scenarioList =  new ArrayList<CnATreeElement>(graph.getLinkTargets(asset, IncidentScenario.REL_INCSCEN_ASSET));
            scenarioMap.put(asset.getDbId(), scenarioList);
        }
    }
    
    public List<CnATreeElement> getAssets() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAssets()...");
        }
        return assetList;
    }
    
    public List<CnATreeElement> getScenarios(Integer assetId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getScenarios for asset-id: " + assetId);
        }
        return scenarioMap.get(assetId);
    }

    public ICommandService getCommandService() {
        if( commandService == null) {
            commandService = ServiceFactory.lookupCommandService();
        }
        return commandService;
    }

    public Integer getScopeId() {
        return scopeId;
    }

    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }
   
}
