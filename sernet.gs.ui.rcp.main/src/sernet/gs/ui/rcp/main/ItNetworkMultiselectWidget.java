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
package sernet.gs.ui.rcp.main;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.ScopeMultiselectWidget;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A widget which provides IT network from old IT base protection for multi
 * selection.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ItNetworkMultiselectWidget extends ScopeMultiselectWidget {

    SelectionListener itNetworkSelectionListener;

    public ItNetworkMultiselectWidget(Composite composite, ITreeSelection selection,
            CnATreeElement selectedElement) throws CommandException {
        super(composite, selection, selectedElement);
    }

    @Override
    protected Set<Class<?>> getElementClasses() {
        Set<Class<?>> elementClasses = new HashSet<>();
        elementClasses.add(ITVerbund.class);
        return elementClasses;
    }

    @Override
    public void addSelectionListener(SelectionListener listener) {
        super.addSelectionListener(listener);
        itNetworkSelectionListener = listener;
    }

    @Override
    protected void removeCheckboxes() {
        removeListenerFromCheckboxes(itNetworkSelectionListener);
        super.removeCheckboxes();
    }

    @Override
    protected void addCheckboxes() {
        super.addCheckboxes();
        addSelectionListener(itNetworkSelectionListener);
    }

    @Override
    public boolean isShowOnlySelected() {
        return false;
    }

    @Override
    public boolean isShowOnlySelectedCheckbox() {
        return false;
    }

    @Override
    public boolean isShowSelectAllCheckbox() {
        return true;
    }

    @Override
    public boolean isShowDeselectAllCheckbox() {
        return true;
    }
}
