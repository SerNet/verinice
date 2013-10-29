/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.bpm;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jbpm.pvm.internal.model.ExecutionImpl;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.bpm.IControlExecutionProcess;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IIsaExecutionProcess;
import sernet.verinice.interfaces.bpm.IProcessServiceIsa;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.bpm.ProcessInformation;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.samt.SamtTopic;

/**
 * ProcessServiceIsa is the server side implementation of the IProcessServiceIsa interface
 * to handle ISA processes.
 * 
 * IProcessServiceIsa is accessible from verinice client by Spring remoting, configured in
 * springDispatcher-servlet.xml and veriniceclient.xml.
 * 
 * This class is created at runtime by Spring. 
 * See sernet/gs/server/spring/veriniceserver-jbpm.xml for configuration.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessServiceIsa extends ProcessServiceVerinice implements IProcessServiceIsa {

    private static final Logger LOG = Logger.getLogger(ProcessServiceIsa.class);
    
    // Dao members (injected by Spring)
    private IBaseDao<Audit, Integer> auditDao;     
    private IBaseDao<ControlGroup, Integer> controlGroupDao;     
    private IBaseDao<SamtTopic, Integer> samtTopicDao;

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceIsa#startProcessForIsa(java.lang.String)
     */
    @Override
    public IProcessStartInformation startProcessForIsa(String uuidAudit) {
        Audit isaAudit = getAuditDao().findByUuid(uuidAudit, RetrieveInfo.getChildrenInstance().setParent(true));  
        IsaProcessContext context = new IsaProcessContext();
        context.setNumberOfProcesses(0);
        context.setUuidAudit(uuidAudit);
        context.setUuidOrganization(loadOrganization(isaAudit.getParent()).getUuid());
        context.setControlGroup(isaAudit.getControlGroup());
        context=startProcessForControlGroup(context);
        return new ProcessInformation(context.getNumberOfProcesses());
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceIsa#handleControl(sernet.verinice.model.iso27k.Control)
     */
    @Override
    public void handleSamtTopic(SamtTopic topic) {
        IsaProcessContext context = new IsaProcessContext();
        context.setSamtTopic(topic);
        handleSamtTopic(context);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceIsa#handleControl(sernet.verinice.model.iso27k.Control)
     */
    @Override
    public void handleControl(Control control) {
        try {
            String uuidControl = control.getUuid();
            List<ExecutionImpl> executionList = findControlExecution(uuidControl);
            if(executionList==null || executionList.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No process for control: " + uuidControl);
                }
                // start process if control is not implemented
                if(!Control.IMPLEMENTED_YES.equals(control.getImplementation())) {
                    startControlExecution(control);
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("Process started for control: " + uuidControl);
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Process found for control: " + uuidControl);
                for (ExecutionImpl executionImpl : executionList) {
                    LOG.debug("Process execution id: " + executionImpl.getId());
                }            
            }
        } catch (RuntimeException re) {
            LOG.error("RuntimeException while handling control", re);
            throw re;
        } catch (CommandException t) {
            LOG.error("Error while handling control", t);
            throw new RuntimeException(t);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessService#findControlExecution(java.lang.String)
     */
    @Override
    public List<ExecutionImpl> findControlExecution(final String uuidControl) {
        DetachedCriteria executionCrit = DetachedCriteria.forClass(ExecutionImpl.class);
        String processDefinitionId = findProcessDefinitionId(IControlExecutionProcess.KEY);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Latest processDefinitionId: " + processDefinitionId);
        }
        executionCrit.add(Restrictions.eq("processDefinitionId", processDefinitionId));
        DetachedCriteria variableCrit = executionCrit.createCriteria("variables");
        variableCrit.add(Restrictions.eq("key", IGenericProcess.VAR_UUID));
        variableCrit.add(Restrictions.eq("string", uuidControl));
        return getJbpmExecutionDao().findByCriteria(executionCrit);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessService#findControlExecution(java.lang.String)
     */
    @Override
    public List<ExecutionImpl> findIsaExecution(final String uuid) {
        return findExecutionForElement(IIsaExecutionProcess.KEY,uuid);       
    }

    /**
     * True: This is a real implementation.
     * 
     * @see sernet.verinice.interfaces.bpm.IProcessServiceIsa#isActive()
     */
    @Override
    public boolean isActive() {
        return true;
    }
    
    /**
     * @param isaAudit
     * @return
     */
    private CnATreeElement loadOrganization(CnATreeElement element) {
        if(Organization.TYPE_ID.equals(element.getTypeId())) {
            return element;
        } else {
            element = getElementDao().findByUuid(element.getUuid(), RetrieveInfo.getPropertyInstance().setParent(true));
            return loadOrganization(element.getParent());
        }
    }

    /**
     * @param context
     * @return
     */
    private IsaProcessContext startProcessForControlGroup(IsaProcessContext context) {
        ControlGroup controlGroup = context.getControlGroup();
        controlGroup = getControlGroupDao().findByUuid(controlGroup.getUuid(), RetrieveInfo.getChildrenInstance());
        for (CnATreeElement element : controlGroup.getChildren()) {
            if(SamtTopic.TYPE_ID.equals(element.getTypeId())) {
                RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                ri.setLinksDown(true);
                SamtTopic samtTopic = getSamtTopicDao().findByUuid(element.getUuid(), ri);
                context.setSamtTopic(samtTopic);
                context = handleSamtTopic(context);
            }
            if(ControlGroup.TYPE_ID.equals(element.getTypeId())) {
                context.setControlGroup((ControlGroup) element);
                context = startProcessForControlGroup(context);
            }        
        }
        return context;
    }
       
    /**
     * @param context
     * @return
     */
    private IsaProcessContext handleSamtTopic(IsaProcessContext context) {
        try {
            SamtTopic samtTopic = context.getSamtTopic();
            String uuid = samtTopic.getUuid();
            List<ExecutionImpl> executionList = findIsaExecution(uuid);
            if(executionList==null || executionList.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No process for isa topic: " + uuid);
                }
                // start process if control is not implemented
                if(SamtTopic.IMPLEMENTED_NOTEDITED_NUMERIC == samtTopic.getMaturity()) {
                    startIsaExecution(context);
                    context.increaseProcessNumber();
                }
                if (LOG.isInfoEnabled()) {
                    LOG.info("Process started for isa topic: " + uuid);
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Process found for isa topic: " + uuid);
                for (ExecutionImpl executionImpl : executionList) {
                    LOG.debug("Process execution id: " + executionImpl.getId());
                }            
            }
            return context;
        } catch (RuntimeException re) {
            LOG.error("RuntimeException while handling isa topic", re);
            throw re;
        } catch (Exception t) {
            LOG.error("Error while handling isa topic", t);
            throw new RuntimeException(t);
        }
    }
    
      
    /**
     * @param control
     * @throws CommandException 
     */
    private void startControlExecution(Control control) throws CommandException {      
        Map<String, Object> props = new HashMap<String, Object>();
        
        String username = getProcessDao().getAssignee(control);
        
        props.put(IControlExecutionProcess.VAR_ASSIGNEE_NAME, username);
        props.put(IGenericProcess.VAR_UUID, control.getUuid());
        props.put(IGenericProcess.VAR_TYPE_ID, control.getTypeId());    
        props.put(IControlExecutionProcess.VAR_OWNER_NAME, getOwnerName(control));
        props.put(IControlExecutionProcess.VAR_IMPLEMENTATION, control.getImplementation());
        Date duedate = control.getDueDate();
        Date now = new Date(System.currentTimeMillis());
        if(duedate!=null && now.before(duedate)) {
            props.put(IControlExecutionProcess.VAR_DUEDATE, duedate);
        } else {
            props.put(IControlExecutionProcess.VAR_DUEDATE, IControlExecutionProcess.DEFAULT_DUEDATE);
        }
        
        
        startProcess(IControlExecutionProcess.KEY, props);     
    }
    
    /**
     * @param topic
     * @throws CommandException 
     */
    private void startIsaExecution(IsaProcessContext context) throws CommandException {   
        SamtTopic topic = context.getSamtTopic();
        Map<String, Object> props = new HashMap<String, Object>();      
        String username = getProcessDao().getAssignee(topic);  
        props.put(IIsaExecutionProcess.VAR_ASSIGNEE_NAME, username);
        props.put(IGenericProcess.VAR_UUID, topic.getUuid());
        props.put(IGenericProcess.VAR_TYPE_ID, topic.getTypeId());
        props.put(IIsaExecutionProcess.VAR_OWNER_NAME, getOwnerName(topic));
        props.put(IIsaExecutionProcess.VAR_IMPLEMENTATION, topic.getMaturity());
        if(context.getUuidAudit()!=null) {
            props.put(IIsaExecutionProcess.VAR_AUDIT_UUID, context.getUuidAudit()); 
        }
        Date duedate = topic.getCompleteUntil();
        Date now = new Date(System.currentTimeMillis());
        if(duedate!=null && now.before(duedate)) {
            props.put(IGenericProcess.VAR_DUEDATE, duedate);
        } else {
            props.put(IGenericProcess.VAR_DUEDATE, IControlExecutionProcess.DEFAULT_DUEDATE);
        }
             
        startProcess(IIsaExecutionProcess.KEY, props);     
    }
    
    public IBaseDao<Audit, Integer> getAuditDao() {
        return auditDao;
    }

    public void setAuditDao(IBaseDao<Audit, Integer> auditDao) {
        this.auditDao = auditDao;
    }

    public IBaseDao<ControlGroup, Integer> getControlGroupDao() {
        return controlGroupDao;
    }

    public void setControlGroupDao(IBaseDao<ControlGroup, Integer> controlGroupDao) {
        this.controlGroupDao = controlGroupDao;
    }

    public IBaseDao<SamtTopic, Integer> getSamtTopicDao() {
        return samtTopicDao;
    }
    
    public void setSamtTopicDao(IBaseDao<SamtTopic, Integer> samtTopicDao) {
        this.samtTopicDao = samtTopicDao;
    }
    
}
