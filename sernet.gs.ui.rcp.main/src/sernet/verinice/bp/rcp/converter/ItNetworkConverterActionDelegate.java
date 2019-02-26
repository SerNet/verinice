/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin
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
 *     Daniel Murygin - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp.converter;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.NonModalWizardDialog;
import sernet.verinice.rcp.RightsEnabledActionDelegate;

/**
 * This action delegate class opens a wizard to convert one or more IT networks
 * from old IT base protection to new IT base protection.
 *
 * This action delegate class is configured in plugin.xml of bundle
 * sernet.gs.ui.rcp.main. See extension point: "org.eclipse.ui.popupMenus".
 *
 * @see ItNetworkConverterAction
 * @author Daniel Murygin
 */
public class ItNetworkConverterActionDelegate extends RightsEnabledActionDelegate
        implements IWorkbenchWindowActionDelegate {

    private static final Logger log = Logger.getLogger(ItNetworkConverterActionDelegate.class);

    private CnATreeElement selectedItNetwork;
    private Shell shell;

    @Override
    public void doRun(IAction action) {
        try {
            TitleAreaDialog wizardDialog = openWizard();
            if (wizardDialog.open() == Window.OK) {
                MessageDialog.openInformation(getShell(),
                        Messages.ItNetworkConverterActionDelegate_DialogTitle,
                        Messages.ItNetworkConverterActionDelegate_ConvertingFinished);
            }
        } catch (Exception e) {
            MessageDialog.openError(getShell(),
                    Messages.ItNetworkConverterActionDelegate_DialogTitle,
                    Messages.ItNetworkConverterActionDelegate_Error);
        }
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.CONVERT_IT_NETWORK;
    }

    @Override
    public void init(IWorkbenchWindow window) {
        try {
            shell = window.getShell();
        } catch (Exception t) {
            log.error("Error creating ItNetworkConverterActionDelegate", t); //$NON-NLS-1$
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof ITreeSelection) {
            selectedItNetwork = null;
            ITreeSelection selectionCurrent = (ITreeSelection) selection;
            for (Iterator<?> iter = selectionCurrent.iterator(); iter.hasNext();) {
                Object selectedObject = iter.next();
                if (isItNetwork(selectedObject)) {
                    selectedItNetwork = (CnATreeElement) selectedObject;
                }
            }
        }
    }

    private boolean isItNetwork(Object element) {
        return element instanceof ITVerbund;
    }

    private Shell getShell() {
        return shell;
    }

    private TitleAreaDialog openWizard() {
        ItNetworkConverterWizard wizard = new ItNetworkConverterWizard(selectedItNetwork);
        return new NonModalWizardDialog(getShell(), wizard);

    }

}
