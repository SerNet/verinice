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

import sernet.verinice.graph.GraphElementLoader;
import sernet.verinice.graph.IGraphElementLoader;
import sernet.verinice.graph.IGraphService;
import sernet.verinice.interfaces.bpm.IGsmValidationResult;
import sernet.verinice.model.bpm.GsmValidationResult;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * GsmProcessValidator is part of the GSM vulnerability tracking process.
 * Process definition is: gsm-ism-execute.jpdl.xml
 * 
 * GsmProcessValidator checks if an organization is valid
 * before starting the GSM process.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GsmProcessValidator {

    private static final Logger LOG = Logger.getLogger(GsmProcessValidator.class);
    
    private static final String[] typeIds = {AssetGroup.TYPE_ID, Asset.TYPE_ID, ControlGroup.TYPE_ID, Control.TYPE_ID};

    private static final String[] relationIds = {AssetGroup.REL_PERSON_ISO};
    
    private Integer orgId;
    
    private IGsmValidationResult result = new GsmValidationResult();
    
    /**
     * Every instance of GsmProcessStarter has an exclusive instance of a IGraphService
     * Spring scope of graphService in veriniceserver-jbpm.xml is 'prototype'
     */
    private IGraphService graphService;

    public IGsmValidationResult validateOrganization(Integer orgId) {
        this.orgId = orgId;
        initGraph();      
        validateAssets();
        validateControls();
        
        return result;
    }

    private void validateAssets() {
        Set<CnATreeElement> assetGroupSet = getGraphService().getElements(AssetGroup.TYPE_ID);
        for (CnATreeElement assetGroup : assetGroupSet) {
            if(!isTopLevel(assetGroup)) {
                result.oneMoreRelevantAssetGroup();
                if(!hasLinkedPerson(assetGroup)) {
                    result.addAssetGroupWithoutLinkedPerson(assetGroup.getTitle());
                }
            }
        }
        Set<CnATreeElement> assetSet = getGraphService().getElements(Asset.TYPE_ID);
        for (CnATreeElement asset : assetSet) {
            if(isUngrouped(asset)) {
                result.addUngroupedAsset(asset.getTitle());               
            }
        }
    }

    private void validateControls() {
        Set<CnATreeElement> controlSet = getGraphService().getElements(Control.TYPE_ID);
        for (CnATreeElement control : controlSet) {
            if(isUngrouped(control)) {
                result.addUngroupedControl(control.getTitle()); 
            }
        }
    }

    private boolean hasLinkedPerson(CnATreeElement assetGroup) {
        return !(getGraphService().getLinkTargets(assetGroup, AssetGroup.REL_PERSON_ISO).isEmpty());
    }
    
    private boolean isUngrouped(CnATreeElement asset) {
        return isTopLevel(asset.getParent());
    }

    private boolean isTopLevel(CnATreeElement assetGroup) {
        return assetGroup.getParentId().equals(this.orgId);
    }
    
    private void initGraph() {
        try { 
                   
            // Add elements with type of Array "typeIds" of organization with id "orgId" to the graph
            IGraphElementLoader loader1 = new GraphElementLoader();
            loader1.setTypeIds(typeIds);
            loader1.setScopeId(orgId);
            
            // add all persons in the database to the graph
            IGraphElementLoader loader2 = new GraphElementLoader();
            loader2.setTypeIds(new String[]{PersonIso.TYPE_ID});
           
            getGraphService().setLoader(loader1, loader2);
            
            getGraphService().setRelationIds(relationIds);
            getGraphService().create();          
        } catch(Exception e) {
            LOG.error("Error while initialization", e);
        }
    }
    
    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public IGraphService getGraphService() {
        return graphService;
    }

    public void setGraphService(IGraphService graphService) {
        this.graphService = graphService;
    }

    
       
}
