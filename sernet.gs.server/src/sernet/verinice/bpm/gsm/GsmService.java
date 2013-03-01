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
            LOG.info( information.getNumber() + " tasks created");
        }

        return information;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IGsmService#deleteAssetScenarioLinks(java.util.Set)
     */
    @Override
    public int deleteAssetScenarioLinks(Set<String> elementUuidSet) {  
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting links from assets to scenario...");
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
        String loginNameAssignee = null;
        
        final CnATreeElement person = processParameter.getPerson();
        if(person!=null) {
            map.put(IGsmIsmExecuteProzess.VAR_ASSIGNEE_DISPLAY_NAME, person.getTitle());
            loginNameAssignee = getProcessDao().loadUsername(person.getUuid());
            if(loginNameAssignee==null) {
                LOG.error("Can't determine username of person (there is probably no account): " + person.getTitle());
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
        
        // TODO dm - VAR_ELEMENT_SET muss umgewandelt werden in ein UUID-Set
        // TODO dm - Die Asset Titel muessen beim Erzeugen des Prozesses als Set<String> gespeichert werden 
        // TODO dm - Der Titel des 1. Controls muss beim Erzeugen des Prozesses als String gespeichert werden 
        // TODO dm - Der Risk-Value muss beim Erzeugen des Prozesses als int gespeichert werden         
        
        final Set<CnATreeElement> elementSet = processParameter.getElementSet();
        map.put(IGsmIsmExecuteProzess.VAR_ELEMENT_UUID_SET, convertToUuidSet(elementSet));     
        map.put(IGsmIsmExecuteProzess.VAR_ASSET_DESCRIPTION_LIST, createAssetDescriptionList(elementSet));     
        map.put(IGsmIsmExecuteProzess.VAR_CONTROL_DESCRIPTION, getFirstControlDescription(elementSet));      
        map.put(IGsmIsmExecuteProzess.VAR_RISK_VALUE, getRiskValue(elementSet));
        
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
        return elementTitles;
    }
    
    /**
     * @param elementSet Elements of process
     * @return GSM-description of first control
     */
    private String getFirstControlDescription(Set<CnATreeElement> elementSet) {
        for (CnATreeElement element : elementSet) {
            if(Control.TYPE_ID.equals(element.getTypeId())) {
                return ((Control)element).getGsmDescription();
            }
        }
        return "";
    }
    
    private String getRiskValue(Set<CnATreeElement> elementSet) {
        return "unbekannt";
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

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
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
