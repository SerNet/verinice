/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin
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
 *     Daniel Murygin  - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp.converter;

import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.DefaultModelLoadListener;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.NonModalWizardDialog;

/**
 * This action class opens a wizard to convert one or more IT networks from old
 * IT base protection to new IT base protection.
 *
 * @author Daniel Murygin
 */
public class ItNetworkConverterAction extends RightsEnabledAction implements ISelectionListener {

    public static final String ID = "sernet.verinice.bp.rcp.converter.itnetworkconverteraction"; //$NON-NLS-1$

    private CnATreeElement selectedItNetwork;

    public ItNetworkConverterAction(IWorkbenchWindow window) {
        super(ActionRightIDs.CONVERT_IT_NETWORK, Messages.ItNetworkConverterAction_ActionTitle);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.CONVERT));
        addLoadListener();
        window.getSelectionService().addSelectionListener(this);
    }

    @Override
    public void doRun() {
        try {
            TitleAreaDialog wizardDialog = openWizard();
            if (wizardDialog.open() == Window.OK) {
                MessageDialog.openInformation(getShell(),
                        Messages.ItNetworkConverterAction_DialogTitle,
                        Messages.ItNetworkConverterAction_ConvertingFinished);
            }
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.ItNetworkConverterAction_DialogTitle,
                    Messages.ItNetworkConverterAction_Error);
        }
    }

    private TitleAreaDialog openWizard() {
        ItNetworkConverterWizard wizard = new ItNetworkConverterWizard(selectedItNetwork);
        return new NonModalWizardDialog(getShell(), wizard);

    }

    private void addLoadListener() {
        CnAElementFactory.getInstance().addLoadListener(new DefaultModelLoadListener() {
            @Override
            public void closed(BSIModel model) {
                setEnabled(false);
            }

            @Override
            public void loaded(BpModel model) {
                setEnabled(checkRights());
            }

        });
    }

    @Override
    public void selectionChanged(IWorkbenchPart arg0, ISelection selection) {
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
        return Display.getCurrent().getActiveShell();
    }
}
