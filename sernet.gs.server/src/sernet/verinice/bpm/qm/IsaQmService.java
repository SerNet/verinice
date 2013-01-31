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
import java.util.Map;

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
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaQmService extends ProcessServiceVerinice implements IIsaQmService {

    // Dao members (injected by Spring)
    private IBaseDao<CnATreeElement, Integer> elementDao;
    
    private IAuthService authService;
    
    private String defaultAssignee;
    
    public IsaQmService() {
        super();
        // this is not the main process service:
        setWasInitCalled(true);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIsaQmService#startProcessesForElement(java.lang.String, java.lang.Object, java.lang.String)
     */
    @Override
    public IProcessStartInformation startProcessesForElement(String uuid, Object feedback, String priority) {
        return startProcessesForElement(uuid, null, feedback, priority);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIsaQmService#startProcessesForControl(java.lang.String)
     */
    @Override
    public IProcessStartInformation startProcessesForElement(String uuid, String auditUuid, Object feedback, String priority) {
        IsaQmContext context = new IsaQmContext();
        CnATreeElement element = loadElement(uuid);
        context.setElement(element);
        context.setOwnerName(getAuthService().getUsername());
        context.setUuidAudit(auditUuid);
        if(feedback!=null) {
            context.setComment(feedback);
        }
        context.setPriority(priority);
        startProcess(context);         
        return new ProcessInformation(context.getNumberOfProcesses());
    }
    
    private void startProcess(/*not final*/IsaQmContext context) {
        CnATreeElement control = context.getElement();
        Map<String, Object> props = new HashMap<String, Object>();      
        String username = getAuthService().getUsername(); 
        props.put(IGenericProcess.VAR_ASSIGNEE_NAME, username);
        props.put(IGenericProcess.VAR_UUID, control.getUuid()); 
        props.put(IGenericProcess.VAR_TYPE_ID, control.getTypeId());            
        props.put(IGenericProcess.VAR_OWNER_NAME, context.getOwnerName());
        if(context.getUuidAudit()!=null) {
            props.put(IGenericProcess.VAR_AUDIT_UUID, context.getUuidAudit());
        }
        Object comment = context.getComment();
        if(comment instanceof String) {
            String text = (String) comment;
            text = text.trim();
            if(text.isEmpty()) {
                text = null;
            }
            comment = text;
        }
        props.put(IIsaQmProcess.VAR_FEEDBACK, comment);
        props.put(IGenericProcess.VAR_PRIORITY, context.getPriority());
        props.put(IIsaQmProcess.VAR_IQM_REVIEW, getDefaultAssignee());
        startProcess(IIsaQmProcess.KEY, props);
        context.increaseProcessNumber(); 
    }
    
    private CnATreeElement loadElement(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        return getElementDao().findByUuid(uuid, ri);
    }

    public String getDefaultAssignee() {
        return defaultAssignee;
    }

    public void setDefaultAssignee(String defaultAssignee) {
        this.defaultAssignee = defaultAssignee;
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
