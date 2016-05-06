/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable.composite.combo;

import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.rcp.linktable.composite.VeriniceLinkTableColumn;

/**
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableOperationTypeComboViewer extends VeriniceLinkTableComboViewer {

    private static final Logger LOG = Logger.getLogger(VeriniceLinkTableOperationTypeComboViewer.class);

    public VeriniceLinkTableOperationTypeComboViewer(VeriniceLinkTableComboViewer leftCombo, String relatedID,
            VeriniceLinkTableOperationType operationType,
            VeriniceLinkTableColumn ltrParent, Composite parent) {
        this(leftCombo, relatedID, operationType, ltrParent, parent, false);
    }

    public VeriniceLinkTableOperationTypeComboViewer(VeriniceLinkTableComboViewer leftCombo, String relatedID,
            VeriniceLinkTableOperationType operationType,
            VeriniceLinkTableColumn ltrParent, Composite parent, boolean isCopy) {
        super(leftCombo, relatedID, operationType, ltrParent, parent);

        if (!isCopy) {
            selectFirstElement(true);
        }
        getCombo().setToolTipText(VeriniceLinkTableOperationType.toolTip());

    }

    @Override
    public VeriniceLinkTableComboViewer createChild(Composite parent) {

        if (getSelectedElement() == VeriniceLinkTableOperationType.PROPERTY) {
            return new VeriniceLinkTablePropertyComboViewer(this, relatedID, getSelectedElement(), ltrColumn,
                    parent);
        } else {
            return new VeriniceLinkTableElementComboViewer(this, relatedID, getSelectedElement(), ltrColumn,
                    parent);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.
     * lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        return VeriniceLinkTableOperationType.values();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.composite.combo.LTRComboViewer#copy()
     */
    @Override
    public VeriniceLinkTableComboViewer createCopy(VeriniceLinkTableComboViewer leftCombo, VeriniceLinkTableColumn ltrParent,
            Composite newParent) {

        return new VeriniceLinkTableOperationTypeComboViewer(leftCombo, relatedID, getSelectedElement(),
                ltrParent,
                newParent, true);

    }

    public VeriniceLinkTableOperationType getSelectedElement() {
        StructuredSelection test = (StructuredSelection) this.getSelection();
        return (VeriniceLinkTableOperationType) test.getFirstElement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.composite.combo.LTRComboViewer#doSelectionChanged()
     */
    @Override
    protected void doSelectionChanged() {

        if (LOG.isDebugEnabled()) {
            if (operationType != null) {
                LOG.debug("operationType changed from " + operationType.getLabel() + " to "
                        + getCurrentSelection());
            } else {
                LOG.debug("operationType is null");
            }
        }
        this.operationType = getSelectedElement();
        if (rightCombo != null) {
            this.rightCombo.dispose();
            this.rightCombo = null;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.composite.combo.LTRComboViewer#getLabelText(java.lang.
     * Object)
     */
    @Override
    protected String getLabelText(Object element) {
        if (element instanceof VeriniceLinkTableOperationType) {
            return ((VeriniceLinkTableOperationType) element).getOutput();
        } else {
            return element.toString();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.rcp.linktable.composite.combo.LTRComboViewer#select(java.lang.String)
     */
    @Override
    protected void select(String string) {

        ISelection selection = new StructuredSelection(VeriniceLinkTableOperationType.getOperationType(string));
        setSelection(selection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.composite.combo.
     * VeriniceLinkTableComboViewer#doGetAllRelationTypes()
     */
    @Override
    protected Set<String> doGetAllRelationTypes() {

        return Collections.emptySet();
    }

}
