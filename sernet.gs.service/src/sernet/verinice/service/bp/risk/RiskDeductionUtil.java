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
package sernet.verinice.service.bp.risk;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

public class RiskDeductionUtil {

    private RiskDeductionUtil() {
    }

    public static BpThreat deduceRisk(BpThreat threat, RiskConfiguration riskConfiguration) {
        String frequency = threat.getFrequencyWithoutAdditionalSafeguards();
        String impact = threat.getImpactWithoutAdditionalSafeguards();

        if (frequency == null || frequency.isEmpty()) {
            threat.setFrequencyWithAdditionalSafeguards(null);
            threat.setRiskWithoutAdditionalSafeguards(null);
            threat.setRiskWithAdditionalSafeguards(null);
            return threat;
        } else if (impact == null || impact.isEmpty()) {
            threat.setImpactWithAdditionalSafeguards(null);
            threat.setRiskWithoutAdditionalSafeguards(null);
            threat.setRiskWithAdditionalSafeguards(null);
            return threat;
        }
        Risk risk = riskConfiguration.getRisk(frequency, impact);
        threat.setRiskWithoutAdditionalSafeguards(risk.getId());

        Set<CnATreeElement> linkedRequirements = getLinkedRequirements(threat);

        String frequencyWithAdditionalSafeguards = getFrequencyWithAdditionalSafeguards(
                Stream.of(frequency), linkedRequirements);

        String impactWithAdditionalSafeguards = getImpactsWithAdditionalSafeguarts(
                Stream.of(impact), linkedRequirements);
        threat.setFrequencyWithAdditionalSafeguards(frequencyWithAdditionalSafeguards);
        threat.setImpactWithAdditionalSafeguards(impactWithAdditionalSafeguards);
        String riskWithAdditionalSafeguards = null;
        if (frequencyWithAdditionalSafeguards != null && impactWithAdditionalSafeguards != null) {
            riskWithAdditionalSafeguards = riskConfiguration
                    .getRisk(frequencyWithAdditionalSafeguards, impactWithAdditionalSafeguards)
                    .getId();
        }
        threat.setRiskWithAdditionalSafeguards(riskWithAdditionalSafeguards);
        return threat;
    }

    private static Set<CnATreeElement> getLinkedRequirements(BpThreat threat) {
        Set<CnATreeElement> linkedRequirements = threat.getLinksUp().stream().filter(
                link -> BpRequirement.REL_BP_REQUIREMENT_BP_THREAT.equals(link.getRelationId())
                        && BpRequirement.TYPE_ID.equals(link.getDependant().getTypeId()))
                .map(CnALink::getDependant).collect(Collectors.toSet());
        return Collections.unmodifiableSet(linkedRequirements);
    }

    private static String getFrequencyWithAdditionalSafeguards(Stream<String> frequenciesWithoutAdditionalSafeguard,
            Set<CnATreeElement> linkedRequirements) {
        Set<String> allFrequencies = Stream
                .concat(frequenciesWithoutAdditionalSafeguard,
                        linkedRequirements.stream()
                                .map(requirement -> requirement.getEntity().getRawPropertyValue(
                                        BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY)))
                .filter(value -> value != null && !value.isEmpty()).collect(Collectors.toSet());
        return allFrequencies.contains(null) ? null : Collections.min(allFrequencies);
    }

    private static String getImpactsWithAdditionalSafeguarts(
            Stream<String> impactsWithoutAdditionalSafeguard,
            Set<CnATreeElement> linkedRequirements) {
        Set<String> allImpacts = Stream
                .concat(impactsWithoutAdditionalSafeguard,
                        linkedRequirements.stream()
                                .map(requirement -> requirement.getEntity().getRawPropertyValue(
                                        BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT)))
                .filter(value -> value != null && !value.isEmpty()).collect(Collectors.toSet());

        return allImpacts.contains(null) ? null : Collections.min(allImpacts);
    }

    public static BpThreat retreiveProperties(BpThreat threat) {
        return (BpThreat) Retriever.retrieveElement(threat,
                new RetrieveInfo()
                .setProperties(true).setLinksUp(true).setLinksUpProperties(true));
    }
}
