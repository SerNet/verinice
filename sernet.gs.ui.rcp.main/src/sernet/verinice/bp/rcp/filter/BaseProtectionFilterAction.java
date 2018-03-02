/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementFilter;
import sernet.verinice.model.iso27k.Group;

/**
 * This action shows the base protection filter dialog
 */
public class BaseProtectionFilterAction extends Action {
    private StructuredViewer viewer;

    private Set<Qualifier> selectedQualifiers = EnumSet.noneOf(Qualifier.class);
    private Set<ImplementationStatus> selectedImplementationStatus = EnumSet
            .noneOf(ImplementationStatus.class);
    private Set<String> selectedElementTypes = new HashSet<>();
    private Set<String> selectedTags = new HashSet<>();
    private boolean applyTagFilterToItNetworks;
    private boolean hideEmptyGroups;

    public BaseProtectionFilterAction(StructuredViewer viewer) {
        super("Filter..."); // //$NON-NLS-1$
        this.viewer = viewer;

        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
    }

    @Override
    public void run() {
        BaseProtectionFilterDialog dialog = new BaseProtectionFilterDialog(
                Display.getCurrent().getActiveShell(), selectedImplementationStatus,
                selectedQualifiers, selectedElementTypes, selectedTags, applyTagFilterToItNetworks,
                hideEmptyGroups);
        if (dialog.open() != InputDialog.OK) {
            return;
        }

        selectedImplementationStatus = dialog.getSelectedImplementationStatus();
        selectedQualifiers = dialog.getSelectedQualifiers();
        selectedElementTypes = dialog.getSelectedElementTypes();
        selectedTags = dialog.getSelectedTags();
        applyTagFilterToItNetworks = dialog.isApplyTagFilterToItNetworks();
        hideEmptyGroups = dialog.isHideEmptyGroups();

        List<ViewerFilter> viewerFilters = new LinkedList<>();
        addImplementationStateFilter(viewerFilters);
        addQualifierFilter(viewerFilters);
        addTypeFilter(viewerFilters);
        addTagFilter(viewerFilters);
        addHideEmptyGroupsFilter(viewerFilters);
        ViewerFilter[] filtersAsArray = viewerFilters
                .toArray(new ViewerFilter[viewerFilters.size()]);
        viewer.setFilters(filtersAsArray);
        if (filtersAsArray.length > 0) {
            setImageDescriptor(
                    ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER_ACTIVE));
        } else {
            setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
        }
    }

    private void addImplementationStateFilter(List<ViewerFilter> viewerFilters) {
        if (!selectedImplementationStatus.isEmpty()) {
            viewerFilters.add(new RecursiveTreeFilter(new ImplementationStatusFilter(
                    Collections.unmodifiableSet(selectedImplementationStatus), hideEmptyGroups)));
        }
    }

    private void addQualifierFilter(List<ViewerFilter> viewerFilters) {
        if (!selectedQualifiers.isEmpty()) {
            viewerFilters.add(new RecursiveTreeFilter(new QualifierFilter(
                    Collections.unmodifiableSet(selectedQualifiers), hideEmptyGroups)));
        }
    }

    private void addTypeFilter(List<ViewerFilter> viewerFilters) {
        if (!selectedElementTypes.isEmpty()) {
            viewerFilters.add(new RecursiveTreeFilter(new TypeFilter(
                    Collections.unmodifiableSet(selectedElementTypes), hideEmptyGroups)));
        }
    }

    private void addTagFilter(List<ViewerFilter> viewerFilters) {
        if (!selectedTags.isEmpty()) {
            viewerFilters.add(
                    new RecursiveTreeFilter(new TagFilter(Collections.unmodifiableSet(selectedTags),
                            applyTagFilterToItNetworks, hideEmptyGroups)) {

                        @Override
                        protected boolean checkChildren(CnATreeElement cnATreeElement) {
                            if (applyTagFilterToItNetworks && cnATreeElement instanceof ItNetwork) {
                                return false;
                            }
                            return super.checkChildren(cnATreeElement);
                        }
                    });
        }
    }

    private void addHideEmptyGroupsFilter(List<ViewerFilter> viewerFilters) {
        if (hideEmptyGroups) {
            viewerFilters.add(new RecursiveTreeFilter(HideEmptyGroupsFilter.INSTANCE));
        }
    }

    private static final class ImplementationStatusFilter extends ViewerFilter {
        private final Collection<ImplementationStatus> selectedImplementationStatus;
        private final boolean hideEmptyGroups;

        ImplementationStatusFilter(Set<ImplementationStatus> selectedImplementationStatus,
                boolean hideEmptyGroups) {
            this.selectedImplementationStatus = selectedImplementationStatus;
            this.hideEmptyGroups = hideEmptyGroups;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (!hideEmptyGroups && element instanceof Group || element instanceof ItNetwork) {
                return true;
            }
            ImplementationStatus implementationStatus = ImplementationStatus
                    .findValue((CnATreeElement) element);
            return selectedImplementationStatus.contains(implementationStatus);
        }
    }

    private static final class TagFilter extends ViewerFilter {
        private final Set<String> selectedTags;
        private final boolean applyTagFilterToItNetworks;
        private final boolean hideEmptyGroups;

        TagFilter(Set<String> selectedTags, boolean applyTagFilterToItNetworks,
                boolean hideEmptyGroups) {
            this.selectedTags = selectedTags;
            this.applyTagFilterToItNetworks = applyTagFilterToItNetworks;
            this.hideEmptyGroups = hideEmptyGroups;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (!hideEmptyGroups && element instanceof Group) {
                return true;
            }
            if (!(element instanceof ITaggableElement)) {
                return false;
            }
            if ((element instanceof ItNetwork || element instanceof ImportBpGroup)
                    && !applyTagFilterToItNetworks) {
                return true;
            }
            ITaggableElement taggableElement = (ITaggableElement) element;
            Collection<String> elementTags = taggableElement.getTags();
            boolean elementHasNoTags = elementTags.isEmpty();
            if (elementHasNoTags) {
                return selectedTags.contains(ElementFilter.NO_TAG);
            }
            for (String selectedTag : selectedTags) {
                if (elementTags.contains(selectedTag)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class TypeFilter extends ViewerFilter {
        private final Collection<String> selectedElementTypes;
        private final boolean hideEmptyGroups;

        TypeFilter(Collection<String> selectedElementTypes, boolean hideEmptyGroups) {
            this.selectedElementTypes = selectedElementTypes;
            this.hideEmptyGroups = hideEmptyGroups;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (!hideEmptyGroups && element instanceof Group || element instanceof ItNetwork) {
                return true;
            }
            CnATreeElement cnATreeElement = (CnATreeElement) element;
            return selectedElementTypes.contains(cnATreeElement.getTypeId());
        }
    }

    private static final class QualifierFilter extends ViewerFilter {
        private final Collection<Qualifier> selectedQualifiers;
        private final boolean hideEmptyGroups;

        QualifierFilter(Collection<Qualifier> selectedQualifiers, boolean hideEmptyGroups) {
            this.selectedQualifiers = selectedQualifiers;
            this.hideEmptyGroups = hideEmptyGroups;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (!hideEmptyGroups && element instanceof Group || element instanceof ItNetwork) {
                return true;
            }
            Qualifier qualifier = Qualifier.findValue((CnATreeElement) element);
            return qualifier != null && selectedQualifiers.contains(qualifier);
        }
    }

    private static final class HideEmptyGroupsFilter extends ViewerFilter {

        private static final HideEmptyGroupsFilter INSTANCE = new HideEmptyGroupsFilter();

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            return !(element instanceof Group);
        }
    }

    /**
     * A viewer filter that can be used with tree structures. In order for an
     * element to be selected (i.e. shown), the element itself or any descendant
     * must match the delegate filter.
     */
    private static class RecursiveTreeFilter extends ViewerFilter {

        private final ViewerFilter delegateFilter;

        RecursiveTreeFilter(ViewerFilter delegateFilter) {
            this.delegateFilter = delegateFilter;
        }

        /**
         * Override this method to specify whether an element's children are
         * checked. For example, this can be used to abort the checks at a
         * specific level of the tree.
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
}