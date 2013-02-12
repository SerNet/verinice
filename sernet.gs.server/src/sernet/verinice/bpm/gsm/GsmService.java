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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.bpm.ProcessServiceVerinice;
import sernet.verinice.graph.Edge;
import sernet.verinice.graph.IGraphService;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IGsmIsmExecuteProzess;
import sernet.verinice.interfaces.bpm.IGsmService;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.bpm.ProcessInformation;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.PersonIso;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GsmService extends ProcessServiceVerinice implements IGsmService {

    private static final Logger LOG = Logger.getLogger(GsmService.class);
    
    private static final String[] typeIds = {Asset.TYPE_ID,
                                             AssetGroup.TYPE_ID,
                                             Control.TYPE_ID,
                                             ControlGroup.TYPE_ID,
                                             IncidentScenario.TYPE_ID,
                                             PersonIso.TYPE_ID};
    
    private static final String[] relationIds = {Control.REL_CONTROL_INCSCEN,
                                             AssetGroup.REL_PERSON_ISO,
                                             IncidentScenario.REL_INCSCEN_ASSET};
    
    private IGraphService graphService;
    
    private IAuthService authService;
    
    private IBaseDao<CnATreeElement, Integer> elementDao;

    public GsmService() {
        super();
        // this is not the main process service:
        setWasInitCalled(true);
        
    }
    
    public void testStartProcess() {
        int orgDbId = 40695;
        testStartProcess(orgDbId);
    }
    
    
    public void testStartProcess(Integer orgId) {
        startProcessesForOrganization(orgId);
    }
    
    /**
     * Called on initialization configured in veriniceserver-jbpm.xml
     */
    public void initGraph(Integer orgId) {
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

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IGsmService#startProcess(java.lang.Integer)
     */
    @Override
    public IProcessStartInformation startProcessesForOrganization(Integer orgId) {
        initGraph(orgId);
      
        List<CnATreeElement> controlGroupList = get2ndLevelControlGroups(orgId);
        List<CnATreeElement> personList = getPersons(orgId);
        
        if (LOG.isInfoEnabled()) {
            LOG.info( controlGroupList.size() + " control groups");
            LOG.info( personList.size() + " persons");
        }
        
        ProcessInformation information = new ProcessInformation();
        for (CnATreeElement controlGroup : controlGroupList) {           
            for (CnATreeElement person : personList) {
                GsmServiceParameter parameter = new GsmServiceParameter(controlGroup, person);
                Set<CnATreeElement> elements = getAllElements(controlGroup, person);
                if(!elements.isEmpty()) {
                    parameter.setElementSet(elements);
                    startProcess(parameter);
                    information.increaseNumber();
                }
            }
        }
        
        if (LOG.isInfoEnabled()) {
            LOG.info( information.getNumber() + " tasks created");
        }

        return information;
    }

    private void startProcess(GsmServiceParameter processParameter) {
        Map<String, Object> parameterMap = createParameterMap(processParameter);
        startProcess(IGsmIsmExecuteProzess.KEY, parameterMap);
    }

    private Map<String, Object> createParameterMap(GsmServiceParameter processParameter) {
        Map<String, Object> map = new HashMap<String, Object>();
        String loginNameAssignee = null;
        if(processParameter.getPerson()!=null) {
            map.put(IGsmIsmExecuteProzess.VAR_ASSIGNEE_DISPLAY_NAME, processParameter.getPerson().getTitle());
            loginNameAssignee = getProcessDao().loadUsername(processParameter.getPerson().getUuid());
            if(loginNameAssignee==null) {
                LOG.error("Can't determine username of person (there is probably no account): " + processParameter.getPerson().getTitle());
            }
        }
        if(loginNameAssignee==null) {
            LOG.warn("Username of assignee not found. Using currently logged in person as assignee.");
            loginNameAssignee = getAuthService().getUsername();
        }       
        map.put(IGenericProcess.VAR_ASSIGNEE_NAME, loginNameAssignee);
        if(processParameter.getControlGroup()!=null) {
            map.put(IGsmIsmExecuteProzess.VAR_CONTROL_GROUP_TITLE, processParameter.getControlGroup().getTitle());
        }
        map.put(IGsmIsmExecuteProzess.VAR_ELEMENT_SET, processParameter.getElementSet());     
        return map;
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

    private Set<CnATreeElement> createIntersection(Set<CnATreeElement> controlGroupList, Set<CnATreeElement> personList) {
        controlGroupList.retainAll(personList);
        return controlGroupList;
    }
    
    private void logElementSet(Set<CnATreeElement> elementSet) {
        LOG.debug(createElementInformation(elementSet));
    }
    
    public static String createElementInformation(Set<CnATreeElement> elementSet) {
        StringBuffer message = new StringBuffer();
        message.append(createElementInformation(elementSet, AssetGroup.TYPE_ID));
        message.append(createElementInformation(elementSet, Asset.TYPE_ID));
        message.append(createElementInformation(elementSet, IncidentScenario.TYPE_ID));
        message.append(createElementInformation(elementSet, Control.TYPE_ID));
        return message.toString();
    }
    
    public static String createElementInformation(Set<CnATreeElement> elementSet, String typeId) {
        StringBuffer message = new StringBuffer();
        message.append("\n** ").append(typeId).append("\n");
        for (CnATreeElement element : elementSet) {
            if(element.getTypeId().equals(typeId)) {
                message.append(element.getTitle()).append("\n");
            }
        }
        return message.toString();
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
    
    public IGraphService getGraphService() {
        return graphService;
    }

    public void setGraphService(IGraphService graphService) {
        this.graphService = graphService;
    }

    public IAuthService getAuthService() {
        return authService;
    }


    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }


    @Override
    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    @Override
    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }
}
