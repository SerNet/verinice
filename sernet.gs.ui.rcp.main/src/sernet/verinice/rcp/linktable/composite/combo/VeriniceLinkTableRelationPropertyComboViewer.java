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

import org.eclipse.swt.widgets.Composite;

import sernet.verinice.rcp.linktable.Messages;
import sernet.verinice.rcp.linktable.composite.VeriniceLinkTableColumn;
import sernet.verinice.service.linktable.LinkPropertyElement;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class VeriniceLinkTableRelationPropertyComboViewer
        extends VeriniceLinkTablePropertyComboViewer {

    public VeriniceLinkTableRelationPropertyComboViewer(VeriniceLinkTableComboViewer leftCombo,
            String relatedID, VeriniceLinkTableOperationType operationType,
            VeriniceLinkTableColumn ltrParent, Composite parent) {
        super(leftCombo, relatedID, operationType, ltrParent, parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.composite.combo.
     * VeriniceLinkTablePropertyComboViewer#getLabelText(java.lang.Object)
     */
    @Override
    protected String getLabelText(Object element) {
        if (element instanceof String) {
            return Messages.getString((String) element);
        } else {
            return ltrColumn.getContentService().getLabel(element.toString());
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

        return LinkPropertyElement.getAllProperties().toArray(new String[0]);
    }

}
