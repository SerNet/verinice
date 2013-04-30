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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.graph.GraphElementLoader;
import sernet.verinice.graph.IGraphElementLoader;
import sernet.verinice.graph.IGraphService;
import sernet.verinice.hibernate.HibernateDao;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.iso27k.IncidentScenario;

/**
 * GsmAssetScenarioRemover is part of the GSM vulnerability tracking process.
 * Process definition is: gsm-ism-execute.jpdl.xml
 * 
 * GsmAssetScenarioRemover removes links between assets and scenarios.
 * Method deleteAssetScenarioLinks is called after process activity 
 * java.deleteAssetScenarioLinks executed.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GsmAssetScenarioRemover {

    private static final Logger LOG = Logger.getLogger(GsmAssetScenarioRemover.class);
    
    private static final String[] typeIds = {Asset.TYPE_ID, IncidentScenario.TYPE_ID, Control.TYPE_ID};

    private static final String[] relationIds = {IncidentScenario.REL_INCSCEN_ASSET, Control.REL_CONTROL_INCSCEN};
    
    /**
     * Every instance of GsmProcessStarter has an exclusive instance of a IGraphService
     * Spring scope of graphService in veriniceserver-jbpm.xml is 'prototype'
     */
    private IGraphService graphService;
    
    private HibernateDao<CnALink, CnALink.Id> linkDao;
    
    private IBaseDao<CnATreeElement, Integer> elementDao;
    
    
    /**
     * Deletes all links between assets and scenarios for one process.
     * Method is called when a task is finished.
     * 
     * After deleting the links. State of controls is updated.
     * If scenarios a has no more links to assets control state is implemented: yes.
     * If there are any links left state is implemented: partly.
     * 
     * @see sernet.verinice.interfaces.bpm.IGsmService#deleteAssetScenarioLinks(java.util.Set)
     */
    public int deleteAssetScenarioLinks(Set<String> elementUuidSet) {  
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting links from assets to scenario...");
        }
        if(elementUuidSet==null || elementUuidSet.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No elements found.");
            }
            return 0;
        }
        
        List<CnATreeElement> elementList = loadElementSet(elementUuidSet);
        
        // elements of gsm process are *all* from one scope
        int orgId = elementList.iterator().next().getScopeId();
        initGraph(orgId);
        DeleteAssetScenarioLinks hibernateCallback = new DeleteAssetScenarioLinks(elementList);      
        Integer numberOfDeletedLinks = (Integer) getLinkDao().executeCallback(hibernateCallback);   
        return numberOfDeletedLinks;
    }
    
    /**
     * @param elementUuidSet
     * @return
     */
    private List<CnATreeElement> loadElementSet(Set<String> elementUuidSet) {
        return getElementDao().findByCallback(new LoadElements(elementUuidSet)); 
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

    public HibernateDao<CnALink, CnALink.Id> getLinkDao() {
        return linkDao;
    }

    public void setLinkDao(HibernateDao<CnALink, CnALink.Id> linkDao) {
        this.linkDao = linkDao;
    }
    
    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }
    
    class LoadElements implements HibernateCallback {
        
        private final String hql = "from CnATreeElement element where element.uuid in (:uuidList)";
        
        private Set<String> elementUuidSet;
        
        /**
         * @param elementUuidSet
         */
        public LoadElements(Set<String> elementUuidSet) {
            this.elementUuidSet = elementUuidSet;
        }

        /* (non-Javadoc)
         * @see org.springframework.orm.hibernate3.HibernateCallback#doInHibernate(org.hibernate.Session)
         */
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query query =  session.createQuery(hql);
            query.setParameterList("uuidList", this.elementUuidSet);
            return query.list();
        }
    }

    class DeleteAssetScenarioLinks implements HibernateCallback {

        private final String hql = "delete from CnALink link where link.id.typeId = :linkTypeId and link.id.dependencyId in (:assetIds) and link.id.dependantId = :scenarioDbId";
        private Query query;
        
        private List<CnATreeElement> processAssets = new LinkedList<CnATreeElement>();
        private List<CnATreeElement> processScenarios = new LinkedList<CnATreeElement>();

        public DeleteAssetScenarioLinks(List<CnATreeElement> elementList) {
            super();
            processAssets = new LinkedList<CnATreeElement>();
            processScenarios = new LinkedList<CnATreeElement>();
            for (CnATreeElement element : elementList) {
                if(Asset.TYPE_ID.equals(element.getTypeId())) {
                    processAssets.add(element);
                }
                if(IncidentScenario.TYPE_ID.equals(element.getTypeId())) {
                    processScenarios.add(element);
                }              
            }
        }

        /* (non-Javadoc)
         * @see org.springframework.orm.hibernate3.HibernateCallback#doInHibernate(org.hibernate.Session)
         */
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            int numberOfDeletedLinks = 0;        
            query = session.createQuery(hql);
            
            for (CnATreeElement scenario : processScenarios) {
                numberOfDeletedLinks += handleScenario(scenario);
            }          
            
            return numberOfDeletedLinks;           
        }

        private int handleScenario(CnATreeElement scenario) {
            // get all assets linked to the scenario
            Set<CnATreeElement> allLinkedAssets = getGraphService().getLinkTargets(scenario, IncidentScenario.REL_INCSCEN_ASSET);
            int numberOfAllLinkedAssets = allLinkedAssets.size();
            // use all assets which are linked and in the process
            Set<CnATreeElement> processLinkedAssets = createIntersection(allLinkedAssets,processAssets);
            int numberOfProcessLinkedAssets = processLinkedAssets.size();
            int numberOfDeletedLinks = deleteAssetScenarioLinks(query, scenario, processLinkedAssets);
            // Update control state              
            Set<CnATreeElement> linkedControls = getGraphService().getLinkTargets(scenario,Control.REL_CONTROL_INCSCEN);
            String state = determineState(numberOfProcessLinkedAssets,numberOfAllLinkedAssets);
            updateControlState(linkedControls, state);
            return numberOfDeletedLinks;
        }

        private int deleteAssetScenarioLinks(Query query, CnATreeElement scenario, Set<CnATreeElement> assets) {
            if(scenario==null || assets==null || assets.isEmpty()) {
                return 0;
            }
            if (LOG.isDebugEnabled()) {
                logParameter(scenario, assets);
            }
            
            List<Integer> assetIdList = new ArrayList<Integer>();
            for (CnATreeElement asset : assets) {
                assetIdList.add(asset.getDbId());
            }
            query.setParameter("linkTypeId", IncidentScenario.REL_INCSCEN_ASSET);
            query.setParameter("scenarioDbId", scenario.getDbId());
            query.setParameterList("assetIds", assetIdList);
            int result = query.executeUpdate();
            return result;
        }
         
        /**
         * Determines the implementation state of an control by assets linked to
         * it (via a scenario).
         * 
         * If all linked assets are part of this process, state is IControl.IMPLEMENTED_YES,
         * otherwise is  IControl.IMPLEMENTED_PARTLY.
         * 
         * @param processLinkedAssets Number of assets linked to one control (via a scenario) which are part of this process
         * @param allLinkedAssets Number of assets linked to one control via a scenario
         * @return Implementation state of an control: IControl.IMPLEMENTED_PARTLY or IControl.IMPLEMENTED_YES
         */
        private String determineState(int numberOfProcessLinkedAssets, int numberOfAllLinkedAssets) {
            String state = IControl.IMPLEMENTED_PARTLY;
            if(numberOfAllLinkedAssets==0 || (numberOfAllLinkedAssets==numberOfProcessLinkedAssets)) {
                state = IControl.IMPLEMENTED_YES;
            }
            return state;
        }

        /**
         * @param linkedControls
         * @param b
         */
        private void updateControlState(Set<CnATreeElement> linkedControls, String state) {
           if(linkedControls==null || linkedControls.isEmpty()) {
               if (LOG.isDebugEnabled()) {
                   LOG.debug("No control found. Can not update state.");
               }
               return;
           }
           if(linkedControls.size()>1) {
               if (LOG.isDebugEnabled()) {
                   LOG.debug("More than one control linked to scenarion. Can not update state.");
               }
               return;
           }
           Control control = (Control) linkedControls.iterator().next();
           control.setImplementation(state);
           getElementDao().saveOrUpdate(control);
        }
        
        private void logParameter(CnATreeElement asset, Set<CnATreeElement> scenarios) {
            LOG.debug("Deleting links to scenarios, asset: " + asset.getUuid() + "...");
            for (CnATreeElement scenario : scenarios) {
                LOG.debug("Scenario: " + scenario.getUuid());
            }
        }

        private Set<CnATreeElement> createIntersection(Set<CnATreeElement> elementSet, List<CnATreeElement> elementList2) {
            elementSet.retainAll(elementList2);
            return elementSet;
        }
        
    }
    
}
