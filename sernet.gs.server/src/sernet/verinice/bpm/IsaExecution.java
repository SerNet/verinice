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
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.CheckWritingPermission;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.LoadUsername;

/**
 * Execution class for a jBPM Java task of process isa-execution
 * defined in sernet/verinice/bpm/isa-execution.jpdl.xml.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaExecution {

    private final Logger log = Logger.getLogger(IsaExecution.class);
    
    ICommandService commandService;
    
    /**
     * Loads an assignee for a {@link SamtTopic} (ISA topic).
     * An assignee of an ISA topic is an {@link PersonIso}
     * linked to the topic.
     * Returns the username of the {@link Configuration}
     * connected to PersonIso. If there is no linked PersonIso
     * or no configuration for PersonIso <code>null</code> is returned.
     * 
     * @param uuid uuid of an ISA topic
     * @return username of the assignee
     */
    public String loadAssignee(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        String username = null;
        try {
            LoadUsername command = new LoadUsername(uuid,SamtTopic.REL_SAMTTOPIC_PERSON_ISO);
            command = getCommandService().executeCommand(command);
            username = command.getUsername();         
        } catch(Throwable t) {
            log.error("Error while loading assignee.", t); //$NON-NLS-1$
        }
        if (log.isDebugEnabled()) {
            log.debug("uuid SamtTopic: " + uuid + ", username: " + username); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return username;
    }
    
    public String loadWritePermission(String uuid, String username) {
        ServerInitializer.inheritVeriniceContextState();
        boolean isWriteAllowed = false;
        try {
            CheckWritingPermission command = new CheckWritingPermission(uuid,username);
            command = getCommandService().executeCommand(command);
            isWriteAllowed = command.isWriteAllowed();         
        } catch(Throwable t) {
            log.error("Error while loading write permission.", t); //$NON-NLS-1$
        }
        if (log.isDebugEnabled()) {
            log.debug("uuid element: " + uuid + ", username: " + username + ", write allowed: " + isWriteAllowed); //$NON-NLS-1$ //$NON-NLS-2$
        } 
        return Boolean.toString(isWriteAllowed);
    }
    
    
    /**
     * Returns the implementation state of the an ISA topic
     * (SNCA property "samt_topic_maturity").
     * 
     * @param uuid uuid of an ISA topic
     * @return implementation state of the an ISA topic
     */
    public String loadImplementation(String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        String implementation = "0";
        try {
            LoadElementByUuid<SamtTopic> command = new LoadElementByUuid(SamtTopic.TYPE_ID,uuid,RetrieveInfo.getPropertyInstance());
            command = getCommandService().executeCommand(command);
            SamtTopic topic = command.getElement();
            if(topic!=null) {
                implementation = Integer.valueOf(topic.getMaturity()).toString();
            }
        } catch(Throwable t) {
            log.error("Error while loading implementation.", t); //$NON-NLS-1$
        }
        if (log.isDebugEnabled()) {
            log.debug("uuid SamtTopic: " + uuid + ", implementation: " + implementation); //$NON-NLS-1$ //$NON-NLS-2$
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
