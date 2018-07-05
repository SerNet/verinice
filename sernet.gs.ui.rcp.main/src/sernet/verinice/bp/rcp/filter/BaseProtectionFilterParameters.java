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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (applyTagFilterToItNetworks ? 1231 : 1237);
        result = prime * result + ((elementTypes == null) ? 0 : elementTypes.hashCode());
        result = prime * result + (hideEmptyGroups ? 1231 : 1237);
        result = prime * result
                + ((implementationStatuses == null) ? 0 : implementationStatuses.hashCode());
        result = prime * result + ((securityLevels == null) ? 0 : securityLevels.hashCode());
        result = prime * result + ((tags == null) ? 0 : tags.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseProtectionFilterParameters other = (BaseProtectionFilterParameters) obj;
        if (applyTagFilterToItNetworks != other.applyTagFilterToItNetworks)
            return false;
        if (!elementTypes.equals(other.elementTypes))
            return false;
        if (hideEmptyGroups != other.hideEmptyGroups)
            return false;
        if (!implementationStatuses.equals(other.implementationStatuses))
            return false;
        if (!securityLevels.equals(other.securityLevels))
            return false;
        if (!tags.equals(other.tags))
            return false;
        return true;
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