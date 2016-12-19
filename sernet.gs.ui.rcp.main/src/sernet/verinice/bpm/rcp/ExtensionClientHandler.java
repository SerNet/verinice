/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
package sernet.verinice.bpm.rcp;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.bpm.ICompleteClientHandler;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.ITask;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ExtensionClientHandler implements ICompleteClientHandler {

    private static final Logger LOG = Logger.getLogger(ExtensionClientHandler.class);
    
    private Shell shell;
    
    private int dialogStatus;
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.ICompleteClientHandler#execute()
     */
    @Override
    public Map<String, Object> execute(ITask task) {
        Map<String, Object> parameter = new Hashtable<String, Object>();
        try {                             
            final DescriptionDialog dialog = new DescriptionDialog(shell);
            Display.getDefault().syncExec(new Runnable() {
                @Override
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
            LOG.error("Error while apply for extension.", e);
        }
        return parameter;
    }

    private Map<String, Object> getParameterFromDialog(DescriptionDialog dialog) {
        Map<String, Object> parameter = new Hashtable<String, Object>();
        parameter.put(IIndividualProcess.VAR_EXTENSION_JUSTIFICATION, dialog.getDescription());     
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
