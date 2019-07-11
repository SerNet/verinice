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

import java.util.Set;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This class migrates the data of a safeguard. See ModelingMigrationServiceImpl
 * for more details
 */
public class MigrateThreatJob extends MigrateCompendiumElementJob {

    private static final String ELEMENTAL_THREAT_GROUP_NAME = sernet.verinice.service.bp.importer.Messages.Elemental_Threat_Group_Name;

    public MigrateThreatJob(CnATreeElement element, CnATreeElement threat) {
        super(element, threat);
    }

    @Override
    protected void createLink(CnATreeElement threatCopy, CnATreeElement element,
            String linkTypeId) {
        super.createLink(threatCopy, element, linkTypeId);
        createLinksWithRequirements(threatCopy, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
    }

    @Override
    protected CnATreeElement getOrCreateGroup() throws CommandException {
        CnATreeElement group = null;
        Set<CnATreeElement> children = veriniceGraph.getChildren(element);
        for (CnATreeElement child : children) {
            if (ELEMENTAL_THREAT_GROUP_NAME.equals(child.getTitle())) {
                group = child;
            }
        }
        if (group == null) {
            group = copyElement(element, veriniceGraph.getParent(elementCompendium));
        }
        return group;
    }

    protected String getLinkTypeId() {
        return BpThreat.getLinkTypeToTargetObject(element.getTypeId());
    }
}
