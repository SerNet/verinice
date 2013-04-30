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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;

import sernet.verinice.bpm.ProcessServiceVerinice;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IGsmIsmExecuteProzess;
import sernet.verinice.interfaces.bpm.IGsmService;
import sernet.verinice.interfaces.bpm.IGsmValidationResult;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.bpm.ProcessInformation;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;

/**
 * Process service for GSM vulnerability tracking process
 * Process definition is: gsm-ism-execute.jpdl.xml 
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GsmService extends ProcessServiceVerinice implements IGsmService {

    private static final Logger LOG = Logger.getLogger(GsmService.class);
    
    private IAuthService authService;
    
    /**
     * Factory to create GsmProcessValidator instances
     * configured in veriniceserver-jbpm.xml
     */
    private ObjectFactory processValidatorFactory;
    
    /**
     * Factory to create GsmProcessStarter instances
     * configured in veriniceserver-jbpm.xml
     */
    private ObjectFactory processStarterFactory;
    
    /**
     * Factory to create GsmProcessStarter instances
     * configured in veriniceserver-jbpm.xml
     */
    private ObjectFactory assetScenarioRemoverFactory;

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
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IGsmService#validateOrganization(java.lang.Integer)
     */
    @Override
    public IGsmValidationResult validateOrganization(Integer orgId) {
        // creates a new (prototype) instances of the GsmProcessValidator spring bean
        // see veriniceserver-jbpm.xml and http://static.springsource.org/spring/docs/2.5.x/reference/beans.html#beans-factory-aware-beanfactoryaware
        GsmProcessValidator processValidator = (GsmProcessValidator) processValidatorFactory.getObject();
        IGsmValidationResult result = processValidator.validateOrganization(orgId);
        
        return result;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IGsmService#startProcess(java.lang.Integer)
     */
    @Override
    public IProcessStartInformation startProcessesForOrganization(Integer orgId) {
        
        // creates a new (prototype) instances of the GsmProcessStarter spring bean
        // see veriniceserver-jbpm.xml and http://static.springsource.org/spring/docs/2.5.x/reference/beans.html#beans-factory-aware-beanfactoryaware
        GsmProcessParameterCreater processStarter = (GsmProcessParameterCreater) processStarterFactory.getObject();
        
        List<GsmServiceParameter> parameterList = processStarter.createProcessParameterForOrganization(orgId);
        
        ProcessInformation information = new ProcessInformation();
        for (GsmServiceParameter parameter : parameterList) {                      
            startProcess(parameter);
            information.increaseNumber();            
        }
        
        if (LOG.isInfoEnabled()) {
            LOG.info( information.getNumber() + " tasks created"); //$NON-NLS-1$
        }

        return information;
    }
    
    /**
     * Deletes all links between assets and scenarios in elementUuidSet.
     * ElementUuidSet contains all elements of one process.
     * Method is called when a task is finished.
     * 
     * @param elementUuidSet Uuids of all elements in one process
     * @see sernet.verinice.interfaces.bpm.IGsmService#deleteAssetScenarioLinks(java.util.Set)
     */
    @Override
    public int deleteAssetScenarioLinks(Set<String> elementUuidSet) {  
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting links from assets to scenario..."); //$NON-NLS-1$
        }
        
        // creates a new (prototype) instances of the GsmAssetScenarioRemover spring bean
        // see veriniceserver-jbpm.xml and http://static.springsource.org/spring/docs/2.5.x/reference/beans.html#beans-factory-aware-beanfactoryaware
        GsmAssetScenarioRemover assetScenarioRemover = (GsmAssetScenarioRemover) assetScenarioRemoverFactory.getObject();
        Integer numberOfDeletedLinks = assetScenarioRemover.deleteAssetScenarioLinks(elementUuidSet); 
        return numberOfDeletedLinks;
    }

    private void startProcess(GsmServiceParameter processParameter) {
        Map<String, Object> parameterMap = createParameterMap(processParameter);
        startProcess(IGsmIsmExecuteProzess.KEY, parameterMap);
    }

    private Map<String, Object> createParameterMap(GsmServiceParameter processParameter) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put(IGenericProcess.VAR_PROCESS_ID, processParameter.getProcessId());     
        
        String loginNameAssignee = null;     
        final CnATreeElement person = processParameter.getPerson();
        if(person!=null) {
            map.put(IGsmIsmExecuteProzess.VAR_ASSIGNEE_DISPLAY_NAME, person.getTitle());
            loginNameAssignee = getProcessDao().loadUsername(person.getUuid());
            if(loginNameAssignee==null) {
                LOG.error("Can't determine username of person (there is probably no account): " + person.getTitle()); //$NON-NLS-1$
            }
        }
        if(loginNameAssignee==null) {
            LOG.warn("Username of assignee not found. Using currently logged in person as assignee."); //$NON-NLS-1$
            loginNameAssignee = getAuthService().getUsername();
        }       
        map.put(IGenericProcess.VAR_ASSIGNEE_NAME, loginNameAssignee);
        
        if(processParameter.getControlGroup()!=null) {
            map.put(IGsmIsmExecuteProzess.VAR_CONTROL_GROUP_TITLE, processParameter.getControlGroup().getTitle());
        }
        
        final Set<CnATreeElement> elementSet = processParameter.getElementSet();
        map.put(IGsmIsmExecuteProzess.VAR_ELEMENT_UUID_SET, convertToUuidSet(elementSet));     
        map.put(IGsmIsmExecuteProzess.VAR_ASSET_DESCRIPTION_LIST, createAssetDescriptionList(elementSet));     
        map.put(IGsmIsmExecuteProzess.VAR_CONTROL_DESCRIPTION, getFirstControlDescription(elementSet));      
        map.put(IGenericProcess.VAR_UUID, getAssetGroupUuid(elementSet));
        map.put(IGenericProcess.VAR_AUDIT_UUID, processParameter.getUuidOrg());    
        map.put(IGsmIsmExecuteProzess.VAR_RISK_VALUE, processParameter.getRiskValue());
        map.put(IGenericProcess.VAR_PRIORITY, processParameter.getPriority());     
        
        return map;
    }

    /**
     * @param elementSet Elements of process
     * @return UUIDs of all elements in a set 
     */
    private Set<String> convertToUuidSet(Set<CnATreeElement> elementSet) {
        Set<String> uuidSet = new HashSet<String>();
        for (CnATreeElement element : elementSet) {
            uuidSet.add(element.getUuid());
        }
        return uuidSet;
    }
    
    /**
     * @param elementSet Elements of process
     * @return Titles of all assets in a list. 
     */
    private List<String> createAssetDescriptionList(Set<CnATreeElement> elementSet) {
        List<String> elementTitles = new LinkedList<String>();
        for (CnATreeElement element : elementSet) {
            if(element.getTypeId().equals(Asset.TYPE_ID)) {
                elementTitles.add(element.getTitle());
            }
        }
        Collections.sort(elementTitles);
        return elementTitles;
    }
    

    private String getFirstControlDescription(Set<CnATreeElement> elementSet) {
        String description = Messages.getString("GsmService.0"); //$NON-NLS-1$
        for (CnATreeElement element : elementSet) {
            if(Control.TYPE_ID.equals(element.getTypeId())) {
                String current = ((Control)element).getGsmDescription();
                if(current!=null && !current.isEmpty()) {
                    description = current;
                }
            }
        }
        return description; 
    }
    
    /**
     * Returns the UUID from the *first* AssetGroup in a set of elements.
     * 
     * @param elementSet Elements of process
     * @return UUID from the *first* AssetGroup or null if there is AssetGroup
     */
    private Object getAssetGroupUuid(Set<CnATreeElement> elementSet) {
        for (CnATreeElement element : elementSet) {
            if(AssetGroup.TYPE_ID.equals(element.getTypeId())) {
                return ((AssetGroup)element).getUuid();
            }
        }
        return null;
    }

    public static String createElementInformation(Set<CnATreeElement> elementSet) {
        StringBuffer message = new StringBuffer();
        message.append(createElementInformation(elementSet, AssetGroup.TYPE_ID));
        message.append(createElementInformation(elementSet, Asset.TYPE_ID));
        message.append(createElementInformation(elementSet, IncidentScenario.TYPE_ID));
        message.append(createElementInformation(elementSet, Control.TYPE_ID));
        return message.toString();
    }
    
    /**
     * @param person A person
     * @param controlGroup A control group
     * @return P=<UUID_PERSON>;CG=<UUID_CONTROL_GROUP>
     */
    public static String createProcessId(CnATreeElement person, CnATreeElement controlGroup) {
        return new StringBuilder().append("P=").append(person.getUuid()).append(";CG=").append(controlGroup.getUuid()).toString(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public static String createElementInformation(Set<CnATreeElement> elementSet, String typeId) {
        StringBuffer message = new StringBuffer();
        message.append("\n** ").append(typeId).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        for (CnATreeElement element : elementSet) {
            if(element.getTypeId().equals(typeId)) {
                message.append(element.getTitle()).append("\n"); //$NON-NLS-1$
            }
        }
        return message.toString();
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }
    
    public ObjectFactory getProcessValidatorFactory() {
        return processValidatorFactory;
    }

    public void setProcessValidatorFactory(ObjectFactory processValidatorFactory) {
        this.processValidatorFactory = processValidatorFactory;
    }

    public ObjectFactory getProcessStarterFactory() {
        return processStarterFactory;
    }

    public void setProcessStarterFactory(ObjectFactory factory) {
        this.processStarterFactory = factory;
    }

    public ObjectFactory getAssetScenarioRemoverFactory() {
        return assetScenarioRemoverFactory;
    }

    public void setAssetScenarioRemoverFactory(ObjectFactory assetScenarioRemoverFactory) {
        this.assetScenarioRemoverFactory = assetScenarioRemoverFactory;
    }

}
