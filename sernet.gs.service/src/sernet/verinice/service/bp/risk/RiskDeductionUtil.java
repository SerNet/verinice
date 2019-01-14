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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.risk.GetLinkedRequirementsInfo;
import sernet.verinice.service.commands.risk.GetLinkedRequirementsInfo.LinkedRequirementsInfo;
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

        boolean frequencyIsUnset = resetRiskValuesIfFrequencyIsUnset(threat);
        boolean impactIsUnset = resetRiskValuesIfImpactIsUnset(threat);

        if (frequencyIsUnset || impactIsUnset) {
            return threat;
        }

        try {
            return RiskDeductionUtil.deduceRisk(threat, findRiskConfiguration(threat.getScopeId()));
        } catch (CommandException e) {
            logger.error("error fetching scope for bp_threat", e);
        }
        return threat;
    }

    private static BpThreat deduceRisk(BpThreat threat, RiskConfiguration riskConfiguration)
            throws CommandException {
        boolean frequencyIsUnset = resetRiskValuesIfFrequencyIsUnset(threat);
        boolean impactIsUnset = resetRiskValuesIfImpactIsUnset(threat);

        if (frequencyIsUnset || impactIsUnset) {
            return threat;
        }
        String frequency = threat.getFrequencyWithoutAdditionalSafeguards();
        String impact = threat.getImpactWithoutAdditionalSafeguards();

        if (riskConfiguration == null) {
            riskConfiguration = DefaultRiskConfiguration.getInstance();
        }

        Risk risk = riskConfiguration.getRisk(frequency, impact);
        setPropertyIfNecessary(threat, BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS,
                Optional.ofNullable(risk).map(Risk::getId).orElse(null));

        LinkedRequirementsInfo linkedRequirementsInfo = getLinkedRequirementsForRiskDeduction(
                threat);

        String frequencyWithAdditionalSafeguards = getFrequencyWithAdditionalSafeguards(
                Stream.of(frequency), linkedRequirementsInfo.getFrequencies());
        if (nullOrEmpty(frequencyWithAdditionalSafeguards)) {
            threat.setFrequencyWithAdditionalSafeguards(frequency);
            threat.setImpactWithAdditionalSafeguards(impact);
            threat.setRiskWithAdditionalSafeguards(
                    riskConfiguration.getRisk(frequency, impact).getId());
            return threat;
        }

        String impactWithAdditionalSafeguards = getImpactsWithAdditionalSafeguards(
                Stream.of(impact), linkedRequirementsInfo.getImpacts());
        if (nullOrEmpty(impactWithAdditionalSafeguards)) {
            threat.setFrequencyWithAdditionalSafeguards(frequency);
            threat.setImpactWithAdditionalSafeguards(impact);
            threat.setRiskWithAdditionalSafeguards(
                    riskConfiguration.getRisk(frequency, impact).getId());
            return threat;
        }

        threat.setFrequencyWithAdditionalSafeguards(frequencyWithAdditionalSafeguards);
        threat.setImpactWithAdditionalSafeguards(impactWithAdditionalSafeguards);
        Risk riskWithAdditionalSafeguards = riskConfiguration
                .getRisk(frequencyWithAdditionalSafeguards, impactWithAdditionalSafeguards);
        setPropertyIfNecessary(threat, BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS,
                Optional.ofNullable(riskWithAdditionalSafeguards).map(Risk::getId).orElse(null));
        return threat;
    }

    /**
     * Update the safeguard strength and risk category in linked threats. Uses
     * the VeriniceContext to execute LoadCnAElementById command to find the
     * scope/risk configuration.
     */
    public static void deduceRiskForLinkedThreats(BpRequirement requirement) {
        try {
            Set<@NonNull BpThreat> affectedThreats = requirement.getLinksDown().stream()
                    .filter(link -> BpRequirement.REL_BP_REQUIREMENT_BP_THREAT
                            .equals(link.getRelationId()))
                    .map(CnALink::getDependency).map(HibernateUtil::unproxy)
                    .map(BpThreat.class::cast).collect(Collectors.toSet());
            if (!affectedThreats.isEmpty()) {
                RiskConfiguration riskConfiguration = findRiskConfiguration(
                        requirement.getScopeId());
                for (BpThreat threat : affectedThreats) {
                    RiskDeductionUtil.deduceRisk(threat, riskConfiguration);
                }
            }
        } catch (CommandException e) {
            logger.error("error fetching scope for bp_requirement", e);
        }
    }

    public static CnATreeElement deduceSafeguardStrength(CnATreeElement requirement) {
        if (!requirement.getEntity().isFlagged(BpRequirement.PROP_IMPLEMENTATION_DEDUCE)) {
            return requirement;
        }

        String impactStrength = getLinkedSafeguards(requirement)
                .filter(s -> s.getEntity().isFlagged(Safeguard.PROP_REDUCE_RISK)
                        && isSafeGuardStrenghtBothSet(s))
                .map(s -> s.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_IMPACT))
                .filter(RiskDeductionUtil::notNullAndNotEmpty).min(String::compareTo).orElse(null);

        String frequencyStrength = getLinkedSafeguards(requirement)
                .filter(s -> s.getEntity().isFlagged(Safeguard.PROP_REDUCE_RISK)
                        && isSafeGuardStrenghtBothSet(s))
                .map(s -> s.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_FREQUENCY))
                .filter(RiskDeductionUtil::notNullAndNotEmpty).min(String::compareTo).orElse(null);
        setPropertyIfNecessary(requirement, BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT,
                impactStrength);
        setPropertyIfNecessary(requirement, BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY,
                frequencyStrength);
        return requirement;
    }

    /**
     * Set a property in an entity with a special handling for null values to
     * avoid creating unnecessary {@link sernet.hui.common.connect.PropertyList}
     * instances.
     */
    private static void setPropertyIfNecessary(CnATreeElement element, String propertyId,
            String propertyValue) {

        if (propertyValue == null
                && !element.getEntity().getTypedPropertyLists().containsKey(propertyId)) {
            // if the value is null and there is no existing property list for
            // the property, don't change anything
            return;
        }

        // now, either the property value is not null, so we would have to
        // create a PropertyList if it does not exist, or it is null AND the
        // element already has a PropertyList for the property ID
        element.setSimpleProperty(propertyId, propertyValue);
    }

    private static boolean resetRiskValuesIfImpactIsUnset(BpThreat threat) {
        if (nullOrEmpty(threat.getImpactWithoutAdditionalSafeguards())) {
            threat.setImpactWithAdditionalSafeguards(null);
            threat.setRiskWithoutAdditionalSafeguards(null);
            threat.setRiskWithAdditionalSafeguards(null);
            return true;
        }
        return false;
    }

    private static boolean resetRiskValuesIfFrequencyIsUnset(BpThreat threat) {
        if (nullOrEmpty(threat.getFrequencyWithoutAdditionalSafeguards())) {
            threat.setFrequencyWithAdditionalSafeguards(null);
            threat.setRiskWithoutAdditionalSafeguards(null);
            threat.setRiskWithAdditionalSafeguards(null);
            return true;
        }
        return false;
    }

    private static LinkedRequirementsInfo getLinkedRequirementsForRiskDeduction(BpThreat threat)
            throws CommandException {

        GetLinkedRequirementsInfo command = new GetLinkedRequirementsInfo(threat);
        ICommandService commandService = (ICommandService) VeriniceContext
                .get(VeriniceContext.COMMAND_SERVICE);
        commandService.executeCommand(command);
        return command.getLinkedRequirementsInfo();
    }

    private static String getFrequencyWithAdditionalSafeguards(
            Stream<String> frequenciesWithoutAdditionalSafeguard,
            Set<String> freqenciesFromLinkedRequirements) {
        Set<String> allFrequencies = Stream.concat(frequenciesWithoutAdditionalSafeguard,
                freqenciesFromLinkedRequirements.stream()).collect(Collectors.toSet());
        return allFrequencies.contains(null) ? null : Collections.min(allFrequencies);
    }

    private static String getImpactsWithAdditionalSafeguards(
            Stream<String> impactsWithoutAdditionalSafeguard,
            Set<String> impactsFromLinkedRequirements) {
        Set<String> allImpacts = Stream
                .concat(impactsWithoutAdditionalSafeguard, impactsFromLinkedRequirements.stream())
                .collect(Collectors.toSet());
        return allImpacts.contains(null) ? null : Collections.min(allImpacts);
    }

    private static Stream<CnATreeElement> getLinkedSafeguards(CnATreeElement requirement) {
        return requirement.getLinksDown().stream().filter(
                link -> BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(link.getRelationId()))
                .map(CnALink::getDependency);
    }

    private static RiskConfiguration findRiskConfiguration(Integer scopeId)
            throws CommandException {
        RiskService riskService = (RiskService) VeriniceContext
                .get(VeriniceContext.ITBP_RISK_SERVICE);
        return riskService.findRiskConfiguration(scopeId);
    }

    private static boolean notNullAndNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private static boolean isSafeGuardStrenghtBothSet(CnATreeElement e) {
        String f = e.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_IMPACT);
        String i = e.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_FREQUENCY);
        return notNullAndNotEmpty(i) && notNullAndNotEmpty(f);
    }

}
