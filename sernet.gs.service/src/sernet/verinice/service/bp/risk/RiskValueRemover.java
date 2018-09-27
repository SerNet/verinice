/*******************************************************************************
 * Copyright (c) 2018 <Vorname> <Nachname>.
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

import sernet.gs.service.StringUtil;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateContext;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateResult;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Removes risk properties from elements when these properties have been deleted
 * from a risk configuration.
 * 
 * Extend this abstract class and overwrite abstract methods
 * getFrequencyPropertyId() and getImpactPropertyId(). Both methods must return
 * a property ID from the SNCA.xml.
 */
public abstract class RiskValueRemover {

    private static final Logger log = Logger.getLogger(RiskValueRemover.class);

    protected Set<CnATreeElement> elements;
    protected RiskConfigurationUpdateContext updateContext;
    protected RiskConfigurationUpdateResult updateResult;
    private Set<String> uuidsOfChangedElements = new HashSet<>();

    public RiskValueRemover(RiskConfigurationUpdateContext updateContext,
            Set<CnATreeElement> elements) {
        super();
        this.updateContext = updateContext;
        this.elements = elements;
    }

    public void execute() {
        uuidsOfChangedElements.clear();
        removeFrequencies();
        removeImpacts();
        updateResult = new RiskConfigurationUpdateResult();
        saveNumberOfChangedElements(uuidsOfChangedElements.size());
        updateResult.setNumberOfRemovedFrequencies(updateContext.getDeletedFrequencies().size());
        updateResult.setNumberOfRemovedImpacts(updateContext.getDeletedImpacts().size());
        if (log.isInfoEnabled()) {
            logStatistic(updateResult);
        }
        uuidsOfChangedElements.clear();
    }
    private void removeFrequencies() {
        List<Frequency> removedFrequencies = updateContext.getDeletedFrequencies();
        List<String> removedFrequencyIds = removedFrequencies.stream().map(Frequency::getId)
                .collect(Collectors.toList());
        removeProperty(removedFrequencyIds, getFrequencyPropertyId());
    }

    private void removeImpacts() {
        List<Impact> removedImpacts = updateContext.getDeletedImpacts();
        List<String> removedImpactIds = removedImpacts.stream().map(Impact::getId)
                .collect(Collectors.toList());
        removeProperty(removedImpactIds, getImpactPropertyId());
    }

    protected abstract String getFrequencyPropertyId();

    protected abstract String getImpactPropertyId();

    protected abstract void saveNumberOfChangedElements(int numberOfChangedElements);

    protected void removeProperty(List<String> removedIdsFromConfiguration, String propertyId) {
        if (removedIdsFromConfiguration.isEmpty()) {
            return;
        }
        for (CnATreeElement requirement : elements) {
            String selectedId = getSelectedId(requirement, propertyId);
            if (removedIdsFromConfiguration.contains(selectedId)) {
                removeProperty(requirement, propertyId);
            }
        }
    }

    private String getSelectedId(CnATreeElement element, String propertyId) {
        return StringUtil
                .replaceEmptyStringByNull(element.getEntity().getRawPropertyValue(propertyId));
    }

    private void removeProperty(CnATreeElement element, String propertyId) {
        element.getEntity().getTypedPropertyLists().remove(propertyId);
        uuidsOfChangedElements.add(element.getUuid());
        if (log.isDebugEnabled()) {
            log.debug("Property " + propertyId + " removed from " + element.getTypeId()
                    + " with uuid " + element.getUuid());
        }
    }

    public RiskConfigurationUpdateResult getRiskConfigurationUpdateResult() {
        return updateResult;
    }

    private static void logStatistic(RiskConfigurationUpdateResult updateResult) {
        log.debug(updateResult);
    }

}
