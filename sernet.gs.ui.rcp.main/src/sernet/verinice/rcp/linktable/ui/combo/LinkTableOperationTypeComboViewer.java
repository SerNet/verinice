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
package sernet.verinice.rcp.linktable.ui.combo;

import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import sernet.verinice.rcp.linktable.ui.LinkTableColumn;
import sernet.verinice.service.model.IObjectModelService;

/**
 * Provides a combo box for {@link ILinkTableOperationType}
 * 
 * @see IObjectModelService
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LinkTableOperationTypeComboViewer extends LinkTableComboViewer {

    private static final Logger LOG = Logger.getLogger(LinkTableOperationTypeComboViewer.class);

    public LinkTableOperationTypeComboViewer(LinkTableComboViewer leftCombo, String relatedID,
            LinkTableOperationType operationType,
            LinkTableColumn ltrParent, Composite parent) {
        this(leftCombo, relatedID, operationType, ltrParent, parent, false);
    }

    public LinkTableOperationTypeComboViewer(LinkTableComboViewer leftCombo, String relatedID,
            LinkTableOperationType operationType,
            LinkTableColumn ltrParent, Composite parent, boolean isCopy) {
        super(leftCombo, relatedID, operationType, ltrParent, parent);

        if (!isCopy) {
            selectFirstElement(true);
        }
        getCombo().setToolTipText(LinkTableOperationType.toolTip());

    }

    @Override
    public LinkTableComboViewer createChild(Composite parent) {

        if (getSelectedElement() == LinkTableOperationType.PROPERTY) {
            if(leftCombo.operationType == LinkTableOperationType.RELATION){
                return new LinkTableRelationPropertyComboViewer(this, relatedID,
                        getSelectedElement(), ltrColumn, parent);
            }
            return new LinkTablePropertyComboViewer(this, relatedID, getSelectedElement(), ltrColumn,
                    parent);
        } else {
            return new LinkTableElementComboViewer(this, relatedID, getSelectedElement(), ltrColumn,
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
        return LinkTableOperationType.values();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.ui.combo.LTRComboViewer#copy()
     */
    @Override
    public LinkTableComboViewer createCopy(LinkTableComboViewer leftCombo, LinkTableColumn ltrParent,
            Composite newParent) {

        return new LinkTableOperationTypeComboViewer(leftCombo, relatedID, getSelectedElement(),
                ltrParent,
                newParent, true);

    }

    public LinkTableOperationType getSelectedElement() {
        StructuredSelection test = (StructuredSelection) this.getSelection();
        return (LinkTableOperationType) test.getFirstElement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.ui.combo.LTRComboViewer#doSelectionChanged()
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
     * @see sernet.verinice.rcp.linktable.ui.combo.LTRComboViewer#getLabelText(java.lang.
     * Object)
     */
    @Override
    protected String getLabelText(Object element) {
        if (element instanceof LinkTableOperationType) {
            return ((LinkTableOperationType) element).getOutput();
        } else {
            return element.toString();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.rcp.linktable.ui.combo.LTRComboViewer#select(java.lang.String)
     */
    @Override
    protected void select(String string) {

        ISelection selection = new StructuredSelection(LinkTableOperationType.getOperationType(string));
        setSelection(selection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.ui.combo.
     * LinkTableComboViewer#doGetAllRelationTypes()
     */
    @Override
    protected Set<String> doGetAllRelationTypes() {
        return Collections.emptySet();
    }

    @Override
    public LinkTableComboViewer copy(LinkTableComboViewer leftCombo, Composite newParent,
            Control formerElement) {

        if (operationType == LinkTableOperationType.RELATION) {

            Composite newParentComposite = new Composite(newParent, SWT.NONE);
            newParentComposite.setLayout(new FormLayout());
            newParentComposite.setLayoutData(getDefaultFormData(formerElement));

            LinkTableComboViewer newViewer = createCopy(leftCombo, ltrColumn,
                    newParentComposite);

            newViewer.getCombo().select(this.getCombo().getSelectionIndex());
            if (rightCombo != null) {

                newViewer.selectionChanged(null);
                newViewer.rightCombo.getCombo()
                        .select(this.rightCombo.getCombo().getSelectionIndex());
                newViewer.rightCombo.selectionChanged(null);
                LinkTableComboViewer relationPropertyViewer = newViewer.rightCombo.rightCombo.rightCombo;
                int selectesRelationProperty = this.rightCombo.rightCombo.rightCombo.getCombo()
                        .getSelectionIndex();
                relationPropertyViewer.getCombo().select(selectesRelationProperty);
            }
            return newViewer;
        } else {
            return super.copy(leftCombo, newParent, formerElement);
        }

    }

}
