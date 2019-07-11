/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
package sernet.verinice.service.bp.migration;

import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This class migrates the data of a requirement. See
 * ModelingMigrationServiceImpl for more details
 */
public class MigrateRequirementJob extends MigrateCompendiumElementJob {

    public MigrateRequirementJob(CnATreeElement element, CnATreeElement requirement) {
        super(element, requirement);
    }

    @Override
    protected String getIdentifier(CnATreeElement element) {
        if (element instanceof BpRequirementGroup) {
            return ((BpRequirementGroup) element).getIdentifier();
        }
        if (element instanceof BpRequirement) {
            return ((BpRequirement) element).getIdentifier();
        }
        return null;
    }

    protected String getLinkTypeId() {
        return BpRequirement.getLinkTypeToTargetObject(element.getTypeId());
    }
}
