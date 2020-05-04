/*******************************************************************************
 * Copyright (c) 2020 Jonas Jordan
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
package sernet.verinice.bp.rcp.filter;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Filters by dynamic (SNCA) entity properties. Elements that don't even have
 * that property automatically pass. Implement abstract matches() method with
 * your actual filter logic.
 */
public abstract class AbstractDynamicPropertyFilter extends ViewerFilter {

    private String dynamicPropertyType;

    /**
     * @param dynamicPropertyType
     *            Property type name (without entity type prefix).
     */
    public AbstractDynamicPropertyFilter(@NonNull String dynamicPropertyType) {
        this.dynamicPropertyType = dynamicPropertyType;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof CnATreeElement) {
            CnATreeElement treeElement = (CnATreeElement) element;
            if (treeElement.hasDynamicProperty(dynamicPropertyType)) {
                return matches(treeElement, dynamicPropertyType);
            }
        }
        return true;
    }

    protected abstract boolean matches(CnATreeElement treeElement, String dynamicPropertyType);
}
