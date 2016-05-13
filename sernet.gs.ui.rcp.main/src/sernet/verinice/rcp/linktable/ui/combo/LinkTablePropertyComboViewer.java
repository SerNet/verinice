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

import java.util.*;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.rcp.linktable.ui.LinkTableColumn;
import sernet.verinice.service.model.IObjectModelService;

/**
 * @see IObjectModelService
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LinkTablePropertyComboViewer extends LinkTableComboViewer {

    public LinkTablePropertyComboViewer(LinkTableComboViewer leftCombo, String relatedID,
            LinkTableOperationType operationType,
            LinkTableColumn ltrParent, Composite parent) {
        super(leftCombo, relatedID, operationType, ltrParent, parent);


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

        ArrayList<String> list = new ArrayList<>();
        if (leftCombo.leftCombo.operationType == LinkTableOperationType.RELATION) {

        } else if (relatedID != null && !relatedID.isEmpty()) {
            list.addAll(ltrColumn.getContentService().getPossibleProperties(relatedID));
        }
        return sortElementsByLabel(list.toArray(new String[0]));
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.ui.combo.LTRComboViewer#copy()
     */
    @Override
    public LinkTableComboViewer createCopy(LinkTableComboViewer leftCombo, LinkTableColumn ltrParent,
            Composite newParent) {

        return new LinkTablePropertyComboViewer(leftCombo, relatedID, operationType, ltrParent,
                newParent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.ui.combo.LTRComboViewer#doSelectionChanged()
     */
    @Override
    protected void doSelectionChanged() {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.ui.combo.LTRComboViewer#getLabelText(java.lang.
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
     * @see
     * sernet.verinice.rcp.linktable.ui.combo.LTRComboViewer#select(java.lang.String)
     */
    @Override
    protected void select(String string) {

        ISelection selection = new StructuredSelection(string);
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.rcp.linktable.ui.combo.LinkTableComboViewer#createChild(
     * org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected LinkTableComboViewer createChild(Composite parent) {
        return null;
    }
}
