/*******************************************************************************
 * Copyright (c) 2018 Alexander Ben Nasrallah.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.ISecurityLevelProvider;
import sernet.verinice.model.bp.ImplementationStatus;
import sernet.verinice.model.bp.Proceeding;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementFilter;
import sernet.verinice.model.iso27k.Group;

/**
 * This builder generates a null-free collection of ViewerFilter based on
 * BaseProtectionFilterParameters, which can be applied to CnATreeElements.
 */
public class BaseProtectionFilterBuilder {

    private BaseProtectionFilterBuilder() {
    }

    public static @NonNull Collection<ViewerFilter> makeFilters(
            BaseProtectionFilterParameters params) {
        Collection<ViewerFilter> viewerFilters = new ArrayList<>(7);
        Optional.ofNullable(createImplementationStateFilter(params)).ifPresent(viewerFilters::add);
        Optional.ofNullable(createSecurityLevelFilter(params)).ifPresent(viewerFilters::add);
        Optional.ofNullable(createTypeFilter(params)).ifPresent(viewerFilters::add);
        Optional.ofNullable(createTagFilter(params)).ifPresent(viewerFilters::add);
        Optional.ofNullable(createHideEmptyGroupsFilter(params)).ifPresent(viewerFilters::add);
        viewerFilters.add(createProceedingFilter());
        return viewerFilters;
    }

    private static ViewerFilter createImplementationStateFilter(
            BaseProtectionFilterParameters filterParameters) {
        if (!filterParameters.getImplementationStatuses().isEmpty()) {
            return new RecursiveTreeFilter(
                    new ImplementationStatusFilter(filterParameters.getImplementationStatuses(),
                            filterParameters.isHideEmptyGroups()));
        }
        return null;
    }

    private static ViewerFilter createSecurityLevelFilter(
            BaseProtectionFilterParameters filterParameters) {
        if (!filterParameters.getSecurityLevels().isEmpty()) {
            return new RecursiveTreeFilter(new SecurityLevelFilter(
                    filterParameters.getSecurityLevels(), filterParameters.isHideEmptyGroups()));
        }
        return null;
    }

    private static ViewerFilter createTypeFilter(BaseProtectionFilterParameters filterParameters) {
        if (!filterParameters.getElementTypes().isEmpty()) {
            return new RecursiveTreeFilter(new TypeFilter(filterParameters.getElementTypes(),
                    filterParameters.isHideEmptyGroups()));
        }
        return null;
    }

    private static ViewerFilter createProceedingFilter() {
        return new ProceedingFilter();
    }

    private static ViewerFilter createTagFilter(BaseProtectionFilterParameters filterParameters) {
        if (!filterParameters.getTags().isEmpty()) {
            return new RecursiveTreeFilter(new TagFilter(filterParameters.getTags(),
                    filterParameters.isApplyTagFilterToItNetworks(),
                    filterParameters.isHideEmptyGroups())) {

                @Override
                protected boolean checkChildren(CnATreeElement cnATreeElement) {
                    if (filterParameters.isApplyTagFilterToItNetworks()
                            && cnATreeElement instanceof ItNetwork) {
                        return false;
                    }
                    return super.checkChildren(cnATreeElement);
                }
            };
        }
        return null;
    }

    private static ViewerFilter createHideEmptyGroupsFilter(
            BaseProtectionFilterParameters filterParameters) {
        if (filterParameters.isHideEmptyGroups()) {
            return new RecursiveTreeFilter(HideEmptyGroupsFilter.INSTANCE);
        }
        return null;
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
            if (hideEmptyGroups && (element instanceof BpRequirementGroup
                    || element instanceof SafeguardGroup)) {
                return false;
            }
            if (element instanceof BpRequirement) {
                return selectedImplementationStatus
                        .contains(((BpRequirement) element).getImplementationStatus());
            }
            if (element instanceof Safeguard) {
                return selectedImplementationStatus
                        .contains(((Safeguard) element).getImplementationStatus());
            }
            return false;
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

    private static final class SecurityLevelFilter extends ViewerFilter {
        private final Collection<SecurityLevel> selectedSecurityLevels;
        private final boolean hideEmptyGroups;

        SecurityLevelFilter(Collection<SecurityLevel> selectedSecurityLevels,
                boolean hideEmptyGroups) {
            this.selectedSecurityLevels = selectedSecurityLevels;
            this.hideEmptyGroups = hideEmptyGroups;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (!hideEmptyGroups && element instanceof Group || element instanceof ItNetwork) {
                return true;
            }
            if (hideEmptyGroups && (element instanceof BpRequirementGroup
                    || element instanceof SafeguardGroup)) {
                return false;
            }
            if (element instanceof Safeguard) {
                return selectedSecurityLevels.contains(((Safeguard) element).getSecurityLevel());
            }
            if (element instanceof BpRequirement) {
                return selectedSecurityLevels
                        .contains(((BpRequirement) element).getSecurityLevel());
            }
            return false;
        }
    }

    private static final class HideEmptyGroupsFilter extends ViewerFilter {

        private static final HideEmptyGroupsFilter INSTANCE = new HideEmptyGroupsFilter();

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            return !(element instanceof Group);
        }
    }

    private static final class ProceedingFilter extends ViewerFilter {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            boolean filterByProceeding = Activator.getDefault().getPreferenceStore()
                    .getBoolean(PreferenceConstants.FILTER_INFORMATION_NETWORKS_BY_PROCEEDING);
            if (filterByProceeding && element instanceof CnATreeElement) {
                if (element instanceof BpThreat) {
                    return ThreatByProceedingFilterUtil
                            .showThreatWhenProceedingFilterIsEnabled((BpThreat) element);
                } else if (element instanceof ISecurityLevelProvider) {
                    SecurityLevel securityLevel = ((ISecurityLevelProvider) element)
                            .getSecurityLevel();
                    return scopeRequiresSecurityLevel(((CnATreeElement) element), securityLevel);
                }
            }
            return true;
        }

        private boolean scopeRequiresSecurityLevel(CnATreeElement element,
                SecurityLevel securityLevel) {
            CnATreeElement scope = CnATreeElementScopeUtils.getScope(element);
            if (scope instanceof ItNetwork) {
                ItNetwork itNetwork = (ItNetwork) scope;
                Proceeding proceeding = itNetwork.getProceeding();
                if (proceeding == null) {
                    return true; // undefined state
                }
                return proceeding.requires(securityLevel);
            }
            // scope is no it network. This state is
            // undefined.
            return true;
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
