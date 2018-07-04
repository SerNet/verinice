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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import sernet.gs.service.CollectionUtil;
import sernet.verinice.model.bp.SecurityLevel;

public class BaseProtectionFilterParameters {
    private @NonNull Set<ImplementationStatus> implementationStatuses;
    private @NonNull Set<SecurityLevel> securityLevels;
    private @NonNull Set<String> elementTypes;
    private @NonNull Set<String> tags;
    private boolean applyTagFilterToItNetworks;
    private boolean hideEmptyGroups;

    private BaseProtectionFilterParameters() {
        implementationStatuses = new HashSet<>();
        securityLevels = new HashSet<>();
        elementTypes = new HashSet<>();
        tags = new HashSet<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public @NonNull Set<ImplementationStatus> getImplementationStatuses() {
        return implementationStatuses;
    }

    public @NonNull Set<SecurityLevel> getSecurityLevels() {
        return securityLevels;
    }

    public @NonNull Set<String> getElementTypes() {
        return elementTypes;
    }

    public @NonNull Set<String> getTags() {
        return tags;
    }

    public boolean isApplyTagFilterToItNetworks() {
        return applyTagFilterToItNetworks;
    }

    public boolean isHideEmptyGroups() {
        return hideEmptyGroups;
    }

    public static class Builder {
        private @NonNull BaseProtectionFilterParameters parameters;

        private Builder() {
            parameters = new BaseProtectionFilterParameters();
        }

        public @NonNull BaseProtectionFilterParameters build() {
            return parameters;
        }

        public Builder withHideEmptyGroups(boolean hideEmptyGroups) {
            parameters.hideEmptyGroups = hideEmptyGroups;
            return this;
        }

        public Builder withApplyTagFilterToItNetworks(boolean applyTagFilterToItNetworks) {
            parameters.applyTagFilterToItNetworks = applyTagFilterToItNetworks;
            return this;
        }

        public Builder withTags(@NonNull Set<String> selectedTags) {
            parameters.tags = CollectionUtil.unmodifiableSet(selectedTags);
            return this;
        }

        public Builder withElementTypes(@NonNull Set<String> selectedElementTypes) {
            parameters.elementTypes = CollectionUtil.unmodifiableSet(selectedElementTypes);
            return this;
        }

        public Builder withSecurityLevels(@NonNull Set<SecurityLevel> selectedSecurityLevels) {
            parameters.securityLevels = CollectionUtil.unmodifiableSet(selectedSecurityLevels);
            return this;
        }

        public Builder withImplementationStatuses(
                @NonNull Set<ImplementationStatus> selectedImplementationStatus) {
            parameters.implementationStatuses = CollectionUtil
                    .unmodifiableSet(selectedImplementationStatus);
            return this;
        }
    }
}