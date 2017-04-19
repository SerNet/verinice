/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * A widget which provides organizations for multi selection.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class OrganizationMultiselectWidget extends ScopeMultiselectWidget {

    SelectionListener organizationSelectionListener;
   
    public OrganizationMultiselectWidget(Composite composite, ITreeSelection selection,
            CnATreeElement selectedElement) throws CommandException {
        super(composite, selection, selectedElement);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.ScopeMultiselectWidget#getElementClasses()
     */
    @Override
    protected Set<Class<?>> getElementClasses() {
        Set<Class<?>> elementClasses = new HashSet<>();
        elementClasses.add(Organization.class);
        return elementClasses;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.MultiselectWidget#addSelectionListener(org.eclipse.swt.events.SelectionListener)
     */
    @Override
    public void addSelectionListener(SelectionListener listener) {
        super.addSelectionListener(listener);
        organizationSelectionListener = listener;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.MultiselectWidget#removeCheckboxes()
     */
    @Override
    protected void removeCheckboxes() {
        removeListenerFromCheckboxes(organizationSelectionListener);
        super.removeCheckboxes();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.MultiselectWidget#addCheckboxes()
     */
    @Override
    protected void addCheckboxes() {
        super.addCheckboxes();
        addSelectionListener(organizationSelectionListener);
    }
    
    public boolean isShowOnlySelected() {
        return false;
    }

    public boolean isShowOnlySelectedCheckbox() {
        return false;
    }
    
    public boolean isShowSelectAllCheckbox() {
        return true;
    }
    

    public boolean isShowDeselectAllCheckbox() {
        return true;
    }

}
