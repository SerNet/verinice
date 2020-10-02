/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf
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
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.service.NonNullUtils;

/**
 * ConsoliData holds the info for the CondolidatorCommand.
 * <p>
 * This is used by the {@link ConsolidatorWizard} to hold all relevant info and
 * use it in a {@link ConsolidatorCommand}. That includes the UUIDs of the
 * modules and the property groups which will be consolidated.
 */
public class ConsoliData implements Serializable {

    private static final long serialVersionUID = -6455184214761797773L;
    private final @NonNull String sourceRequirementGroupUuid;
    private final @NonNull Set<String> targetRequirementGroupUuids;
    private final @NonNull Collection<String> selectedPropertyGroups;

    public ConsoliData(@NonNull BpRequirementGroup sourceModule,
            @NonNull Set<String> selectedPropertyGroups, @NonNull Set<String> selectedModuleIDs) {
        this.sourceRequirementGroupUuid = NonNullUtils.toNonNull(sourceModule.getUuid());
        this.selectedPropertyGroups = selectedPropertyGroups;
        this.targetRequirementGroupUuids = selectedModuleIDs;
    }

    public @NonNull String getSourceRequirementGroupUuid() {
        return sourceRequirementGroupUuid;
    }

    public @NonNull Collection<String> getSelectedPropertyGroups() {
        return selectedPropertyGroups;
    }

    public @NonNull Set<String> getTargetRequirementGroupUuids() {
        return targetRequirementGroupUuids;
    }
}
