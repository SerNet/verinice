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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;
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

    private Set<String> uuidsOfChangedThreats = new HashSet<>();

    private IBaseDao<PropertyList, Integer> propertyListDao;

    public RiskValueInThreatUpdater(RiskConfigurationUpdateContext updateContext,
            Set<BpThreat> threatsFromScope) {
        super();
        this.updateContext = updateContext;
        this.threatsFromScope = threatsFromScope;
    }

    public void execute() {
        uuidsOfChangedThreats.clear();
        removeFrequencies();
        removeImpacts();
        removeRisks();
        checkAndFixRisks();
        updateResult = new RiskConfigurationUpdateResult();
        updateResult.setNumberOfChangedThreats(getNumberOfChangedThreats());
        updateResult.setNumberOfRemovedFrequencies(updateContext.getDeletedFrequencies().size());
        updateResult.setNumberOfRemovedImpacts(updateContext.getDeletedImpacts().size());
        updateResult.setNumberOfRemovedRisks(updateContext.getDeletedRisks().size());
        if (log.isInfoEnabled()) {
            logStatistic(updateResult);
        }
        uuidsOfChangedThreats.clear();
    }

    private void removeFrequencies() {
        List<Frequency> removedFrequencies = updateContext.getDeletedFrequencies();
        List<String> removedFrequencyIds = removedFrequencies.stream().map(Frequency::getId)
                .collect(Collectors.toList());
        if (removedFrequencyIds.isEmpty()) {
            return;
        }
        for (BpThreat threat : threatsFromScope) {
            String selectedId = threat.getFrequencyWithoutAdditionalSafeguards();
            if (removedFrequencyIds.contains(selectedId)) {
                removeProperty(threat, BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS);
                removeProperty(threat, BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS);
            }
        }
    }

    private void removeImpacts() {
        List<Impact> removedImpacts = updateContext.getDeletedImpacts();
        List<String> removedImpactIds = removedImpacts.stream().map(Impact::getId)
                .collect(Collectors.toList());
        if (removedImpactIds.isEmpty()) {
            return;
        }
        for (BpThreat threat : threatsFromScope) {
            String selectedId = threat.getImpactWithoutAdditionalSafeguards();
            if (removedImpactIds.contains(selectedId)) {
                removeProperty(threat, BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS);
                removeProperty(threat, BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS);
            }
        }
    }

    private void removeRisks() {
        List<Risk> removedRisks = updateContext.getDeletedRisks();
        List<String> removedRiskIds = removedRisks.stream().map(Risk::getId)
                .collect(Collectors.toList());
        if (removedRiskIds.isEmpty()) {
            return;
        }
        for (BpThreat threat : threatsFromScope) {
            String selectedId = threat.getRiskWithoutAdditionalSafeguards();
            if (removedRiskIds.contains(selectedId)) {
                removeProperty(threat, BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS);
            }
        }
    }

    private void checkAndFixRisks() {
        for (BpThreat threat : threatsFromScope) {
            checkAndFixRiskWithoutAdditionalSafeguards(threat);
        }
    }

    private void checkAndFixRiskWithoutAdditionalSafeguards(BpThreat threat) {
        String impactIdInThreat = threat.getImpactWithoutAdditionalSafeguards();
        String frequencyIdInThreat = threat.getFrequencyWithoutAdditionalSafeguards();
        String riskIdInThreat = threat.getRiskWithoutAdditionalSafeguards();
        String propertyId = BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS;
        checkAndFixRisk(threat, impactIdInThreat, frequencyIdInThreat, riskIdInThreat, propertyId);
    }

    private void checkAndFixRisk(BpThreat threat, String impactIdInThreat,
            String frequencyIdInThreat, String riskIdInThreat, String riskPropertyId) {
        if (impactIdInThreat == null || frequencyIdInThreat == null) {
            if (riskIdInThreat != null) {
                removeProperty(threat, riskPropertyId);
            }
        } else {
            Risk riskInConfiguration = getConfiguration().getRisk(frequencyIdInThreat,
                    impactIdInThreat);
            if (riskIdInThreat == null && riskInConfiguration == null) {
                return;
            }
            if (riskIdInThreat != null && riskInConfiguration == null) {
                removeProperty(threat, riskPropertyId);
            } else if (riskIdInThreat == null
                    || !riskIdInThreat.equals(riskInConfiguration.getId())) {
                setPropertyValue(threat, riskPropertyId, riskInConfiguration.getId());
            }
        }
    }

    private void removeProperty(CnATreeElement element, String propertyId) {
        element.getEntity().getTypedPropertyLists().remove(propertyId);
        uuidsOfChangedThreats.add(element.getUuid());
        if (log.isDebugEnabled()) {
            log.debug("Property " + propertyId + " removed from " + element.getTypeId()
                    + " with uuid " + element.getUuid());
        }
    }

    private void setPropertyValue(CnATreeElement element, String propertyId, String value) {
        element.getEntity().setSimpleValue(element.getEntityType().getPropertyType(propertyId),
                value);
        savePropertylistsManually(element);
        uuidsOfChangedThreats.add(element.getUuid());
        if (log.isDebugEnabled()) {
            log.debug(propertyId + "=" + value + " set in " + element.getTypeId());
        }
    }

    /**
     * This method is a work around for a suspected error in Hibernate.
     * 
     * Manually saves the PropertyList instances contained in the
     * CnATreeElement. If this method is not called, then a Hibernate exception
     * is thrown when saving the threats after changing the RiskConfiguration:
     * 
     * org.hibernate.TransientObjectException: object references an unsaved
     * transient instance - save the transient instance before flushing:
     * sernet.hui.common.connect.PropertyList
     */
    private void savePropertylistsManually(CnATreeElement element) {
        Collection<PropertyList> propertyListCollection = element.getEntity()
                .getTypedPropertyLists().values();
        for (PropertyList propertyList : propertyListCollection) {
            getPropertyListDao().saveOrUpdate(propertyList);
        }
    }

    private RiskConfiguration getConfiguration() {
        return updateContext.getRiskConfiguration();
    }

    private int getNumberOfChangedThreats() {
        return uuidsOfChangedThreats.size();
    }

    public RiskConfigurationUpdateResult getRiskConfigurationUpdateResult() {
        return updateResult;
    }

    public IBaseDao<PropertyList, Integer> getPropertyListDao() {
        return propertyListDao;
    }

    public void setPropertyListDao(IBaseDao<PropertyList, Integer> propertyListDao) {
        this.propertyListDao = propertyListDao;
    }

    private static void logStatistic(RiskConfigurationUpdateResult updateResult) {
        log.debug("Removed frequencies: " + updateResult.getNumberOfRemovedFrequencies());
        log.debug("Removed impacts: " + updateResult.getNumberOfRemovedImpacts());
        log.debug("Removed risks: " + updateResult.getNumberOfRemovedRisks());
        log.debug("Changed threats: " + updateResult.getNumberOfChangedThreats());
    }

}
