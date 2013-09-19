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
package sernet.verinice.bpm;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.bsi.dialogs.CnATreeElementSelectionDialog;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.verinice.bpm.rcp.CompletionAbortedException;
import sernet.verinice.interfaces.bpm.IIsaQmProcess;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * Task complete client handler for task IIsaQmProcess.TASK_IQM_SET_ASSIGNEE 
 * and transition IIsaQmProcess.TRANS_IQM_COMPLETE.
 * 
 * This handler opens a dialog to choose a person. Login name of the person is
 * returned as parameter IIsaQmProcess.VAR_IQM_ASSIGNEE in the parameter map.
 * 
 * Handler is registered in {@link CompleteHandlerRegistry}
 * 
 * @see {@link CompleteHandlerRegistry}
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class SetAssigneeClientHandler implements ICompleteClientHandler {

    private static final Logger LOG = Logger.getLogger(SetAssigneeClientHandler.class);
    
    private Shell shell;
    
    private int dialogStatus;
    
    /**
     * Opens a dialog to choose a person. Login name of the person is
     * returned as parameter IIsaQmProcess.VAR_IQM_ASSIGNEE in the parameter map.
     * 
     * @see sernet.verinice.interfaces.bpm.ICompleteClientHandler#execute()
     */
    @Override
    public Map<String, Object> execute() {
        Map<String, Object> parameter = null;
        try {                             
            final CnATreeElementSelectionDialog dialog = new CnATreeElementSelectionDialog(shell, PersonIso.TYPE_ID, null);            
            dialog.setScopeOnly(false);
            dialog.setShowScopeCheckbox(false);
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    dialogStatus = dialog.open();
                }
            });
            if (dialogStatus == Window.OK) {         
                List<CnATreeElement> userList = dialog.getSelectedElements();
                if(userList.size()==1) {
                    CnATreeElement element = userList.get(0);                
                    LoadConfiguration command = new LoadConfiguration(element);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    Configuration configuration = command.getConfiguration();
                    if(configuration!=null) {
                        parameter = new Hashtable<String, Object>();
                        parameter.put(IIsaQmProcess.VAR_IQM_ASSIGNEE, configuration.getUser());
                    }                           
                }
            } else {
                throw new CompletionAbortedException("Canceled by user.");
            }
        } catch(CompletionAbortedException e) {
            throw e;
        } catch(Exception e) {
            LOG.error("Error while assigning user to task.", e);
        }
        return parameter;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ICompleteClientHandler#setShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    public void setShell(Shell shell) {
        this.shell = shell;      
    }

}
