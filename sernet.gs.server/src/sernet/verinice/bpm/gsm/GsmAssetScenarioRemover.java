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

import sernet.verinice.graph.IGraphService;
import sernet.verinice.hibernate.HibernateDao;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
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
    
    private static final String[] typeIds = {Asset.TYPE_ID, IncidentScenario.TYPE_ID};

    private static final String[] relationIds = {IncidentScenario.REL_INCSCEN_ASSET};
    
    /**
     * Every instance of GsmProcessStarter has an exclusive instance of a IGraphService
     * Spring scope of graphService in veriniceserver-jbpm.xml is 'prototype'
     */
    private IGraphService graphService;
    
    private HibernateDao<CnALink, CnALink.Id> linkDao;
    
    private IBaseDao<CnATreeElement, Integer> elementDao;
    
    
    /* (non-Javadoc)
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
            getGraphService().setTypeIds(typeIds);
            getGraphService().setRelationIds(relationIds);
            getGraphService().setScopeId(orgId);
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

        private final String hql = "delete from CnALink link where link.id.typeId = :linkTypeId and link.id.dependencyId = :assetDbId and link.id.dependantId in (:scenarioIds)";
        
        private List<CnATreeElement> elementList;

        public DeleteAssetScenarioLinks(List<CnATreeElement> elementList) {
            super();
            this.elementList = elementList;
        }

        /* (non-Javadoc)
         * @see org.springframework.orm.hibernate3.HibernateCallback#doInHibernate(org.hibernate.Session)
         */
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            int numberOfDeletedLinks = 0;        
            Query query =  session.createQuery(hql);
            
            List<CnATreeElement> processAssets = new LinkedList<CnATreeElement>();
            List<CnATreeElement> processScenarios = new LinkedList<CnATreeElement>();
            for (CnATreeElement element : elementList) {
                if(Asset.TYPE_ID.equals(element.getTypeId())) {
                    processAssets.add(element);
                }
                if(IncidentScenario.TYPE_ID.equals(element.getTypeId())) {
                    processScenarios.add(element);
                }              
            }
            
            for (CnATreeElement asset : processAssets) {
                // get all scenarios linked to the asset
                Set<CnATreeElement> allLinkedScenarios = getGraphService().getLinkTargets(asset, IncidentScenario.REL_INCSCEN_ASSET);
                // use all scenarios which are linked and in the process
                int result = deleteAssetScenarioLinks(query, asset, createIntersection(allLinkedScenarios,processScenarios));
                numberOfDeletedLinks += result;              
            }
            
            return numberOfDeletedLinks;           
        }

        private int deleteAssetScenarioLinks(Query query, CnATreeElement asset, Set<CnATreeElement> scenarios) {
            if(asset==null || scenarios==null || scenarios.isEmpty()) {
                return 0;
            }
            if (LOG.isDebugEnabled()) {
                logParameter(asset, scenarios);
            }
            
            List<Integer> scenarioIdList = new ArrayList<Integer>();
            for (CnATreeElement element : scenarios) {
                scenarioIdList.add(element.getDbId());
            }
            query.setParameter("linkTypeId", IncidentScenario.REL_INCSCEN_ASSET);
            query.setParameter("assetDbId", asset.getDbId());
            query.setParameterList("scenarioIds", scenarioIdList);
            int result = query.executeUpdate();
            return result;
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
