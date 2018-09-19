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
package sernet.verinice.bp.rcp.risk.ui;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

public final class RiskInputValueChanged extends SelectionAdapter {
    private final CnATreeElement element;

    public RiskInputValueChanged(CnATreeElement element) {
        this.element = element;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        BpThreat threat = (BpThreat) element;

        String frequency = threat.getFrequencyWithoutAdditionalSafeguards();
        String impact = threat.getImpactWithoutAdditionalSafeguards();

        if (frequency == null || frequency.isEmpty()) {
            threat.setFrequencyWithAdditionalSafeguards(null);
            threat.setRiskWithoutAdditionalSafeguards(null);
            threat.setRiskWithAdditionalSafeguards(null);
        } else if (impact == null || impact.isEmpty()) {
            threat.setImpactWithAdditionalSafeguards(null);
            threat.setRiskWithoutAdditionalSafeguards(null);
            threat.setRiskWithAdditionalSafeguards(null);
        } else {
            ItNetwork itNetwork = (ItNetwork) CnATreeElementScopeUtils.getScope(element);
            RiskConfiguration riskConfiguration = Optional
                    .ofNullable(itNetwork.getRiskConfiguration())
                    .orElseGet(DefaultRiskConfiguration::getInstance);
            Risk risk = riskConfiguration.getRisk(frequency, impact);
            threat.setRiskWithoutAdditionalSafeguards(risk.getId());

            // TODO extract this into a utility method and reuse it for
            // VN-2197
            Set<CnATreeElement> linkedRequirements = threat.getLinksUp().stream()
                    .filter(link -> BpRequirement.REL_BP_REQUIREMENT_BP_THREAT
                            .equals(link.getRelationId())
                            && BpRequirement.TYPE_ID.equals(link.getDependant().getTypeId()))
                    .map(CnALink::getDependant).collect(Collectors.toSet());

            Set<String> allFrequencies = Stream
                    .concat(Stream.of(frequency), linkedRequirements.stream()
                            .map(requirement -> requirement.getEntity().getRawPropertyValue(
                                    BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY)))
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(Collectors.toSet());
            Set<String> allImpacts = Stream
                    .concat(Stream.of(impact), linkedRequirements.stream()
                            .map(requirement -> requirement.getEntity().getRawPropertyValue(
                                    BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT)))
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(Collectors.toSet());

            String frequencyWithAdditionalSafeguards = allFrequencies.contains(null) ? null
                    : Collections.min(allFrequencies);
            String impactWithAdditionalSafeguards = allImpacts.contains(null) ? null
                    : Collections.min(allImpacts);
            threat.setFrequencyWithAdditionalSafeguards(
                    frequencyWithAdditionalSafeguards);
            threat.setImpactWithAdditionalSafeguards(impactWithAdditionalSafeguards);
            if (frequencyWithAdditionalSafeguards == null
                    || impactWithAdditionalSafeguards == null) {
                threat.setRiskWithAdditionalSafeguards(null);
            } else {
                Risk riskWithAdditionalSafeguards = riskConfiguration
                        .getRisk(frequencyWithAdditionalSafeguards,
                                impactWithAdditionalSafeguards);
                threat.setRiskWithAdditionalSafeguards(
                        riskWithAdditionalSafeguards.getId());
            }

        }

    }
}