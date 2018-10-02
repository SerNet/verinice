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

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.crud.LoadCnAElementById;
import sernet.verinice.service.hibernate.HibernateUtil;

public class RiskDeductionUtil {

    private static final Logger logger = Logger.getLogger(RiskDeductionUtil.class);

    private RiskDeductionUtil() {
    }

    /**
     * Update the safeguard strength and risk category of the threat. Uses the
     * VeriniceContext to execute LoadCnAElementById command to find the
     * scope/risk configuration.
     */
    public static BpThreat deduceRisk(BpThreat threat) {
        try {
            ItNetwork scope = loadScopeFromContext(threat.getScopeId());
            return RiskDeductionUtil.deduceRisk(threat, scope.getRiskConfiguration());
        } catch (CommandException e) {
            logger.error("error fetching scope for bp_threat", e);
        }
        return threat;
    }

    public static BpThreat deduceRisk(BpThreat threat, RiskConfiguration riskConfiguration) {
        if (riskConfiguration == null) {
            riskConfiguration = DefaultRiskConfiguration.getInstance();
        }
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

        Set<CnATreeElement> linkedRequirements = getLinkedRequirementsForRiskDeduction(threat);

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

    /**
     * Update the safeguard strength and risk category in linked threats. Uses
     * the VeriniceContext to execute LoadCnAElementById command to find the
     * scope/risk configuration.
     */
    public static void deduceRiskForLinkedThreats(BpRequirement requirement) {
        try {
            ItNetwork scope = loadScopeFromContext(requirement.getScopeId());
            requirement.getLinksDown().stream()
                    .filter(link -> BpRequirement.REL_BP_REQUIREMENT_BP_THREAT
                            .equals(link.getRelationId()))
                    .map(CnALink::getDependency).map(HibernateUtil::unproxy)
                    .map(BpThreat.class::cast).forEach(threat -> RiskDeductionUtil
                            .deduceRisk(threat, scope.getRiskConfiguration()));
        } catch (CommandException e) {
            logger.error("error fetching scope for bp_requirement", e);
        }
    }

    public static CnATreeElement deduceSafeguardStrength(CnATreeElement requirement) {
        if (!requirement.getEntity().isFlagged(BpRequirement.PROP_IMPLEMENTATION_DEDUCE)) {
            return requirement;
        }
        String impactStrength = getLinkedSafeguards(requirement)
                .filter(s -> s.getEntity().isFlagged(Safeguard.PROP_REDUCE_RISK))
                .map(s -> s.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_IMPACT))
                .filter(s -> s != null && !s.isEmpty()).min(String::compareTo).orElse(null);

        String frequencyStrength = getLinkedSafeguards(requirement)
                .filter(s -> s.getEntity().isFlagged(Safeguard.PROP_REDUCE_RISK))
                .map(s -> s.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_FREQUENCY))
                .filter(s -> s != null && !s.isEmpty()).min(String::compareTo).orElse(null);

        requirement.getEntity().setPropertyValue(BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT,
                impactStrength);
        requirement.getEntity().setPropertyValue(BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY,
                frequencyStrength);
        return requirement;
    }

    public static BpThreat retreiveProperties(BpThreat threat) {
        return (BpThreat) Retriever.retrieveElement(threat,
                new RetrieveInfo().setProperties(true).setLinksUp(true).setLinksUpProperties(true));
    }

    public static BpRequirement retreiveProperties(BpRequirement requirement) {
        return (BpRequirement) Retriever.retrieveElement(requirement, new RetrieveInfo()
                .setProperties(true).setLinksDown(true).setLinksDownProperties(true));
    }

    private static Set<CnATreeElement> getLinkedRequirementsForRiskDeduction(BpThreat threat) {
        Set<CnATreeElement> linkedRequirements = threat.getLinksUp().stream().filter(
                link -> BpRequirement.REL_BP_REQUIREMENT_BP_THREAT.equals(link.getRelationId())
                        && BpRequirement.TYPE_ID.equals(link.getDependant().getTypeId()))
                .map(CnALink::getDependant)
                .filter(r -> r.getEntity().isFlagged(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK))
                .collect(Collectors.toSet());
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

    private static Stream<CnATreeElement> getLinkedSafeguards(CnATreeElement requirement) {
        return requirement.getLinksDown().stream().filter(
                link -> BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(link.getRelationId()))
                .map(CnALink::getDependency);
    }

    private static ItNetwork loadScopeFromContext(Integer scopeId) throws CommandException {
        ICommandService cs = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
        LoadCnAElementById loadModel = new LoadCnAElementById(ItNetwork.TYPE_ID, scopeId);
        loadModel = cs.executeCommand(loadModel);
        return (ItNetwork) loadModel.getFound();
    }
}
