/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.model.common.CnALink;

/**
 * Delete items on user request.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class DeleteLinkActionDelegate implements IObjectActionDelegate {

    private IWorkbenchPart targetPart;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public void run(IAction action) {

        if (!MessageDialog.openQuestion((Shell) targetPart.getAdapter(Shell.class), Messages.DeleteLinkActionDelegate_0, Messages.DeleteLinkActionDelegate_1)) {
            return;
        }

        // close editors first:
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /*
                                                                                                   * ask
                                                                                                   * save
                                                                                                   */);

        IStructuredSelection selection = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection());

        for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object sel = iter.next();

            if (sel instanceof CnALink) {
                CnALink link = (CnALink) sel;
                try {
                    CnAElementHome.getInstance().remove(link);
                    if (CnAElementFactory.isModelLoaded()) {
                        CnAElementFactory.getLoadedModel().linkRemoved(link);
                    }
                    CnAElementFactory.getInstance().getISO27kModel().linkRemoved(link);
                } catch (Exception e) {
                    ExceptionUtil.log(e, Messages.DeleteLinkActionDelegate_2);
                }
            }
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // Realizes that the action to create a new element is greyed out,
        // when there is no right to do so.
        Object sel = ((IStructuredSelection) selection).getFirstElement();
        if (sel instanceof CnALink) {
            boolean b = CnAElementHome.getInstance().isDeleteAllowed(((CnALink) sel));

            // Only change state when it is enabled, since we do not want to
            // trash the enablement settings of plugin.xml
            if (action.isEnabled()) {
                action.setEnabled(b);
            }
        }

    }

}
