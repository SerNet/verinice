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
package sernet.verinice.iso27k.rcp.action;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

public class ExportJobChangeListener implements IJobChangeListener {
    Shell shell; 
    String path;
    String title;
    public ExportJobChangeListener(Shell shell, String path, String title) {
        super();
        this.shell = shell;
        this.path = path;
        this.title = title;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void aboutToRun(IJobChangeEvent event) {}

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void awake(IJobChangeEvent event) {}

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void done(IJobChangeEvent event) {
        if(Status.OK_STATUS.equals(event.getResult())) {
            shell.getDisplay().asyncExec(new Runnable() {          
                @Override
                public void run() {
                    MessageDialog.openInformation(shell, 
                            Messages.getString("ExportAction_2"), 
                            NLS.bind(Messages.getString("ExportAction_3"), new Object[] {title, path}));
                }
            });   
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void running(IJobChangeEvent event) {}
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void scheduled(IJobChangeEvent event) {}
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void sleeping(IJobChangeEvent event) {}

}