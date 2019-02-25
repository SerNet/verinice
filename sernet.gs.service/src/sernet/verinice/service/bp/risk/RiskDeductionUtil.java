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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
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
        try {
            return RiskDeductionUtil.deduceRisk(threat,
                    () -> findRiskConfiguration(threat.getScopeId()));
        } catch (CommandException e) {
            logger.error("error fetching scope for bp_threat", e);
        }
        return threat;
    }

    private static BpThreat deduceRisk(BpThreat threat,
            Supplier<RiskConfiguration> riskConfigurationSupplier) throws CommandException {
        final String frequencyWithoutAdditionalSafeguards = threat
                .getFrequencyWithoutAdditionalSafeguards();
        final String impactWithoutAdditionalSafeguards = threat
                .getImpactWithoutAdditionalSafeguards();

        if (StringUtils.isEmpty(frequencyWithoutAdditionalSafeguards)
                && StringUtils.isEmpty(impactWithoutAdditionalSafeguards)) {
            setPropertyIfNecessary(threat, BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS,
                    frequencyWithoutAdditionalSafeguards);
            setPropertyIfNecessary(threat, BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS,
                    impactWithoutAdditionalSafeguards);
            setPropertyIfNecessary(threat, BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS, null);
            setPropertyIfNecessary(threat, BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS, null);
            return threat;
        }

        RiskConfiguration riskConfiguration = Optional.ofNullable(riskConfigurationSupplier.get())
                .orElseGet(DefaultRiskConfiguration::getInstance);

        String riskWithoutAdditionalSafeguards = calculateRisk(riskConfiguration,
                frequencyWithoutAdditionalSafeguards, impactWithoutAdditionalSafeguards)
                        .orElse(null);
        setPropertyIfNecessary(threat, BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS,
                riskWithoutAdditionalSafeguards);

        LinkedRequirementsInfo linkedRequirementsInfo = getLinkedRequirementsForRiskDeduction(
                threat);
        String frequencyWithAdditionalSafeguards = getFrequencyWithAdditionalSafeguards(
                Stream.of(frequencyWithoutAdditionalSafeguards),
                linkedRequirementsInfo.getFrequencies());
        String impactWithAdditionalSafeguards = getImpactsWithAdditionalSafeguards(
                Stream.of(impactWithoutAdditionalSafeguards), linkedRequirementsInfo.getImpacts());
        String riskWithAdditionalSafeguards = calculateRisk(riskConfiguration,
                frequencyWithAdditionalSafeguards, impactWithAdditionalSafeguards)
                        .orElse(riskWithoutAdditionalSafeguards);
        setPropertyIfNecessary(threat, BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS,
                frequencyWithAdditionalSafeguards);
        setPropertyIfNecessary(threat, BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS,
                impactWithAdditionalSafeguards);
        setPropertyIfNecessary(threat, BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS,
                riskWithAdditionalSafeguards);

        return threat;
    }

    private static Optional<String> calculateRisk(RiskConfiguration riskConfiguration,
            String frequency, String impact) {

        if (StringUtils.isEmpty(frequency) || StringUtils.isEmpty(impact)) {
            return Optional.empty();
        }

        return Optional.ofNullable(riskConfiguration.getRisk(frequency, impact)).map(Risk::getId);
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
                    RiskDeductionUtil.deduceRisk(threat, () -> riskConfiguration);
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

        boolean reducesRisk = getLinkedSafeguards(requirement)
                .anyMatch(s -> s.getEntity().isFlagged(Safeguard.PROP_REDUCE_RISK));

        String impactStrength = getLinkedSafeguards(requirement)
                .filter(s -> s.getEntity().isFlagged(Safeguard.PROP_REDUCE_RISK)
                        && isSafeGuardStrenghtBothSet(s))
                .map(s -> s.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_IMPACT))
                .filter(StringUtils::isNotEmpty).min(String::compareTo).orElse(null);

        String frequencyStrength = getLinkedSafeguards(requirement)
                .filter(s -> s.getEntity().isFlagged(Safeguard.PROP_REDUCE_RISK)
                        && isSafeGuardStrenghtBothSet(s))
                .map(s -> s.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_FREQUENCY))
                .filter(StringUtils::isNotEmpty).min(String::compareTo).orElse(null);
        requirement.getEntity().setFlag(BpRequirement.PROP_SAFEGUARD_REDUCE_RISK, reducesRisk);
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

    private static LinkedRequirementsInfo getLinkedRequirementsForRiskDeduction(BpThreat threat)
            throws CommandException {

        GetLinkedRequirementsInfo command = new GetLinkedRequirementsInfo(threat);
        ICommandService commandService = (ICommandService) VeriniceContext
                .get(VeriniceContext.COMMAND_SERVICE);
        command = commandService.executeCommand(command);
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

    private static RiskConfiguration findRiskConfiguration(Integer scopeId) {
        RiskService riskService = (RiskService) VeriniceContext
                .get(VeriniceContext.ITBP_RISK_SERVICE);
        return riskService.findRiskConfiguration(scopeId);
    }

    private static boolean isSafeGuardStrenghtBothSet(CnATreeElement e) {
        String f = e.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_IMPACT);
        String i = e.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_FREQUENCY);
        return StringUtils.isNotEmpty(i) && StringUtils.isNotEmpty(f);
    }

}
