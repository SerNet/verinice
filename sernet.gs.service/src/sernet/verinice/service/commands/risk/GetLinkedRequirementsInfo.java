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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

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

    private static final long serialVersionUID = 8217062845243989223L;
    private transient BpThreat threat;
    private LinkedRequirementsInfo result;
    private final Integer threatId;

    public GetLinkedRequirementsInfo(BpThreat threat) {
        this.threat = threat;
        this.threatId = threat.getDbId();
    }

    /*
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if (threat == null) {
            threat = findThreatById(threatId);
        }
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
                    .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
            Set<String> impacts = linkedRequirements.stream()
                    .map(item -> item.getEntity()
                            .getRawPropertyValue(BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT))
                    .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());

            LinkedRequirementsInfo linkedRequirementsInfo = new LinkedRequirementsInfo(frequencies,
                    impacts);
            this.result = linkedRequirementsInfo;
        }
    }

    public LinkedRequirementsInfo getLinkedRequirementsInfo() {
        return result;
    }

    private BpThreat findThreatById(Integer dbId) {
        return getDaoFactory().getDAO(BpThreat.class).findById(dbId);
    }

    @Override
    public void clear() {
        threat = null;
    }

    public static final class LinkedRequirementsInfo implements Serializable {

        private static final long serialVersionUID = -1551552188936477628L;

        public static final LinkedRequirementsInfo EMPTY = new LinkedRequirementsInfo(
                Collections.emptySet(), Collections.emptySet());

        private final HashSet<String> frequencies;
        private final HashSet<String> impacts;

        public LinkedRequirementsInfo(Set<String> frequencies, Set<String> impacts) {
            this.frequencies = new HashSet<>(frequencies);
            this.impacts = new HashSet<>(impacts);
        }

        public Set<String> getFrequencies() {
            return Collections.unmodifiableSet(frequencies);
        }

        public Set<String> getImpacts() {
            return Collections.unmodifiableSet(impacts);
        }

    }
}
