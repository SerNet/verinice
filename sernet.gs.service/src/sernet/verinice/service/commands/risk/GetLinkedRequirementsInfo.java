/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
package sernet.verinice.service.commands.risk;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Retrieve risk-related information about the requirements that are linked to a
 * threat
 */
public class GetLinkedRequirementsInfo extends GenericCommand {

    private static final long serialVersionUID = -8535731305850760902L;
    private transient BpThreat threat;
    private transient LinkedRequirementsInfo result;

    public GetLinkedRequirementsInfo(BpThreat threat) {
        this.threat = threat;
    }

    /*
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        Set<CnATreeElement> linkedRequirements = threat.getLinksUp().stream().filter(
                link -> BpRequirement.REL_BP_REQUIREMENT_BP_THREAT.equals(link.getRelationId())
                        && BpRequirement.TYPE_ID.equals(link.getDependant().getTypeId()))
                .map(CnALink::getDependant)
                .filter(r -> r.getEntity().isFlagged(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK))
                .collect(Collectors.toSet());
        if (linkedRequirements.isEmpty()) {
            result = LinkedRequirementsInfo.EMPTY;
        } else {
            Set<String> frequencies = linkedRequirements.stream()
                    .map(item -> item.getEntity()
                            .getRawPropertyValue(BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY))
                    .collect(Collectors.toSet());
            Set<String> impacts = linkedRequirements.stream()
                    .map(item -> item.getEntity()
                            .getRawPropertyValue(BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT))
                    .collect(Collectors.toSet());

            LinkedRequirementsInfo linkedRequirementsInfo = new LinkedRequirementsInfo(frequencies,
                    impacts);
            this.result = linkedRequirementsInfo;
        }

    }

    public LinkedRequirementsInfo getLinkedRequirementsInfo() {
        return result;
    }

    @Override
    public void clear() {
        threat = null;
    }

    public static final class LinkedRequirementsInfo {

        private static final LinkedRequirementsInfo EMPTY = new LinkedRequirementsInfo(
                Collections.emptySet(), Collections.emptySet());

        private final Set<String> frequencies;
        private final Set<String> impacts;

        public LinkedRequirementsInfo(Set<String> frequencies, Set<String> impacts) {
            this.frequencies = Collections.unmodifiableSet(frequencies);
            this.impacts = Collections.unmodifiableSet(impacts);
        }

        public Set<String> getFrequencies() {
            return frequencies;
        }

        public Set<String> getImpacts() {
            return impacts;
        }

    }
}
