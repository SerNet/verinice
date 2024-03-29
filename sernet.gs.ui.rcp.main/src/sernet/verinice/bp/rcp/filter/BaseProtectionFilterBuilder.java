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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.model.bp.ChangeType;
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
            BaseProtectionFilterParameters params, IPreferenceStore prererenceStore) {
        return Stream.of(createRequirementSafeguardFilter(params),
                createAuditPerformedFilter(params), createChangeTypeFilter(params),
                createReleaseFilter(params), createRiskAnalysisNecessaryFilter(params),
                createRiskLabelFilter(params), createTypeFilter(params), createTagFilter(params),
                createHideEmptyGroupsFilter(params), createProceedingFilter(prererenceStore))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static ViewerFilter createAuditPerformedFilter(BaseProtectionFilterParameters params) {
        return params.getAuditPerformed()
                .map(v -> new DynamicBooleanPropertyFilter("audit_performed", v)).orElse(null);
    }

    private static ViewerFilter createChangeTypeFilter(BaseProtectionFilterParameters params) {
        if (!params.getChangeTypes().isEmpty()) {
            return new RecursiveTreeFilter(new DynamicEnumPropertyFilter<>("change_type",
                    ChangeType.class, params.getChangeTypes()), 1);
        }
        return null;
    }

    private static ViewerFilter createReleaseFilter(BaseProtectionFilterParameters params) {
        if (!params.getReleases().isEmpty()) {
            return new RecursiveTreeFilter(
                    new DynamicStringPropertyFilter("release", params.getReleases()), 1);
        }
        return null;
    }

    private static ViewerFilter createRiskAnalysisNecessaryFilter(
            BaseProtectionFilterParameters params) {
        return params.getRiskAnalysisNecessary()
                .map(v -> new DynamicBooleanPropertyFilter("riskanalysis_necessary", v))
                .orElse(null);
    }

    public static @NonNull Collection<ViewerFilter> makeFilters(
            BaseProtectionFilterParameters params) {
        return makeFilters(params, Activator.getDefault().getPreferenceStore());
    }

    private static ViewerFilter createRiskLabelFilter(BaseProtectionFilterParameters params) {
        if (!params.getRiskLabels().isEmpty()) {
            return new RiskLabelFilter(params.getRiskLabels());
        }
        return null;
    }

    private static ViewerFilter createRequirementSafeguardFilter(
            BaseProtectionFilterParameters filterParameters) {
        if (!filterParameters.getImplementationStatuses().isEmpty()
                || !filterParameters.getSecurityLevels().isEmpty()) {
            return new RecursiveTreeFilter(new RequirementSafeguardFilter(
                    filterParameters.getImplementationStatuses(),
                    filterParameters.getSecurityLevels(), filterParameters.isHideEmptyGroups(),
                    filterParameters.getElementTypes()));
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

    private static ViewerFilter createProceedingFilter(IPreferenceStore preferenceStore) {
        return new ProceedingFilter(preferenceStore);
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

    private static final class RequirementSafeguardFilter extends ViewerFilter {
        private final Collection<ImplementationStatus> selectedImplementationStatus;
        private final Collection<SecurityLevel> selectedSecurityLevels;
        private final Collection<String> selectedElementTypes;
        private final boolean hideEmptyGroups;

        RequirementSafeguardFilter(Collection<ImplementationStatus> selectedImplementationStatus,
                Collection<SecurityLevel> selectedSecurityLevels, boolean hideEmptyGroups,
                Collection<String> selectedElementTypes) {
            this.selectedImplementationStatus = selectedImplementationStatus;
            this.selectedSecurityLevels = selectedSecurityLevels;
            this.hideEmptyGroups = hideEmptyGroups;
            this.selectedElementTypes = selectedElementTypes;
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
            if (!emptyOrContains(selectedElementTypes, ((CnATreeElement) element).getTypeId())) {
                return false;
            }
            if (element instanceof BpRequirement) {
                return (emptyOrContains(selectedImplementationStatus,
                        ((BpRequirement) element).getImplementationStatus())
                        && emptyOrContains(selectedSecurityLevels,
                                ((BpRequirement) element).getSecurityLevel()));
            }
            if (element instanceof Safeguard) {
                return (emptyOrContains(selectedImplementationStatus,
                        ((Safeguard) element).getImplementationStatus())
                        && emptyOrContains(selectedSecurityLevels,
                                ((Safeguard) element).getSecurityLevel()));
            }
            return true;
        }

        private static <T> boolean emptyOrContains(Collection<T> collection, T value) {
            return collection.isEmpty() || collection.contains(value);
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

        private final IPreferenceStore preferenceStore;

        public ProceedingFilter(IPreferenceStore preferenceStore) {
            this.preferenceStore = preferenceStore;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            boolean filterByProceeding = preferenceStore
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
}
