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

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.LoadUsername;

/**
 * Execution class for a jBPM Java task of process control-execution
 * defined in sernet/verinice/bpm/control-execution.jpdl.xml.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ControlExecution {

    private final Logger log = Logger.getLogger(ControlExecution.class);
    
    private ICommandService commandService;
    
    /**
     * Loads an assignee for a {@link Control}.
     * An assignee of an control is an {@link PersonIso}
     * linked to the control.
     * Returns the username of the {@link Configuration}
     * connected to PersonIso. If there is no linked PersonIso
     * or no configuration for PersonIso <code>null</code> is returned.
     * 
     * @param uuid uuid of an control
     * @return username of the assignee
     */
    public String loadAssignee(String uuidControl) {
        ServerInitializer.inheritVeriniceContextState();
        String username = null;
        try {
            LoadUsername command = new LoadUsername(uuidControl,Control.REL_CONTROL_PERSON_ISO);
            command = getCommandService().executeCommand(command);
            username = command.getUsername();
        } catch(Exception t) {
            log.error("Error while loading assignee.", t); //$NON-NLS-1$
        }
        if (log.isDebugEnabled()) {
            log.debug("uuid control: " + uuidControl + ", username: " + username); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return username;
    }
    
    
    /**
     * Returns the implementation state of the an control
     * (SNCA property "control_implemented").
     * 
     * @param uuid uuid of an control
     * @return implementation state of the an control
     */
    public String loadImplementation(String uuidControl) {
        ServerInitializer.inheritVeriniceContextState();
        String implementation = null;
        try {
            LoadElementByUuid<Control> command = new LoadElementByUuid(Control.TYPE_ID,uuidControl,RetrieveInfo.getPropertyInstance());
            command = getCommandService().executeCommand(command);
            Control control = command.getElement();
            implementation = control.getImplementation();
        } catch(Exception t) {
            log.error("Error while loading implementation.", t); //$NON-NLS-1$
        }
        if (log.isDebugEnabled()) {
            log.debug("uuid control: " + uuidControl + ", implementation: " + implementation); //$NON-NLS-1$ //$NON-NLS-2$
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
