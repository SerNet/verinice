/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.verinice.service.linktable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.StringUtil;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationCache;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.RetrieveCnATreeElement;

public class BpRiskValuePropertyAdapter implements IPropertyAdapter {

    private static final Logger log = Logger.getLogger(BpRiskValuePropertyAdapter.class);

    public static final Collection<String> riskPropertiesThreat = Collections
            .unmodifiableList(Arrays.asList(BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS));

    public static final Collection<String> riskPropertiesRequirement = Collections
            .unmodifiableList(Arrays.asList(BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY,
                    BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT));

    public static final Collection<String> riskPropertiesSafeguard = Collections.unmodifiableList(
            Arrays.asList(Safeguard.PROP_STRENGTH_FREQUENCY, Safeguard.PROP_STRENGTH_IMPACT));

    private CnATreeElement element;

    private RiskConfigurationCache riskConfigurationCache;

    private ICommandService commandService;

    public BpRiskValuePropertyAdapter(CnATreeElement element,
            RiskConfigurationCache riskConfigurationCache) {
        this.element = element;
        this.riskConfigurationCache = riskConfigurationCache;
    }

    @Override
    public String getPropertyValue(String propertyId) {
        if (element instanceof BpThreat) {
            BpThreat threat = (BpThreat) element;
            switch (propertyId) {
            case BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS:
                return getLabelForFrequency(threat.getFrequencyWithoutAdditionalSafeguards(),
                        element);
            case BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS:
                return getLabelForImpact(threat.getImpactWithoutAdditionalSafeguards(), element);
            case BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS:
                return getLabelForRisk(threat.getRiskWithoutAdditionalSafeguards(), element);
            case BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS:
                return getLabelForFrequency(threat.getFrequencyWithAdditionalSafeguards(), element);
            case BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS:
                return getLabelForImpact(threat.getImpactWithAdditionalSafeguards(), element);
            case BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS:
                return getLabelForRisk(threat.getRiskWithAdditionalSafeguards(), element);
            default:
                throw new IllegalArgumentException(
                        "Unhandled combination: " + element + ", " + propertyId);
            }
        }
        if (element instanceof BpRequirement) {
            BpRequirement requirement = (BpRequirement) element;
            switch (propertyId) {
            case BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY:
                return getLabelForFrequency(requirement.getSafeguardStrengthFrequency(), element);
            case BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT:
                return getLabelForFrequency(requirement.getSafeguardStrengthImpact(), element);
            default:
                throw new IllegalArgumentException(
                        "Unhandled combination: " + element + ", " + propertyId);
            }
        }
        if (element instanceof Safeguard) {
            Safeguard safeguard = (Safeguard) element;
            switch (propertyId) {
            case Safeguard.PROP_STRENGTH_FREQUENCY:
                String frequencyId = StringUtil.replaceEmptyStringByNull(safeguard.getEntity()
                        .getRawPropertyValue(Safeguard.PROP_STRENGTH_FREQUENCY));
                return getLabelForFrequency(frequencyId, element);
            case Safeguard.PROP_STRENGTH_IMPACT:
                String impactId = StringUtil.replaceEmptyStringByNull(
                        safeguard.getEntity().getRawPropertyValue(Safeguard.PROP_STRENGTH_IMPACT));
                return getLabelForFrequency(impactId, element);
            default:
                throw new IllegalArgumentException(
                        "Unhandled combination: " + element + ", " + propertyId);
            }
        }
        throw new IllegalArgumentException("Unhandled element type: " + element);

    }

    private String getLabelForFrequency(String frequencyId, CnATreeElement element) {
        RiskConfiguration riskConfiguration = getRiskConfiguration(element);
        return riskConfiguration.getFrequencies().stream()
                .filter(item -> item.getId().equals(frequencyId)).findFirst()
                .map(Frequency::getLabel).orElse(null);
    }

    private String getLabelForImpact(String impactId, CnATreeElement element) {
        RiskConfiguration riskConfiguration = getRiskConfiguration(element);
        return riskConfiguration.getImpacts().stream().filter(item -> item.getId().equals(impactId))
                .findFirst().map(Impact::getLabel).orElse(null);
    }

    private String getLabelForRisk(String riskId, CnATreeElement element) {
        RiskConfiguration riskConfiguration = getRiskConfiguration(element);
        return riskConfiguration.getRisks().stream().filter(item -> item.getId().equals(riskId))
                .findFirst().map(Risk::getLabel).orElse(null);
    }

    private RiskConfiguration getRiskConfiguration(CnATreeElement element) {
        RiskConfiguration riskConfigurationFromCache = riskConfigurationCache
                .findRiskConfiguration(element.getScopeId());
        if (riskConfigurationFromCache != null) {
            return riskConfigurationFromCache;
        }
        RiskConfiguration riskConfiguration = loadRiskConfiguration(element);
        riskConfigurationCache.putRiskConfiguration(element.getScopeId(), riskConfiguration);
        return riskConfiguration;
    }

    private RiskConfiguration loadRiskConfiguration(CnATreeElement element) {
        validateScopeId(element);
        Integer scopeId = element.getScopeId();
        if (log.isInfoEnabled()) {
            log.info(
                    "Loading scope with id: " + scopeId + ". Element with scope id is: " + element);
        }
        RetrieveCnATreeElement retrieveCommand = new RetrieveCnATreeElement(ItNetwork.TYPE_ID,
                scopeId,
                RetrieveInfo.getPropertyInstance());
        try {
            retrieveCommand = getCommandService().executeCommand(retrieveCommand);
        } catch (CommandException e) {
            String message = "Error while loading risk configuration";
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
        RiskConfiguration riskConfiguration = ((ItNetwork) retrieveCommand.getElement())
                .getRiskConfiguration();
        if (log.isDebugEnabled()) {
            log.debug("Risk configuration of scope with id: " + scopeId + " is: "
                    + riskConfiguration);
        }
        return riskConfiguration;
    }

    private void validateScopeId(CnATreeElement element) {
        if (element.getScopeId() == null) {
            String message = "Scope ID of element is null: " + element
                    + ". Can not load risk configuration.";
            log.error(message);
            throw new IllegalStateException(message);
        }
    }

    private ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
}
