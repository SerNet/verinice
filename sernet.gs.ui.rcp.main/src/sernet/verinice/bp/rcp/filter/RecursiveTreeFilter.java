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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A viewer filter that can be used with tree structures. In order for an
 * element to be selected (i.e. shown), the element itself or any descendant
 * must match the delegate filter.
 */
class RecursiveTreeFilter extends ViewerFilter {

    private final ViewerFilter delegateFilter;

    RecursiveTreeFilter(ViewerFilter delegateFilter) {
        this.delegateFilter = delegateFilter;
    }

    /**
     * Override this method to specify whether an element's children are
     * checked. For example, this can be used to abort the checks at a specific
     * level of the tree.
     *
     * @param cnATreeElement
     *            the current element
     * @return whether to check the elements of the given element
     */
    protected boolean checkChildren(CnATreeElement cnATreeElement) {
        return (cnATreeElement instanceof ItNetwork || cnATreeElement instanceof IBpGroup);
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        CnATreeElement cnATreeElement = (CnATreeElement) element;
        if (delegateFilter.select(viewer, parentElement, cnATreeElement)) {
            return true;
        }
        if (checkChildren(cnATreeElement)) {
            return containsMatchingChild(viewer, cnATreeElement);
        }
        return false;
    }

    private boolean containsMatchingChild(Viewer viewer, CnATreeElement cnATreeElement) {
        ITreeContentProvider provider = (ITreeContentProvider) ((StructuredViewer) viewer)
                .getContentProvider();
        for (Object child : provider.getChildren(cnATreeElement)) {
            if (select(viewer, cnATreeElement, child)) {
                return true;
            }
        }
        return false;
    }
}