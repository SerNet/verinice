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
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public class ExportJobChangeListener extends JobChangeAdapter {
    String path;
    String title;

    public ExportJobChangeListener(String path, String title) {
        super();
        this.path = path;
        this.title = title;
    }

    /*
     * @see
     * org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.
     * runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void done(IJobChangeEvent event) {
        if (Status.OK_STATUS.equals(event.getResult())) {
            Display.getDefault().asyncExec(() -> MessageDialog.openInformation(
                    Display.getDefault().getActiveShell(), Messages.getString("ExportAction_2"),
                    NLS.bind(Messages.getString("ExportAction_3"), new Object[] { title, path })));
        }
    }
}