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
package sernet.verinice.service.bp.risk;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import sernet.gs.service.StringUtil;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.RiskPropertyValue;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateContext;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateResult;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Purposes of this class:
 * 
 * 1. Remove frequencies, impacts and risk values from threats that are no
 * longer in the configuration.
 * 
 * 2. Update of the risk properties in threats after the matrix in the
 * configuration has changed.
 * 
 * The changes made to threats in this class are not saved directly by this
 * class, they are saved indirectly by Hibernate. To ensure that the changes are
 * really saved, this class must be used in a JDBC transaction. The JDBC
 * transaction management is configured by Spring.
 */
public class RiskValueInThreatUpdater {

    private static final Logger log = Logger.getLogger(RiskValueInThreatUpdater.class);

    private Set<BpThreat> threatsFromScope;

    private RiskConfigurationUpdateContext updateContext;
    private RiskConfigurationUpdateResult updateResult;

    public RiskValueInThreatUpdater(RiskConfigurationUpdateContext updateContext,
            Set<BpThreat> threatsFromScope) {
        this.updateContext = updateContext;
        this.threatsFromScope = threatsFromScope;
    }

    public Set<BpThreat> execute() {
        Set<BpThreat> changedThreats = new HashSet<>();
        changedThreats.addAll(removeFrequencies());
        changedThreats.addAll(removeImpacts());
        changedThreats.addAll(removeRisks());
        changedThreats.addAll(checkAndFixRisks());
        updateResult = new RiskConfigurationUpdateResult();
        updateResult.setNumberOfChangedThreats(changedThreats.size());
        updateResult.setNumberOfRemovedFrequencies(updateContext.getDeletedFrequencies().size());
        updateResult.setNumberOfRemovedImpacts(updateContext.getDeletedImpacts().size());
        updateResult.setNumberOfRemovedRisks(updateContext.getDeletedRisks().size());
        if (log.isInfoEnabled()) {
            logStatistic(updateResult);
        }
        return Collections.unmodifiableSet(changedThreats);
    }

    private Collection<BpThreat> removeFrequencies() {
        return removeDeletedValues(updateContext.getDeletedFrequencies(),
                BpThreat.PROP_FREQUENCY_WITHOUT_SAFEGUARDS,
                BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS,
                BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS);
    }

    private Collection<BpThreat> removeImpacts() {
        return removeDeletedValues(updateContext.getDeletedImpacts(),
                BpThreat.PROP_IMPACT_WITHOUT_SAFEGUARDS,
                BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS,
                BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS);
    }

    private Collection<BpThreat> removeRisks() {
        return removeDeletedValues(updateContext.getDeletedRisks(),
                BpThreat.PROP_RISK_WITHOUT_SAFEGUARDS,
                BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS,
                BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS);
    }

    private Collection<BpThreat> removeDeletedValues(
            List<? extends RiskPropertyValue> deletedValues, String... propertyIds) {
        if (deletedValues.isEmpty() || propertyIds.length == 0) {
            return Collections.emptySet();
        }
        List<String> deletedValuesIDs = deletedValues.stream().map(RiskPropertyValue::getId)
                .collect(Collectors.toList());
        Set<BpThreat> result = new HashSet<>(threatsFromScope.size());
        for (BpThreat threat : threatsFromScope) {
            boolean valueRemoved = false;
            for (String propertyId : propertyIds) {
                String valueFromThreat = StringUtil.replaceEmptyStringByNull(
                        threat.getEntity().getRawPropertyValue(propertyId));
                if (deletedValuesIDs.contains(valueFromThreat)) {
                    removeProperty(threat, propertyId);
                    valueRemoved = true;
                }
            }
            if (valueRemoved) {
                result.add(threat);
            }
        }
        return result;
    }

    private Collection<BpThreat> checkAndFixRisks() {
        Set<BpThreat> result = new HashSet<>(threatsFromScope.size());
        for (BpThreat threat : threatsFromScope) {
            boolean valueChanged = false;
            valueChanged |= checkAndFixRiskWithoutSafeguards(threat);
            valueChanged |= checkAndFixRiskWithoutAdditionalSafeguards(threat);
            valueChanged |= checkAndFixRiskWithAdditionalSafeguards(threat);
            if (valueChanged) {
                result.add(threat);
            }
        }
        return result;

    }

    private boolean checkAndFixRiskWithoutSafeguards(BpThreat threat) {
        String impactIdInThreat = threat.getImpactWithoutSafeguards();
        String frequencyIdInThreat = threat.getFrequencyWithoutSafeguards();
        String riskIdInThreat = threat.getRiskWithoutSafeguards();
        String propertyId = BpThreat.PROP_RISK_WITHOUT_SAFEGUARDS;
        return checkAndFixRisk(threat, impactIdInThreat, frequencyIdInThreat, riskIdInThreat,
                propertyId);
    }

    private boolean checkAndFixRiskWithoutAdditionalSafeguards(BpThreat threat) {
        String impactIdInThreat = threat.getImpactWithoutAdditionalSafeguards();
        String frequencyIdInThreat = threat.getFrequencyWithoutAdditionalSafeguards();
        String riskIdInThreat = threat.getRiskWithoutAdditionalSafeguards();
        String propertyId = BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS;
        return checkAndFixRisk(threat, impactIdInThreat, frequencyIdInThreat, riskIdInThreat,
                propertyId);
    }

    private boolean checkAndFixRiskWithAdditionalSafeguards(BpThreat threat) {
        String impactIdInThreat = threat.getImpactWithAdditionalSafeguards();
        String frequencyIdInThreat = threat.getFrequencyWithAdditionalSafeguards();
        String riskIdInThreat = threat.getRiskWithAdditionalSafeguards();
        String propertyId = BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS;
        return checkAndFixRisk(threat, impactIdInThreat, frequencyIdInThreat, riskIdInThreat,
                propertyId);
    }

    private boolean checkAndFixRisk(BpThreat threat, String impactIdInThreat,
            String frequencyIdInThreat, String riskIdInThreat, String riskPropertyId) {
        if (impactIdInThreat == null || frequencyIdInThreat == null) {
            if (riskIdInThreat != null) {
                removeProperty(threat, riskPropertyId);
                return true;
            }
        } else {
            Risk riskInConfiguration = getConfiguration().getRisk(frequencyIdInThreat,
                    impactIdInThreat);
            if (riskIdInThreat == null && riskInConfiguration == null) {
                return false;
            }
            if (riskIdInThreat != null && riskInConfiguration == null) {
                removeProperty(threat, riskPropertyId);
                return true;
            } else if (riskIdInThreat == null
                    || !riskIdInThreat.equals(riskInConfiguration.getId())) {
                setPropertyValue(threat, riskPropertyId, riskInConfiguration.getId());
                return true;
            }
        }
        return false;

    }

    private void removeProperty(CnATreeElement element, String propertyId) {
        element.getEntity().getTypedPropertyLists().remove(propertyId);
        if (log.isDebugEnabled()) {
            log.debug("Property " + propertyId + " removed from " + element.getTypeId()
                    + " with uuid " + element.getUuid());
        }
    }

    private void setPropertyValue(CnATreeElement element, String propertyId, String value) {
        element.getEntity().setSimpleValue(element.getEntityType().getPropertyType(propertyId),
                value);
        if (log.isDebugEnabled()) {
            log.debug(propertyId + "=" + value + " set in " + element.getTypeId());
        }
    }

    private RiskConfiguration getConfiguration() {
        return updateContext.getRiskConfiguration();
    }

    public RiskConfigurationUpdateResult getRiskConfigurationUpdateResult() {
        return updateResult;
    }

    private static void logStatistic(RiskConfigurationUpdateResult updateResult) {
        log.debug("Removed frequencies: " + updateResult.getNumberOfRemovedFrequencies());
        log.debug("Removed impacts: " + updateResult.getNumberOfRemovedImpacts());
        log.debug("Removed risks: " + updateResult.getNumberOfRemovedRisks());
        log.debug("Changed threats: " + updateResult.getNumberOfChangedThreats());
    }

}
