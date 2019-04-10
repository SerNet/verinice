/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.rcp;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;

/**
 * A handler to add groups to new groups for ISO2700 and base protection
 * elements. The handler can update the element that triggered it
 */
public abstract class UpdatingAddGroupHandler extends AddGroupHandler implements IElementUpdater {

    /*
     * @see
     * org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.
     * menus.UIElement, java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void updateElement(UIElement menu, Map arg1) {
        CnATreeElement selectedElement = getSelectedElement();
        if (selectedElement != null) {
            configureMenu(menu, selectedElement);
        }
    }

    private void configureMenu(UIElement menu, CnATreeElement selectedElement) {
        boolean allowed = CnAElementHome.getInstance().isNewChildAllowed(selectedElement);
        boolean enabled = false;
        if (selectedElement instanceof Audit) {
            menu.setText(Messages.AddGroupHandler_new_group);
        } else if (selectedElement instanceof Group<?>) {
            enabled = true;
            Group<?> group = (Group<?>) selectedElement;
            String childTypeId = group.getChildTypes()[0];
            if (selectedElement instanceof Asset) {
                childTypeId = Control.TYPE_ID;
            }
            String imageUrl = ImageCache.getInstance().getImageURL(childTypeId);
            ImageDescriptor imageDescriptor = ImageCache.getInstance().getImageDescriptor(imageUrl);
            menu.setIcon(imageDescriptor);
            menu.setText(Optional
                    .ofNullable(AddGroupMessageHelper.getMessageForAddGroup(group.getTypeId()))
                    .orElse(Messages.AddGroupHandler_new_group));
        }
        // Only change state when it is enabled, since we do not want to
        // trash the enablement settings of plugin.xml
        if (this.isEnabled()) {
            this.setEnabled(allowed && enabled);
        }
    }

    private CnATreeElement getSelectedElement() {
        CnATreeElement element = null;
        ISelection selection = getSelection();
        if (selection instanceof IStructuredSelection) {
            Object sel = ((IStructuredSelection) selection).getFirstElement();
            if (sel instanceof CnATreeElement) {
                element = (CnATreeElement) sel;
            }
        }
        return element;
    }

    private ISelection getSelection() {
        Activator activator = Activator.getDefault();
        IWorkbench workbench = activator.getWorkbench();
        IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
        ISelectionService selectionService = workbenchWindow.getSelectionService();
        return selectionService.getSelection();
    }
}
