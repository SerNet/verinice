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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.graph.Edge;
import sernet.verinice.graph.IGraphService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * Creates a list of parameters for the creation of
 * a GSM process.
 * 
 * GsmProcessParameterCreater uses an instance of 
 * sernet.verinice.graph.IGraphService to collect information
 * about verinice elements.
 * 
 * Configured in veriniceserver-jbpm.xml.
 * Used by sernet.verinice.bpm.gsm.GsmService
 *
 * @see IGraphService
 * @see GsmService
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GsmProcessParameterCreater {

    private static final Logger LOG = Logger.getLogger(GsmProcessParameterCreater.class);
    
    private static final String[] typeIds = {Asset.TYPE_ID,
        AssetGroup.TYPE_ID,
        Control.TYPE_ID,
        ControlGroup.TYPE_ID,
        IncidentScenario.TYPE_ID,
        PersonIso.TYPE_ID};

    private static final String[] relationIds = {Control.REL_CONTROL_INCSCEN,
        AssetGroup.REL_PERSON_ISO,
        IncidentScenario.REL_INCSCEN_ASSET};
    
    /**
     * Every instance of GsmProcessStarter has an exclusive instance of a IGraphService
     * Spring scope of graphService in veriniceserver-jbpm.xml is 'prototype'
     */
    private IGraphService graphService;
    
    private IBaseDao<CnATreeElement, Integer> elementDao;

    /**
     * Creates a list of parameters for the creation of
     * a GSM process.
     * 
     * @param orgId db-id of an organization
     * @return A list of parameters to create a GSM-process
     */
    public List<GsmServiceParameter> createProcessParameterForOrganization(Integer orgId) {
        initGraph(orgId);
      
        List<CnATreeElement> controlGroupList = get2ndLevelControlGroups(orgId);
        List<CnATreeElement> personList = getPersons(orgId);
        
        if (LOG.isInfoEnabled()) {
            LOG.info( controlGroupList.size() + " control groups");
            LOG.info( personList.size() + " persons");
        }
        
        List<GsmServiceParameter> parameterList = new LinkedList<GsmServiceParameter>();
        for (CnATreeElement controlGroup : controlGroupList) {           
            for (CnATreeElement person : personList) {
                GsmServiceParameter parameter = new GsmServiceParameter(controlGroup, person);
                Set<CnATreeElement> elements = getAllElements(controlGroup, person);
                if(!elements.isEmpty()) {
                    parameter.setElementSet(elements);
                    parameterList.add(parameter);
                }
            }
        }
        
        if (LOG.isInfoEnabled()) {
            LOG.info( parameterList.size() + " process parameter objects created");
        }

        return parameterList;
    }
    
    private void initGraph(Integer orgId) {
        try {          
            getGraphService().setTypeIds(typeIds);
            getGraphService().setRelationIds(relationIds);
            getGraphService().setElementFilter(new TopElementFilter(orgId));
            getGraphService().setScopeId(orgId);
            getGraphService().create();          
        } catch(Exception e) {
            LOG.error("Error while initialization", e);
        }
    }
    
    private List<CnATreeElement> get2ndLevelControlGroups(Integer orgId) {
        StringBuffer hql = new StringBuffer("select distinct e from CnATreeElement e "); //$NON-NLS-1$
        hql.append("inner join fetch e.entity as entity ");  //$NON-NLS-1$
        hql.append("inner join fetch entity.typedPropertyLists as propertyList "); //$NON-NLS-1$
        hql.append("inner join fetch propertyList.properties as props "); //$NON-NLS-1$        
        hql.append("where e.objectType = ? and e.parent.parent.dbId = ?"); //$NON-NLS-1$ 
        return getElementDao().findByQuery(hql.toString(),new Object[]{ControlGroup.TYPE_ID,orgId});
       
    }
    
    private List<CnATreeElement> getPersons(Integer orgId) {
        DetachedCriteria crit = createDefaultCriteria();
        crit.add(Restrictions.eq("objectType", PersonIso.TYPE_ID));
        crit.add(Restrictions.eq("scopeId", orgId));
        return getElementDao().findByCriteria(crit);
    }
    
    private DetachedCriteria createDefaultCriteria() {
        DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class);
        crit.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
        crit.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        return crit;
    }
    
    private Set<CnATreeElement> getAllElements(CnATreeElement controlGroup, CnATreeElement person) {       
        Set<CnATreeElement> setA = getObjectsForControlGroup(controlGroup);
        Set<CnATreeElement> setB = getObjectsForPerson(person);
        Set<CnATreeElement> result = createIntersection(setA,setB);
        if (LOG.isDebugEnabled() && !result.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Task for \"" + person.getTitle() + "\" and group \"" + controlGroup.getTitle() + "\"");
            }
            logElementSet(result);
        }
        return result;
    }
    
    private Set<CnATreeElement> getObjectsForControlGroup(CnATreeElement controlGroup) {
        // elements is a set of controls
        Set<CnATreeElement> elements = getGraphService().getLinkTargets(controlGroup);
        Set<CnATreeElement> scenarios = new HashSet<CnATreeElement>();
        for (CnATreeElement control : elements) {
            scenarios.addAll(getGraphService().getLinkTargets(control, Control.REL_CONTROL_INCSCEN));
        }
        Set<CnATreeElement> assets = new HashSet<CnATreeElement>();
        for (CnATreeElement scen : scenarios) {
            assets.addAll(getGraphService().getLinkTargets(scen, IncidentScenario.REL_INCSCEN_ASSET));
        }
        Set<CnATreeElement> assetGroups = new HashSet<CnATreeElement>();
        for (CnATreeElement asset : assets) {
            assetGroups.addAll(getGraphService().getLinkTargets(asset, Edge.RELATIVES));
        }
        elements.addAll(scenarios);
        elements.addAll(assets);
        elements.addAll(assetGroups);
        return elements;
    }

    private Set<CnATreeElement> getObjectsForPerson(CnATreeElement person) {
        // elements is a set of AssetGroups
        Set<CnATreeElement> elements = getGraphService().getLinkTargets(person,AssetGroup.REL_PERSON_ISO);
        Set<CnATreeElement> assets = new HashSet<CnATreeElement>();
        for (CnATreeElement assetGroup : elements) {
            assets.addAll(getGraphService().getLinkTargets(assetGroup, Edge.RELATIVES));
        }
        Set<CnATreeElement> scenarios = new HashSet<CnATreeElement>();
        for (CnATreeElement asset : assets) {
            scenarios.addAll(getGraphService().getLinkTargets(asset, IncidentScenario.REL_INCSCEN_ASSET));
        }
        Set<CnATreeElement> controls = new HashSet<CnATreeElement>();
        for (CnATreeElement scen : scenarios) {
            controls.addAll(getGraphService().getLinkTargets(scen, Control.REL_CONTROL_INCSCEN));
        }
        elements.addAll(assets);
        elements.addAll(scenarios);
        elements.addAll(controls);
        return elements;
    }
    
    private Set<CnATreeElement> createIntersection(Set<CnATreeElement> elementList1, Set<CnATreeElement> elementList2) {
        elementList1.retainAll(elementList2);
        return elementList1;
    }
    
    private void logElementSet(Set<CnATreeElement> elementSet) {
        LOG.debug(GsmService.createElementInformation(elementSet));
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
