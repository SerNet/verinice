/*******************************************************************************
 * Copyright (c) 2017 Alexander Ben Nasrallah.
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
 *    Alexander Ben Nasrallah an[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.actions.ActionFactory;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.bsi.actions.DeleteHandler;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.iso27k.rcp.Messages;

public class DeleteSelectionAction extends RightsEnabledAction
        implements ISelectionChangedListener {

    public static final String ID = "sernet.verinice.iso27k.rcp.action.deleteselectionaction"; //$NON-NLS-1$

    private ISelection selection;

    public DeleteSelectionAction() {
        setText(Messages.CatalogView_delete);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.CROSS));
        setId(ID);
        setRightID(ActionRightIDs.CATALOGDELETE);
        if (Activator.getDefault().isStandalone()
                && !Activator.getDefault().getInternalServer().isRunning()) {
            IInternalServerStartListener listener = new IInternalServerStartListener() {
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if (e.isStarted()) {
                        setEnabled(checkRights());
                    }
                }

            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            setEnabled(checkRights());
        }

        setDisabledImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.CROSS));
        setActionDefinitionId(ActionFactory.DELETE.getCommandId());
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        selection = event.getSelection();
        setEnabled(CnAElementFactory.selectionOnlyContainsScopes((IStructuredSelection) selection)
                && checkRights());
    }


    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        new DeleteHandler().execute((IStructuredSelection) selection);
    }
}
