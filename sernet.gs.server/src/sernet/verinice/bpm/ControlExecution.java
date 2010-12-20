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

import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.server.ServerInitializer;
import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ControlExecution {

    private final Logger log = Logger.getLogger(ControlExecution.class);
    
    ICommandService commandService;
    
    public String loadAssignee(String uuidControl) {
        ServerInitializer.inheritVeriniceContextState();
        String uuidAssignee = null;
        try {
            RetrieveInfo ri = new RetrieveInfo();
            ri.setLinksUp(true);
            LoadElementByUuid<Control> command = new LoadElementByUuid(Control.TYPE_ID,uuidControl,ri);
            command = getCommandService().executeCommand(command);
            Control control = command.getElement();
            Set<CnALink> linkSet = control.getLinksDown();
            for (CnALink link : linkSet) {
                if(Control.REL_CONTROL_PERSON_ISO.equals(link.getRelationId())) {
                    uuidAssignee = link.getDependency().getUuid();
                    break;
                }
            }
        } catch(Throwable t) {
            log.error("Error while loading assignee.", t);
        }
        if (log.isDebugEnabled()) {
            log.debug("uuid control: " + uuidControl + ", uuid assignee: " + uuidAssignee);
        }
        return uuidAssignee;
    }
    
    public String loadImplementation(String uuidControl) {
        ServerInitializer.inheritVeriniceContextState();
        String implementation = null;
        try {
            RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
            LoadElementByUuid<Control> command = new LoadElementByUuid(Control.TYPE_ID,uuidControl,ri);
            command = getCommandService().executeCommand(command);
            Control control = command.getElement();
            implementation = control.getImplementation();
        } catch(Throwable t) {
            log.error("Error while loading implementation.", t);
        }
        if (log.isDebugEnabled()) {
            log.debug("uuid control: " + uuidControl + ", implementation: " + implementation);
        }
        return implementation;
    }
    
    private ICommandService getCommandService() {
        if(commandService==null) {
            commandService = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
        }
        return commandService;
    }
}
