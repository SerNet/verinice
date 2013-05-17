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

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jbpm.pvm.internal.model.ExecutionImpl;

import sernet.verinice.graph.Edge;
import sernet.verinice.graph.GraphElementLoader;
import sernet.verinice.graph.IGraphElementLoader;
import sernet.verinice.graph.IGraphService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.ITask;
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
        IncidentScenario.TYPE_ID};

    private static final String[] relationIds = {Control.REL_CONTROL_INCSCEN,
        AssetGroup.REL_PERSON_ISO,
        IncidentScenario.REL_INCSCEN_ASSET};
   
    /*
     * Configure this in veriniceserver-jbpm.xml
     * Process with risk value below lowPriorityRiskLimit: priority low
     * Process with risk value above lowPriorityRiskLimit: priority normal
     */
    private int lowPriorityRiskLimit;
    public static final int LOW_PRIORITY_RISK_LIMIT_DEFAULT = 100;
    
    /*
     * Configure this in veriniceserver-jbpm.xml
     * Process with risk value below normalPriorityRiskLimit: priority normal
     * Process with risk value above normalPriorityRiskLimit: priority high
     */
    private int normalPriorityRiskLimit;
    public static final int NORMAL_PRIORITY_RISK_LIMIT_DEFAULT = 500;
    
    
    /**
     * Every instance of GsmProcessStarter has an exclusive instance of a IGraphService
     * Spring scope of graphService in veriniceserver-jbpm.xml is 'prototype'
     */
    private IGraphService graphService;
    
    private IBaseDao<CnATreeElement, Integer> elementDao;
    
    private IDao<ExecutionImpl, Long> jbpmExecutionDao;

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
        List<CnATreeElement> personList = getPersons();
        
        if (LOG.isInfoEnabled()) {
            LOG.info( controlGroupList.size() + " control groups");
            LOG.info( personList.size() + " persons");
        }
        
        List<GsmServiceParameter> parameterList = new LinkedList<GsmServiceParameter>();
        for (CnATreeElement controlGroup : controlGroupList) {           
            for (CnATreeElement person : personList) {
                if(processExists(person, controlGroup)) {
                    continue;
                }
                GsmServiceParameter parameter = createParameter(person, controlGroup);
                if(parameter!=null) {
                    List<String> uuidList = getElementDao().findByQuery("select e.uuid from CnATreeElement e where e.dbId = ?", new Object[]{orgId});
                    parameter.setUuidOrg(uuidList.get(0));
                    parameterList.add(parameter);
                }
            }
        }
        
        if (LOG.isInfoEnabled()) {
            LOG.info( parameterList.size() + " process parameter objects created");
        }

        return parameterList;
    }

    private GsmServiceParameter createParameter(CnATreeElement person, CnATreeElement controlGroup) {
        GsmServiceParameter parameter = null;
        Set<CnATreeElement> elementSet = getAllElements(controlGroup, person);
        if(!elementSet.isEmpty()) {
            parameter = new GsmServiceParameter(controlGroup, person);
            parameter.setElementSet(elementSet);
            Double riskValueDouble = getRiskValue(elementSet);
            parameter.setRiskValue(convertRiskValueToString(riskValueDouble));
            parameter.setPriority(convertRiskValueToPriority(riskValueDouble));
        }
        return parameter;
    }
    
    /**
     * Returns true if there is a process for a person and a control-group,
     * false if not.
     * 
     * @param person A person
     * @param controlGroup A control-group 
     * @return true if there is a process, fasle if not
     */
    private boolean processExists(CnATreeElement person, CnATreeElement controlGroup) {
        String value = GsmService.createProcessId(person,controlGroup);
        List<?> processDbIdsList = searchProcessByVariable(IGenericProcess.VAR_PROCESS_ID, value);
        return !processDbIdsList.isEmpty();
    }

    private List<?> searchProcessByVariable(String key, String value) {        
        DetachedCriteria executionCrit = DetachedCriteria.forClass(ExecutionImpl.class);
        DetachedCriteria variableCrit = executionCrit.createCriteria("variables");
        variableCrit.add(Restrictions.eq("key", key));
        variableCrit.add(Restrictions.eq("string", value));
        return getJbpmExecutionDao().findByCriteria(executionCrit);
    }

    /**
     * Calculate the risk value of a process.
     * 
     * CVSS value of incidentScenario: CVSS
     * Number of assets linked to incidentScenario: NUMBER_OF_LINKED_ASSETS
     * 
     * RISK_VALUE = [[CVSS_1 * NUMBER_OF_LINKED_ASSETS_1] + [CVSS_2 * NUMBER_OF_LINKED_ASSETS_2] + ...]
     * 
     * RISK_VALUE is rounded and has two digits to the right of the decimal point.
     * All CVSS are null (no CVSS is set at all): RISK_VALUE = "not determinable"
     * Total number of linked assets is 0: RISK_VALUE = 0
     * 
     * @param elementSet Elements of process
     * @return risk value of a process
     */
    private Double getRiskValue(Set<CnATreeElement> elementSet) {
        Double riskValueDouble = null;
        for (CnATreeElement element : elementSet) {
            if(IncidentScenario.TYPE_ID.equals(element.getTypeId())) {
                Double currentValue = getRiskValue(element, elementSet);
                if(riskValueDouble==null) {
                    riskValueDouble = currentValue;
                } else {
                    riskValueDouble += currentValue;
                }             
            }
        }
        return riskValueDouble;
    }
    
    private String convertRiskValueToString(Double riskValueDouble) {
        String riskValue = Messages.getString("GsmService.5"); //$NON-NLS-1$
        if(riskValueDouble!=null) {
            DecimalFormat formatterAndRounder = new DecimalFormat("###.##"); //$NON-NLS-1$
            riskValue = formatterAndRounder.format(riskValueDouble);
        }
        return riskValue;
    }
    
    private String convertRiskValueToPriority(Double riskValueDouble) {
        String priority = ITask.PRIO_NORMAL;
        if(riskValueDouble!=null) {
            priority = ITask.PRIO_LOW;
            if(riskValueDouble.doubleValue() > getNormalPriorityRiskLimit()) {
                priority = ITask.PRIO_HIGH;
            } else if(riskValueDouble.doubleValue() > getLowPriorityRiskLimit()) {
                priority = ITask.PRIO_NORMAL;              
            }
        }
        return priority;
    }

    private Double getRiskValue(CnATreeElement element, Set<CnATreeElement> processElementSet) {
        Double riskValue = null;
        IncidentScenario scenario = (IncidentScenario) element;
        Double cvss = scenario.getGsmCvss();
        if(cvss!=null) {
            Set<CnATreeElement> allAssetList = getGraphService().getLinkTargets(scenario, IncidentScenario.REL_INCSCEN_ASSET);      
            int numberOfLinkedAssets = 0;
            for (CnATreeElement linkedAsset : allAssetList) {
                if(processElementSet.contains(linkedAsset)) {
                    numberOfLinkedAssets++;
                }
            }
            riskValue = cvss * numberOfLinkedAssets;
        }
        return riskValue;
    }
    
    private void initGraph(Integer orgId) {
        try { 
            IGraphElementLoader loader1 = new GraphElementLoader();
            loader1.setTypeIds(typeIds);
            loader1.setScopeId(orgId);
            loader1.setElementFilter(new TopElementFilter(orgId));
            
            IGraphElementLoader loader2 = new GraphElementLoader();
            loader2.setTypeIds(new String[]{PersonIso.TYPE_ID});
           
            getGraphService().setLoader(loader1, loader2);
            
            getGraphService().setRelationIds(relationIds);
            getGraphService().create();          
        } catch(Exception e) {
            LOG.error("Error while initialization", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<CnATreeElement> get2ndLevelControlGroups(Integer orgId) {
        StringBuffer hql = new StringBuffer("select e.dbId from CnATreeElement e "); //$NON-NLS-1$      
        hql.append("where e.objectType = ? and e.parent.parent.dbId = ?"); //$NON-NLS-1$ 
        List<Integer> dbIdResult = getElementDao().findByQuery(hql.toString(),new Object[]{ControlGroup.TYPE_ID,orgId});
        if(dbIdResult==null || dbIdResult.isEmpty()) {
            return Collections.emptyList();
        }
        DetachedCriteria crit = createDefaultCriteria();
        Integer[] dbIdArray = dbIdResult.toArray(new Integer[dbIdResult.size()]);
        crit.add(Restrictions.in("dbId", dbIdArray));
        return getElementDao().findByCriteria(crit);   
    }
    
    @SuppressWarnings("unchecked")
    private List<CnATreeElement> getPersons() {
        DetachedCriteria crit = createDefaultCriteria();
        crit.add(Restrictions.eq("objectType", PersonIso.TYPE_ID));
        return getElementDao().findByCriteria(crit);
    }
    
    private DetachedCriteria createDefaultCriteria() {
        DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class);
        crit.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
        crit.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
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

    public int getLowPriorityRiskLimit() {
        return lowPriorityRiskLimit;
    }

    public void setLowPriorityRiskLimit(int lowPriorityRiskLimit) {
        this.lowPriorityRiskLimit = lowPriorityRiskLimit;
    }

    public int getNormalPriorityRiskLimit() {
        return normalPriorityRiskLimit;
    }

    public void setNormalPriorityRiskLimit(int normalPriorityRiskLimit) {
        this.normalPriorityRiskLimit = normalPriorityRiskLimit;
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

    public IDao<ExecutionImpl, Long> getJbpmExecutionDao() {
        return jbpmExecutionDao;
    }

    public void setJbpmExecutionDao(IDao<ExecutionImpl, Long> jbpmExecutionDao) {
        this.jbpmExecutionDao = jbpmExecutionDao;
    }

}
