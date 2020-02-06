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

import org.apache.commons.lang.StringUtils;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
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
        final String frequencyWithoutSafeguards = threat.getFrequencyWithoutSafeguards();
        final String frequencyWithoutAdditionalSafeguards = threat
                .getFrequencyWithoutAdditionalSafeguards();
        final String frequencyWithAdditionalSafeguards = threat
                .getFrequencyWithAdditionalSafeguards();
        final String impactWithoutSafeguards = threat.getImpactWithoutSafeguards();
        final String impactWithoutAdditionalSafeguards = threat
                .getImpactWithoutAdditionalSafeguards();
        final String impactWithAdditionalSafeguards = threat.getImpactWithAdditionalSafeguards();

        RiskConfiguration riskConfiguration = findRiskConfiguration(threat.getScopeId());

        String riskWithoutSafeguards = calculateRisk(riskConfiguration, frequencyWithoutSafeguards,
                impactWithoutSafeguards).orElse(null);
        setPropertyIfNecessary(threat, BpThreat.PROP_RISK_WITHOUT_SAFEGUARDS,
                riskWithoutSafeguards);

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

    private static RiskConfiguration findRiskConfiguration(Integer scopeId) {
        RiskService riskService = (RiskService) VeriniceContext
                .get(VeriniceContext.ITBP_RISK_SERVICE);
        return riskService.findRiskConfigurationOrDefault(scopeId);
    }

}
