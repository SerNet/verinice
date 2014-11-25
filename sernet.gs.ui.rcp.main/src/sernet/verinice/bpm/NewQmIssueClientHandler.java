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
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.bpm.rcp.CompletionAbortedException;
import sernet.verinice.bpm.rcp.NewQmIssueDialog;
import sernet.verinice.interfaces.bpm.IIsaQmProcess;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class NewQmIssueClientHandler implements ICompleteClientHandler {

    private static final Logger LOG = Logger.getLogger(NewQmIssueClientHandler.class);
    
    private Shell shell;
    
    private int dialogStatus;
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.ICompleteClientHandler#execute()
     */
    @Override
    public Map<String, Object> execute() {
        Map<String, Object> parameter = new Hashtable<String, Object>();
        try {                             
            final NewQmIssueDialog dialog = new NewQmIssueDialog(shell);
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    dialogStatus = dialog.open();
                }
            });
            if (dialogStatus == Window.OK) {         
                parameter = getParameterFromDialog(dialog);
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

  
    private Map<String, Object> getParameterFromDialog(NewQmIssueDialog dialog) {
        Map<String, Object> parameter = new Hashtable<String, Object>();
        parameter.put(IIsaQmProcess.VAR_FEEDBACK, dialog.getDescription());
        parameter.put(IIsaQmProcess.VAR_QM_PRIORITY, dialog.getPriority());      
        return parameter;
    }



    /* (non-Javadoc)
     * @see sernet.verinice.bpm.ICompleteClientHandler#setShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    public void setShell(Shell shell) {
        this.shell = shell;
    }

}
