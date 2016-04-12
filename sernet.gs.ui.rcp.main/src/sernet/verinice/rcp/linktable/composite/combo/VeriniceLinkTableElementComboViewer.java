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

import java.util.*;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.rcp.linktable.composite.VeriniceLinkTableColumn;
import sernet.verinice.service.linktable.IObjectModelService;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableElementComboViewer extends VeriniceLinkTableComboViewer {

    private static final Logger LOG = Logger.getLogger(VeriniceLinkTableElementComboViewer.class);

    private boolean isDefault;

    public VeriniceLinkTableElementComboViewer(VeriniceLinkTableColumn ltrParent, Composite parent, int style) {
        this(null, null, null, ltrParent, parent, style);

    }

    public VeriniceLinkTableElementComboViewer(VeriniceLinkTableComboViewer leftCombo, String relatedID,
            VeriniceLinkTableOperationType operationType,
            VeriniceLinkTableColumn ltrParent, Composite parent, int style) {
        this(leftCombo, relatedID, operationType, ltrParent, parent, style, false);
    }

    public VeriniceLinkTableElementComboViewer(VeriniceLinkTableComboViewer leftCombo, String relatedID,
            VeriniceLinkTableOperationType operationType,
            VeriniceLinkTableColumn ltrParent, Composite parent, int style, boolean isFirstElement) {
        super(leftCombo, relatedID, operationType, ltrParent, parent, style);

        if (isFirstElement || isDefault) {
            selectFirstElement(!isDefault);
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.
     * eclipse.jface.viewers.SelectionChangedEvent)
     */
    @Override
    public VeriniceLinkTableComboViewer createChild(Composite parent) {

        if (operationType == VeriniceLinkTableOperationType.PROPERTY
                || operationType == VeriniceLinkTableOperationType.RELATION) {
            return null;
        } else {
            return new VeriniceLinkTableOperationTypeComboViewer(this, getCurrentSelection(), operationType,
                    ltrColumn,
                    parent,
                    getCombo().getStyle());
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
        IObjectModelService objectService = ltrColumn.getContentService();
        String[] typeIDs;
        if (leftCombo == null) {
            typeIDs = new ArrayList<>(objectService.getAllTypeIDs()).toArray(new String[0]);
        } else {
            switch (operationType) {
            case CHILD:
                typeIDs = objectService.getPossibleChildren(relatedID)
                        .toArray(new String[0]);
                break;
            case GROUP:
                typeIDs = objectService.getPossibleParents(relatedID)
                        .toArray(new String[0]);
                break;
            case RELATION:
                typeIDs = objectService.getPossibleRelationPartners(relatedID)
                        .toArray(new String[0]);
                break;
            case RELATION_OBJECT:
                typeIDs = objectService.getPossibleRelationPartners(relatedID)
                        .toArray(new String[0]);
                break;
            default:
                throw new IllegalArgumentException("not supported type " + operationType);
            }

            if (typeIDs.length == 0) {
                typeIDs = new String[] { operationType.getDefaultMessage() };
                isDefault = true;
            }

        }
        return sortElementsByLabel(typeIDs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.composite.combo.LTRComboViewer#copy()
     */
    @Override
    public VeriniceLinkTableComboViewer createCopy(VeriniceLinkTableComboViewer leftCombo, VeriniceLinkTableColumn ltrParent,
            Composite newParent,
            int style) {

        return new VeriniceLinkTableElementComboViewer(leftCombo, relatedID, operationType, ltrParent,
                newParent, style, false);
    }


    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.composite.combo.LTRComboViewer#doSelectionChanged()
     */
    @Override
    protected void doSelectionChanged() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selection changed to " + getCurrentSelection());
        }
        if (rightCombo != null) {
            rightCombo.relatedID = getCurrentSelection();
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
        if (element instanceof String) {
            return ltrColumn.getContentService().getLabel((String) element);
        } else {
            return ltrColumn.getContentService().getLabel(element.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.composite.combo.LTRComboViewer#refreshCombo()
     */
    @Override
    protected void refreshCombo() {
        this.getCombo().setEnabled(!isDefault);
        super.refreshCombo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.rcp.linktable.composite.combo.LTRComboViewer#select(java.lang.String)
     */
    @Override
    protected void select(String string) {

        ISelection selection = new StructuredSelection(string);
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

        HashSet<String> relationIDs = new HashSet<>(); 
        if(operationType == VeriniceLinkTableOperationType.RELATION){
            relationIDs.addAll(
                    ltrColumn.getContentService().getRelations(relatedID, getCurrentSelection()));
        }
        return relationIDs;
    }

}
