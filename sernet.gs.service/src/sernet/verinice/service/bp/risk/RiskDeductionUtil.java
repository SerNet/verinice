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

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

public class RiskDeductionUtil {

    private RiskDeductionUtil() {
    }

    /**
     * Update the safeguard strength and risk category of the threat. Uses the
     * VeriniceContext to execute LoadCnAElementById command to find the
     * scope/risk configuration.
     */
    public static BpThreat deduceRisk(BpThreat threat) {
        final String frequencyWithoutAdditionalSafeguards = threat
                .getFrequencyWithoutAdditionalSafeguards();
        final String frequencyWithAdditionalSafeguards = threat
                .getFrequencyWithAdditionalSafeguards();
        final String impactWithoutAdditionalSafeguards = threat
                .getImpactWithoutAdditionalSafeguards();
        final String impactWithAdditionalSafeguards = threat.getImpactWithAdditionalSafeguards();

        RiskConfiguration riskConfiguration = Optional
                .ofNullable(findRiskConfiguration(threat.getScopeId()))
                .orElseGet(DefaultRiskConfiguration::getInstance);

        String riskWithoutAdditionalSafeguards = calculateRisk(riskConfiguration,
                frequencyWithoutAdditionalSafeguards, impactWithoutAdditionalSafeguards)
                        .orElse(null);
        setPropertyIfNecessary(threat, BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS,
                riskWithoutAdditionalSafeguards);

        String riskWithAdditionalSafeguards = calculateRisk(riskConfiguration,
                frequencyWithAdditionalSafeguards, impactWithAdditionalSafeguards).orElse(null);
        setPropertyIfNecessary(threat, BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS,
                riskWithAdditionalSafeguards);

        return threat;
    }

    public static Optional<String> calculateRisk(RiskConfiguration riskConfiguration,
            String frequency, String impact) {

        if (StringUtils.isEmpty(frequency) || StringUtils.isEmpty(impact)) {
            return Optional.empty();
        }

        return Optional.ofNullable(riskConfiguration.getRisk(frequency, impact)).map(Risk::getId);
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
