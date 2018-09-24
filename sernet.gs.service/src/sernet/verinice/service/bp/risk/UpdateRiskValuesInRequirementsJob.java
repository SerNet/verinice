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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
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
public class UpdateRiskValuesInRequirementsJob {

    private static final Logger log = Logger.getLogger(UpdateRiskValuesInRequirementsJob.class);

    private Set<BpRequirement> requirementsFromScope;

    private RiskConfigurationUpdateContext updateContext;
    private RiskConfigurationUpdateResult updateResult;

    private Set<String> uuidsOfChangedRequirements = new HashSet<>();

    public UpdateRiskValuesInRequirementsJob(RiskConfigurationUpdateContext updateContext,
            Set<BpRequirement> requirementsFromScope) {
        super();
        this.updateContext = updateContext;
        this.requirementsFromScope = requirementsFromScope;
    }

    public void run() {
        uuidsOfChangedRequirements.clear();
        removeFrequencies();
        removeImpacts();
        updateResult = new RiskConfigurationUpdateResult();
        updateResult.setNumberOfChangedRequirements(uuidsOfChangedRequirements.size());
        updateResult.setNumberOfRemovedFrequencies(updateContext.getDeletedFrequencies().size());
        updateResult.setNumberOfRemovedImpacts(updateContext.getDeletedImpacts().size());
        if (log.isInfoEnabled()) {
            logStatistic(updateResult);
        }
        uuidsOfChangedRequirements.clear();
    }

    private void removeFrequencies() {
        List<Frequency> removedFrequencies = updateContext.getDeletedFrequencies();
        List<String> removedFrequencyIds = removedFrequencies.stream().map(Frequency::getId)
                .collect(Collectors.toList());
        if (removedFrequencyIds.isEmpty()) {
            return;
        }
        for (BpRequirement requirement : requirementsFromScope) {
            String selectedId = requirement.getSafeguardStrengthFrequency();
            if (removedFrequencyIds.contains(selectedId)) {
                removeProperty(requirement,
                        BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY);
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
        for (BpRequirement requirement : requirementsFromScope) {
            String selectedId = requirement.getSafeguardStrengthImpact();
            if (removedImpactIds.contains(selectedId)) {
                removeProperty(requirement,
                        BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT);
            }
        }
    }

    private void removeProperty(CnATreeElement element, String propertyId) {
        element.getEntity().getTypedPropertyLists().remove(propertyId);
        uuidsOfChangedRequirements.add(element.getUuid());
        if (log.isDebugEnabled()) {
            log.debug("Property " + propertyId + " removed from " + element.getTypeId()
                    + " with uuid " + element.getUuid());
        }
    }

    public RiskConfigurationUpdateResult getRiskConfigurationUpdateResult() {
        return updateResult;
    }

    private static void logStatistic(RiskConfigurationUpdateResult updateResult) {
        log.debug("Removed frequencies: " + updateResult.getNumberOfRemovedFrequencies());
        log.debug("Removed impacts: " + updateResult.getNumberOfRemovedImpacts());
        log.debug("Changed threats: " + updateResult.getNumberOfChangedThreats());
    }

}
