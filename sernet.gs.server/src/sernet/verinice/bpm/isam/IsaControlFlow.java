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

import org.apache.log4j.Logger;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.bpm.ProzessExecution;
import sernet.verinice.interfaces.bpm.IIsaQmService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaControlFlow extends ProzessExecution {

    private static final Logger LOG = Logger.getLogger(IsaControlFlow.class);
    
    private IIsaQmService qmService;
    
    public Date loadExecuteDuedate(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        Date duedate = null;
        try {
            CnATreeElement element = loadElementByUuid(uuid);  
            if(element instanceof Control) {
                duedate = ((Control) element).getDueDate();
                Date now = Calendar.getInstance().getTime();
                if(duedate.before(now)) {
                    LOG.warn("Duedate is in the past, uuid of control: " + element.getUuid()); //$NON-NLS-1$
                    duedate = null;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Duedete of control: " + element.getUuid() + " set to " + duedate); //$NON-NLS-1$
                }
            }
        } catch(Exception t) {
            LOG.error("Error while loading duedate.", t); //$NON-NLS-1$
        }
        return duedate;
    }
    
    public String loadImplementation(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        String implementation = null;
        try {
            CnATreeElement element = loadElementByUuid(uuid);  
            if(element instanceof Control) {
                implementation = ((Control) element).getImplementation();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Implementation of control: " + element.getUuid() + " set to " + implementation); //$NON-NLS-1$
                }
            }
        } catch(Exception t) {
            LOG.error("Error while loading implementation.", t); //$NON-NLS-1$
        }
        return implementation;
    }
    
    public String loadComment(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        String comment = null;
        try {
            CnATreeElement element = loadElementByUuid(uuid);  
            if(element instanceof Control) {
                comment = ((Control)element).getImplementationExplanation();
                if(comment!=null && comment.trim().isEmpty()) {
                    comment = null;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Comment of control: " + element.getUuid() + ": " + comment); //$NON-NLS-1$
                }
            }
        } catch(Exception t) {
            LOG.error("Error while loading comment.", t); //$NON-NLS-1$
        }
        return comment;
    }
    
    public Date loadAuditDate(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        Date date = null;
        try {
            CnATreeElement element = loadElementByUuid(uuid);  
            if(element instanceof Audit) {
                date = ((Audit) element).getStartDate();
                Date now = Calendar.getInstance().getTime();
                if(date.before(now)) {
                    LOG.warn("Audit date is in the past, uuid of audit: " + element.getUuid()); //$NON-NLS-1$
                    date = null;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Audit date of audit: " + element.getUuid() + " set to " + date); //$NON-NLS-1$
                }
            }
        } catch(Exception t) {
            LOG.error("Error while loading duedate.", t); //$NON-NLS-1$
        }
        return date;
    }

    public void startQsWorkflow(String uuid, String uuidAudit, Object feedback, String priority) {
        getQmService().startProcessesForElement(uuid, uuidAudit, feedback, priority);
    }
    
    public void remind() {
        
    }
    
    public void deadlinePassed() {
        
    }
    
    public void notResponsible() {
        
    }
    
    protected IIsaQmService getQmService() {
        if(qmService==null) {
            qmService = (IIsaQmService) VeriniceContext.get(VeriniceContext.ISA_QM_SERVICE);
        }
        return qmService;
    }
}
