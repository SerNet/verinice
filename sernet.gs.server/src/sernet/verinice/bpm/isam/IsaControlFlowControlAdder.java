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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.verinice.bpm.ProzessExecution;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.service.commands.CopyCommand;
import sernet.verinice.service.commands.CreateElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaControlFlowControlAdder extends ProzessExecution {

    private static final Logger LOG = Logger.getLogger(IsaControlFlowControlAdder.class);
    private static final String VERIFICATION_SUFFIX = " Follow Up";
    
    public String addControlToAudit(String uuid, String uuidAudit) {
        try {
            ServerInitializer.inheritVeriniceContextState();
            CnATreeElement control = loadElementByUuid(uuid);       
            CnATreeElement audit = loadElementByUuid(uuidAudit); 
            CnATreeElement auditGroup = loadElement(AuditGroup.TYPE_ID,audit.getParentId(), RetrieveInfo.getPropertyChildrenInstance());           
            String title = audit.getTitle() + VERIFICATION_SUFFIX;
            Audit verificationAudit = findOrCreateAudit(auditGroup, title);
            copyElementToAudit(control,verificationAudit);
            return verificationAudit.getUuid();
        } catch(CommandException t) {
            LOG.error("Error while addind control to audit.", t); //$NON-NLS-1$
            return null;
        }
    }

    private Audit findOrCreateAudit(CnATreeElement auditGroup, String title) throws CommandException {
        Audit audit = findAudit(auditGroup, title);
        if(audit==null) {
            audit = createAudit(auditGroup, title);
            if (LOG.isInfoEnabled()) {
                LOG.info("Audit created: " + title + ", uuid: " + audit.getUuid());
            }
        }
        return audit;
    }
    
    private Audit findAudit(CnATreeElement auditGroup, String title) throws CommandException {
        Audit audit = null;
        for (CnATreeElement child : auditGroup.getChildren()) {
            child = loadElementByUuid(child.getUuid()); 
            if(child.getTitle().equals(title)) {
                audit = (Audit) child;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Audit found: " + title + ", uuid: " + audit.getUuid());
                }
                break;
            }
        }
        return audit;
    }

    private Audit createAudit(CnATreeElement auditGroup, String title) throws CommandException {
        CreateElement<Audit> command = new CreateElement<Audit>(auditGroup, Audit.TYPE_ID,title,false, true);
        command = getCommandService().executeCommand(command);      
        return command.getNewElement();
    }

    private void copyElementToAudit(CnATreeElement control, Audit audit) throws CommandException {
         List<CnATreeElement> dirList = getDirListInAudit(control, new LinkedList<CnATreeElement>());
         CnATreeElement parent = audit;
         for (CnATreeElement dir : dirList) {
             parent = findOrAddDirectories(parent,dir);
         }
         boolean exitst = false;
         for (CnATreeElement child : parent.getChildren()) {
             if(child.getTitle().equals(control.getTitle())) {
                 exitst = true;
                 break;
             }
         }
         if(!exitst) {
             CopyCommand copyCommand = new CopyCommand(parent.getUuid(), Arrays.asList(new String[]{control.getUuid()}));
             copyCommand = getCommandService().executeCommand(copyCommand);
             if (LOG.isDebugEnabled()) {
                 LOG.debug("A copy of the control created in audit, orginal control uuid: " + control.getUuid());
             }
         } else if (LOG.isDebugEnabled()) {
            LOG.debug("A copy of the control already exists in audit, orginal control uuid: " + control.getUuid());
        }
    }     

    private List<CnATreeElement> getDirListInAudit(CnATreeElement element, List<CnATreeElement> dirList) throws CommandException {
        List<CnATreeElement> dirList0 = new LinkedList<CnATreeElement>();
        
        if(element.getParent()!=null) {
            RetrieveInfo ri = RetrieveInfo.getPropertyInstance().setParent(true);
            CnATreeElement parent = loadElementByUuid(element.getParent().getUuid(), ri);
            if(!parent.getTypeId().equals(Audit.TYPE_ID)) {          
                dirList0 = getDirListInAudit(parent, dirList);
                dirList0.add(parent);
            }
        }
        return dirList0;
    }
    
    private CnATreeElement findOrAddDirectories(CnATreeElement parent, CnATreeElement dir) throws CommandException {
        boolean exitst = false;
        for (CnATreeElement child : parent.getChildren()) {
            if(child.getTitle().equals(dir.getTitle())) {
                exitst = true;
                dir = child;
                break;
            }
        }
        if(!exitst) {
            CreateElement command = new CreateElement(parent, dir.getTypeId(),dir.getTitle());
            command = getCommandService().executeCommand(command);      
            dir = command.getNewElement(); 
        }
        return dir;
    }

     
}
