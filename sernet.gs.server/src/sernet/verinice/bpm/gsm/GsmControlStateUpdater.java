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

import org.apache.log4j.Logger;

import sernet.verinice.graph.GraphElementLoader;
import sernet.verinice.graph.IGraphElementLoader;
import sernet.verinice.graph.IGraphService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GsmControlStateUpdater {
    
private static final Logger LOG = Logger.getLogger(GsmAssetScenarioRemover.class);
    
    private static final String[] typeIds = {Asset.TYPE_ID, IncidentScenario.TYPE_ID, Control.TYPE_ID};

    private static final String[] relationIds = {IncidentScenario.REL_INCSCEN_ASSET, Control.REL_CONTROL_INCSCEN};
    
    /**
     * Every instance of GsmProcessStarter has an exclusive instance of a IGraphService
     * Spring scope of graphService in veriniceserver-jbpm.xml is 'prototype'
     */
    private IGraphService graphService;
    
    private IBaseDao<CnATreeElement, Integer> elementDao;

    public void updateControlState(String orgUuid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating state of controls in org " + orgUuid + "...");
        }
        init(orgUuid);
        
    }

    private void init(String orgUuid) {
        if(orgUuid==null || orgUuid.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Org uuid is not set.");
            }
            return;
        }       
        Integer orgDbId = loadOrgDbId(orgUuid);        
        if(orgDbId==null) {
            LOG.warn("Org not found. Uuid is: " + orgUuid);
            return;
        }           
        initGraph(orgDbId);
    }
    
    private Integer loadOrgDbId(String orgUuid) {
        Integer dbId = null;
        CnATreeElement element = getElementDao().findByUuid(orgUuid, null);
        if(element!=null) {
            dbId = element.getDbId();
        }
        return dbId;
    }
    
    private void initGraph(Integer orgId) {
        try {
            IGraphElementLoader loader = new GraphElementLoader();
            loader.setTypeIds(typeIds);
            loader.setScopeId(orgId);           
            getGraphService().setLoader(loader);          
            getGraphService().setRelationIds(relationIds);
            getGraphService().create();          
        } catch(Exception e) {
            LOG.error("Error while initialization", e);
        }
    }

    public IGraphService getGraphService() {
        return graphService;
    }

    public void setGraphService(IGraphService graphService) {
        this.graphService = graphService;
    }

    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }
    
}
