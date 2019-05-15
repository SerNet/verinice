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
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
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

    private BpThreat threat;

    private RiskConfigurationCache riskConfigurationCache;

    private ICommandService commandService;

    public BpRiskValuePropertyAdapter(BpThreat threat,
            RiskConfigurationCache riskConfigurationCache) {
        this.threat = threat;
        this.riskConfigurationCache = riskConfigurationCache;
    }

    @Override
    public String getPropertyValue(String propertyId) {
        switch (propertyId) {
        case BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS:
            return getLabelForFrequency(threat.getFrequencyWithoutAdditionalSafeguards(), threat);
        case BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS:
            return getLabelForImpact(threat.getImpactWithoutAdditionalSafeguards(), threat);
        case BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS:
            return getLabelForRisk(threat.getRiskWithoutAdditionalSafeguards(), threat);
        case BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS:
            return getLabelForFrequency(threat.getFrequencyWithAdditionalSafeguards(), threat);
        case BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS:
            return getLabelForImpact(threat.getImpactWithAdditionalSafeguards(), threat);
        case BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS:
            return getLabelForRisk(threat.getRiskWithAdditionalSafeguards(), threat);
        default:
            throw new IllegalArgumentException(
                    "Unhandled combination: " + threat + ", " + propertyId);
        }
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
            if (log.isDebugEnabled()) {
                log.debug("Returning risk configuration from cache: " + riskConfigurationFromCache);
            }
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
                scopeId, RetrieveInfo.getPropertyInstance());
        try {
            retrieveCommand = getCommandService().executeCommand(retrieveCommand);
        } catch (CommandException e) {
            String message = "Error while loading risk configuration";
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
        RiskConfiguration riskConfiguration = ((ItNetwork) retrieveCommand.getElement())
                .getRiskConfigurationOrDefault();
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
