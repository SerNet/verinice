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
package sernet.verinice.bpm.qm;

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
import sernet.verinice.interfaces.bpm.IIsaQmProcess;
import sernet.verinice.interfaces.bpm.IIsaQmService;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.bpm.ProcessInformation;
import sernet.verinice.model.iso27k.Control;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaQmService extends ProcessServiceVerinice implements IIsaQmService {

    private static final Logger LOG = Logger.getLogger(IsaQmService.class);
    
    // Dao members (injected by Spring)
    private IBaseDao<Control, Integer> controlDao;
    
    private IAuthService authService;
    
    public IsaQmService() {
        super();
        // this is not the main process service:
        wasInitCalled = true;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIsaQmService#startProcessesForControl(java.lang.String)
     */
    @Override
    public IProcessStartInformation startProcessesForControl(String controlUuid, String auditUuid) {
        IsaQmContext context = new IsaQmContext();
        Control control = loadControl(controlUuid);
        context.setControl(control);
        context.setOwnerName(getAuthService().getUsername());
        context.setUuidAudit(auditUuid);
        context.setComment(control.getFeedbackNote());
        context = startProcessIfMissing(context);  
        return new ProcessInformation(context.getNumberOfProcesses());
    }

    private IsaQmContext startProcessIfMissing(IsaQmContext context) {
        Control control = context.getControl();
        String uuid = control.getUuid();
        List<ExecutionImpl> executionList = findExecutionForElement(IIsaQmProcess.KEY, uuid);
        if(executionList==null || executionList.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No process for control: " + uuid);
            }
            startProcess(context);
        }
        return context;
    }
    
    private void startProcess(IsaQmContext context) {
        Control control = context.getControl();
        Map<String, Object> props = new HashMap<String, Object>();      
        String username = getAuthService().getUsername(); 
        props.put(IGenericProcess.VAR_ASSIGNEE_NAME, username);
        props.put(IGenericProcess.VAR_UUID, control.getUuid()); 
        props.put(IGenericProcess.VAR_TYPE_ID, control.getTypeId());            
        props.put(IGenericProcess.VAR_OWNER_NAME, context.getOwnerName());
        props.put(IGenericProcess.VAR_AUDIT_UUID, context.getUuidAudit());
        String comment = context.getComment();
        if(comment!=null) {
            comment = comment.trim();
            if(comment.isEmpty()) {
                comment = null;
            }
        }
        props.put(IIsaQmProcess.VAR_FEEDBACK, comment);
        startProcess(IIsaQmProcess.KEY, props);
        context.increaseProcessNumber(); 
    }
    
    private Control loadControl(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        Control control = getControlDao().findByUuid(uuid, ri);
        return control;
    }

    public IBaseDao<Control, Integer> getControlDao() {
        return controlDao;
    }
    public void setControlDao(IBaseDao<Control, Integer> controlDao) {
        this.controlDao = controlDao;
    }


    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

}
