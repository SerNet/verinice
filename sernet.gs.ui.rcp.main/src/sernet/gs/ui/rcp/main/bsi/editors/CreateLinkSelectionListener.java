/*******************************************************************************
 * Copyright (c) 2015 Moritz Reiter.
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
 *     Moritz Reiter - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.bsi.editors;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.bsi.dialogs.CnATreeElementSelectionDialog;
import sernet.gs.ui.rcp.main.common.model.LinkUtil;
import sernet.hui.common.connect.DirectedHuiRelation;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Moritz Reiter
 */
final class CreateLinkSelectionListener implements SelectionListener {

    private final LinkMaker linkMaker;

    public CreateLinkSelectionListener(LinkMaker linkMaker) {
        this.linkMaker = linkMaker;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        Object[] elementTypeNamesAndIds = linkMaker.elementTypeNamesAndIds.entrySet().toArray();
        int selectionIndex = linkMaker.comboElementType.getSelectionIndex();
        Shell shell = linkMaker.viewer.getControl().getShell();

        @SuppressWarnings("unchecked")
        String selectedElementType = ((Entry<String, String>) elementTypeNamesAndIds[selectionIndex])
                .getValue();

        CnATreeElementSelectionDialog dialog = new CnATreeElementSelectionDialog(
                shell, selectedElementType, linkMaker.inputElmt);

        if (dialog.open() != Window.OK) {
            return;
        }

        List<CnATreeElement> linkTargets = dialog.getSelectedElements();

        DirectedHuiRelation selectedRelation;
        StructuredSelection selection = (StructuredSelection) linkMaker.relationComboViewer
                .getSelection();
        selectedRelation = (DirectedHuiRelation) selection.getFirstElement();

        Set<CnATreeElement> linkTargetsSet = new HashSet<>(linkTargets);
        String relationId = selectedRelation.getHuiRelation().getId();
        LinkMaker.prefStore.putValue(LinkMaker.LAST_SELECTED_RELATION_PREF_PREFIX
                + linkMaker.inputElmt.getTypeId() + linkMaker.selectedElementTypeId, relationId);

        if (selectedRelation.isForward()) {
            LinkUtil.createLinks(linkMaker.getInputElmt(), linkTargetsSet, relationId);
        } else {
            LinkUtil.createLinks(linkTargetsSet, linkMaker.getInputElmt(), relationId);
        }

        // refresh viewer because it doesn't listen to events:
        linkMaker.reloadLinks();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }
}
