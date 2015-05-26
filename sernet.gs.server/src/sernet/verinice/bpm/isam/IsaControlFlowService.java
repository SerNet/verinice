/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.bpm.isam;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jbpm.pvm.internal.model.ExecutionImpl;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.verinice.bpm.ProcessServiceVerinice;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IIsaControlFlowProcess;
import sernet.verinice.interfaces.bpm.IIsaControlFlowService;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.bpm.ProcessInformation;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaControlFlowService extends ProcessServiceVerinice implements IIsaControlFlowService {

    private static final Logger LOG = Logger.getLogger(IsaControlFlowService.class);
    
    // Dao members (injected by Spring)
    private IBaseDao<Control, Integer> controlDao; 
    private IBaseDao<ControlGroup, Integer> controlGroupDao;
    private IBaseDao<Audit, Integer> auditDao;
    private IBaseDao<CnATreeElement, Integer> elementDao;
    
    private IAuthService authService;
    
    public IsaControlFlowService() {
        super();
        // this is not the main process service:
        setWasInitCalled(true);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIsaControlFlowService#startProcessesForControls(java.util.List)
     */
    @Override
    public IProcessStartInformation startProcessesForControls(List<String> selectedControlUuids) {
        IsaControlFlowContext context = new IsaControlFlowContext();
        for (String uuid : selectedControlUuids) {
            Control control = loadControl(uuid);
            context.setControl(control);
            Audit audit = retrieveAudit(control);
            if(audit!=null) {
                context.setUuidAudit(audit.getUuid());
            }
            context = startProcessIfMissing(context);            
        }       
        return new ProcessInformation(context.getNumberOfProcesses());
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIsaControlFlowService#startProcessesForGroups(java.util.List)
     */
    @Override
    public IProcessStartInformation startProcessesForGroups(List<String> groupUuids) {
        IsaControlFlowContext context = new IsaControlFlowContext();
        for (String uuid : groupUuids) {
            ControlGroup controlGroup = getControlGroupDao().findByUuid(uuid, RetrieveInfo.getChildrenInstance());
            context.setControlGroup(controlGroup);
            Audit audit = retrieveAudit(controlGroup);
            if(audit!=null) {
                context.setUuidAudit(audit.getUuid());
            }
            startProcessesForGroup(context);
            
        }
        return new ProcessInformation(context.getNumberOfProcesses());
    }

    /**
     * @param controlGroup
     * @return
     */
    private Audit retrieveAudit(CnATreeElement element) {
        Audit audit = null;
        if(element!=null) {
            CnATreeElement parent = getElementDao().retrieve(element.getParentId(), new RetrieveInfo());          
            if(parent!=null && Audit.TYPE_ID.equals(parent.getTypeId())) {
                audit = (Audit) parent;
            } else {
                audit =retrieveAudit(parent);
            }
        }
        return audit;
    }

    private void startProcessesForGroup(IsaControlFlowContext context) {
        ControlGroup controlGroup = context.getControlGroup();
        for (CnATreeElement element : controlGroup.getChildren()) {
            if(Control.TYPE_ID.equals(element.getTypeId())) {
                RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                ri.setLinksDown(true);
                Control control = getControlDao().findByUuid(element.getUuid(), ri);
                context.setControl(control);
                context = startProcessIfMissing(context);
            }
            if(ControlGroup.TYPE_ID.equals(element.getTypeId())) {
                controlGroup = getControlGroupDao().findByUuid(element.getUuid(), RetrieveInfo.getChildrenInstance());
                context.setControlGroup(controlGroup);
                startProcessesForGroup(context);
            }        
        }
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIsaControlFlowService#startProcessesForAudits(java.util.List)
     */
    @Override
    public IProcessStartInformation startProcessesForAudits(List<String> auditUuids) {
        IsaControlFlowContext context = new IsaControlFlowContext();
        for (String uuid : auditUuids) {
            Audit isaAudit = getAuditDao().findByUuid(uuid, RetrieveInfo.getChildrenInstance().setParent(true));  
            ControlGroup controlGroup = getControlGroupDao().findByUuid(isaAudit.getControlGroup().getUuid(), RetrieveInfo.getChildrenInstance());
            context.setControlGroup(controlGroup);
            context.setUuidAudit(uuid);
            startProcessesForGroup(context);
        }
        return new ProcessInformation(context.getNumberOfProcesses());
    }

    private IsaControlFlowContext startProcessIfMissing(IsaControlFlowContext context) {
        Control control = context.getControl();
        String uuid = control.getUuid();
        List<ExecutionImpl> executionList = findExecutionForElement(IIsaControlFlowProcess.KEY, uuid);
        if(executionList==null || executionList.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No process for control: " + uuid);
            }
            startProcess(context);
        }
        return context;
    }
    
    private void startProcess(IsaControlFlowContext context) {
        Control control = context.getControl();
        Map<String, Object> props = new HashMap<String, Object>();      
        String username = getProcessDao().getAssignee(control);  
        props.put(IGenericProcess.VAR_ASSIGNEE_NAME, username);
        props.put(IGenericProcess.VAR_UUID, control.getUuid());  
        Date duedate = control.getDueDate();
        Date now = Calendar.getInstance().getTime();
        if(duedate.before(now)) {
            LOG.warn("Duedate is in the past, uuid of control: " + control.getUuid());
            duedate = null;
        }
        props.put(IGenericProcess.VAR_DUEDATE, duedate);       
        props.put(IGenericProcess.VAR_OWNER_NAME, getAuthService().getUsername());
        props.put(IGenericProcess.VAR_TYPE_ID, control.getTypeId());
        props.put(IGenericProcess.VAR_IMPLEMENTATION, control.getImplementation());
        props.put(IGenericProcess.VAR_AUDIT_UUID, context.getUuidAudit());
        startProcess(IIsaControlFlowProcess.KEY, props);
        context.increaseProcessNumber(); 
    }

    private Control loadControl(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        ri.setLinksDown(true);
        return getControlDao().findByUuid(uuid, ri);
    }

    public IBaseDao<Control, Integer> getControlDao() {
        return controlDao;
    }
    public void setControlDao(IBaseDao<Control, Integer> controlDao) {
        this.controlDao = controlDao;
    }
    
    public IBaseDao<ControlGroup, Integer> getControlGroupDao() {
        return controlGroupDao;
    }

    public void setControlGroupDao(IBaseDao<ControlGroup, Integer> controlGroupDao) {
        this.controlGroupDao = controlGroupDao;
    }

    public IBaseDao<Audit, Integer> getAuditDao() {
        return auditDao;
    }

    public void setAuditDao(IBaseDao<Audit, Integer> auditGroupDao) {
        this.auditDao = auditGroupDao;
    }

    @Override
    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    @Override
    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

}
