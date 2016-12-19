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

import org.eclipse.swt.widgets.Composite;

import sernet.verinice.rcp.linktable.LinkTableUtil;
import sernet.verinice.rcp.linktable.ui.LinkTableColumn;
import sernet.verinice.service.linktable.CnaLinkPropertyConstants;
import sernet.verinice.service.model.HUIObjectModelService;
import sernet.verinice.service.model.IObjectModelService;

/**
 * @see IObjectModelService
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LinkTableRelationPropertyComboViewer
        extends LinkTablePropertyComboViewer {

    public LinkTableRelationPropertyComboViewer(LinkTableComboViewer leftCombo,
            String relatedID, LinkTableOperationType operationType,
            LinkTableColumn ltrParent, Composite parent) {
        super(leftCombo, relatedID, operationType, ltrParent, parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.linktable.ui.combo.
     * LinkTablePropertyComboViewer#getLabelText(java.lang.Object)
     */
    @Override
    protected String getLabelText(Object element) {
        if (element instanceof String) {
            return HUIObjectModelService.getCnaLinkPropertyMessage((String) element);
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

        return CnaLinkPropertyConstants.ALL_PROPERTIES;
    }

}
