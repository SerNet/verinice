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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.hui.swt.widgets.IHuiControlFactory;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This class contains help functions for the UI classes of the base protection
 * risk analysis.
 */
public final class RiskUiUtils {

    private static final Map<String, String> RISK_ANALYSIS_NECESSARY_PROPERTY_ID_BY_TYPE_ID;

    static {
        Map<String, String> m = new HashMap<>();

        m.put(Application.TYPE_ID, Application.PROP_RISKANALYSIS_NECESSARY);
        m.put(BusinessProcess.TYPE_ID, BusinessProcess.PROP_RISKANALYSIS_NECESSARY);
        m.put(Device.TYPE_ID, Device.PROP_RISKANALYSIS_NECESSARY);
        m.put(IcsSystem.TYPE_ID, IcsSystem.PROP_RISKANALYSIS_NECESSARY);
        m.put(ItSystem.TYPE_ID, ItSystem.PROP_RISKANALYSIS_NECESSARY);
        m.put(Network.TYPE_ID, Network.PROP_RISKANALYSIS_NECESSARY);
        m.put(Room.TYPE_ID, Room.PROP_RISKANALYSIS_NECESSARY);

        RISK_ANALYSIS_NECESSARY_PROPERTY_ID_BY_TYPE_ID = Collections.unmodifiableMap(m);
    }

    private RiskUiUtils() {
        // Do not instantiate this class.
    }

    /**
     * @return A map of IHuiControlFactories for the given element. The key is a
     *         property ID from SNCA.xml.
     */
    public static Map<String, IHuiControlFactory> createHuiControlFactories(
            CnATreeElement element) {
        Map<String, IHuiControlFactory> overrides = new HashMap<>();
        if (element instanceof BpThreat) {
            Stream.of(BpThreat.PROP_FREQUENCY_WITHOUT_SAFEGUARDS,
                    BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS)
                    .forEach(property -> overrides.put(property,
                            new DynamicRiskPropertiesControlFactory(element,
                                    RiskConfiguration::getFrequencies)));
            Stream.of(BpThreat.PROP_IMPACT_WITHOUT_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS)
                    .forEach(property -> overrides.put(property,
                            new DynamicRiskPropertiesControlFactory(element,
                                    RiskConfiguration::getImpacts)));
            Stream.of(BpThreat.PROP_RISK_WITHOUT_SAFEGUARDS,
                    BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS)
                    .forEach(property -> overrides.put(property,
                            new DynamicRiskPropertiesControlFactory(element,
                                    RiskConfiguration::getRisks)));
        }
        return overrides;
    }

    /**
     * Add selection listeners to a HUI composite for a given element.
     */
    public static void addSelectionListener(HitroUIComposite huiComposite, CnATreeElement element) {
        if (element instanceof BpThreat) {
            BpThreat threat = (BpThreat) element;
            huiComposite.addSelectionListener(BpThreat.PROP_RISK_TREATMENT_OPTION,
                    new ResetRiskPropertiesWithAdditionalSafeguards(threat));
            addRiskComputeListeners(huiComposite, threat,
                    BpThreat.PROP_FREQUENCY_WITHOUT_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITHOUT_SAFEGUARDS, BpThreat.PROP_RISK_WITHOUT_SAFEGUARDS);

            addRiskComputeListeners(huiComposite, threat,
                    BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS);

            addRiskComputeListeners(huiComposite, threat,
                    BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS);

            ValidateRiskPropertyValues changeListener = new ValidateRiskPropertyValues(huiComposite,
                    threat);
            Stream.of(BpThreat.PROP_FREQUENCY_WITHOUT_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITHOUT_SAFEGUARDS,
                    BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS)
                    .forEach(property -> huiComposite.addSelectionListener(property,
                            changeListener));

        }
    }

    private static void addRiskComputeListeners(HitroUIComposite huiComposite, BpThreat threat,
            String frequencyProperty, String impactProperty, String riskProperty) {
        ComputeRisk listener = new ComputeRisk(threat, frequencyProperty, impactProperty,
                riskProperty);
        huiComposite.addSelectionListener(frequencyProperty, listener);
        huiComposite.addSelectionListener(impactProperty, listener);
    }

    /**
     * Get the {@link EffectiveRisk effective risk} for a threat.<br>
     * If the risk treatment option is unset, it is the risk without additional
     * safeguards or {@link EffectiveRisk#UNKNOWN} if the risk is not
     * specified.<br>
     * If the risk treatment option is risk reduction, this is the risk with
     * additional safeguards or {@link EffectiveRisk#UNKNOWN} if the risk is not
     * specified.<br>
     * For any other risk treatment option, the result is
     * {@link EffectiveRisk#TREATED}.
     *
     * @see EffectiveRisk
     */
    public static EffectiveRisk getEffectiveRisk(BpThreat threat) {
        String riskTreatment = threat.getEntity()
                .getRawPropertyValue(BpThreat.PROP_RISK_TREATMENT_OPTION);
        if (StringUtils.isEmpty(riskTreatment)) {
            return Optional.ofNullable(threat.getRiskWithoutAdditionalSafeguards())
                    .map(EffectiveRisk::of).orElse(EffectiveRisk.UNKNOWN);
        } else if (BpThreat.PROP_RISK_TREATMENT_OPTION_RISK_REDUCTION.equals(riskTreatment)) {
            return Optional.ofNullable(threat.getRiskWithAdditionalSafeguards())
                    .map(EffectiveRisk::of).orElse(EffectiveRisk.UNKNOWN);
        } else {
            return EffectiveRisk.TREATED;
        }
    }

    /**
     * Get the {@link EffectiveRisk effective risk} for a threat group, which is
     * the maximum effective risk of all of its children or
     * {@link EffectiveRisk#UNKNOWN} if the effective risk for a child is
     * {@link EffectiveRisk#UNKNOWN}.
     *
     * @see EffectiveRisk
     * @see #getEffectiveRisk(BpThreat)
     */
    public static EffectiveRisk getEffectiveRisk(BpThreatGroup threatGroup) {
        if (!Retriever.areChildrenInitialized(threatGroup)
                || !threatGroup.getChildren().stream().allMatch(Retriever::isElementInitialized)) {
            threatGroup = (BpThreatGroup) Retriever.retrieveElement(threatGroup,
                    new RetrieveInfo().setChildren(true).setChildrenProperties(true));
        }
        Set<CnATreeElement> children = threatGroup.getChildren();
        if (!children.isEmpty() && children.stream().noneMatch(BpThreatGroup.class::isInstance)) {
            Set<EffectiveRisk> effectiveRisks = children.stream().filter(BpThreat.class::isInstance)
                    .map(BpThreat.class::cast).map(RiskUiUtils::getEffectiveRisk)
                    .collect(Collectors.toSet());
            if (!effectiveRisks.isEmpty()) {
                if (effectiveRisks.contains(EffectiveRisk.UNKNOWN)) {
                    return EffectiveRisk.UNKNOWN;
                }
                return effectiveRisks.stream().max(Comparator.comparing(EffectiveRisk::getRiskId))
                        .orElseThrow(IllegalStateException::new);
            }
        }
        return null;
    }

    /**
     * Get the {@link EffectiveRisk effective risk} for a target object which is
     * the maximum effective risk of all linked threats.<br>
     * If risk analysis is not necessary for the target object, this method
     * returns <code>null</code>.<br>
     * If all of the linked threats' risks are {@link EffectiveRisk#TREATED},
     * the target's object's effective risk is {@link EffectiveRisk#TREATED} as
     * well.<br>
     * If there are no linked threats or some of them have an
     * {@link EffectiveRisk#UNKNOWN unknown effective risk}, the target's
     * object's effective risk is {@link EffectiveRisk#UNKNOWN}.
     */
    public static EffectiveRisk getEffectiveRisk(CnATreeElement element) {
        String riskAnalysisNecessaryPropertyId = RISK_ANALYSIS_NECESSARY_PROPERTY_ID_BY_TYPE_ID
                .get(element.getTypeId());
        if (riskAnalysisNecessaryPropertyId != null
                && element.getEntity().isFlagged(riskAnalysisNecessaryPropertyId)) {
            return getEffectiveRiskRiskAnalsisNecessary(element);
        }
        return null;
    }

    private static EffectiveRisk getEffectiveRiskRiskAnalsisNecessary(CnATreeElement element) {
        if (!Retriever.areLinksInitizialized(element, true) || !element.getLinksUp().stream()
                .map(CnALink::getDependant).allMatch(Retriever::isElementInitialized)) {
            element = Retriever.retrieveElement(element,
                    new RetrieveInfo().setLinksUp(true).setLinksUpProperties(true));
        }
        Set<CnATreeElement> linkedThreats = element.getLinksUp().stream()
                .filter(link -> link.getDependant() instanceof BpThreat).map(CnALink::getDependant)
                .collect(Collectors.toSet());
        if (!linkedThreats.isEmpty()) {
            Set<EffectiveRisk> effectiveRisksFromLinkedThreats = linkedThreats.stream()
                    .filter(BpThreat.class::isInstance).map(BpThreat.class::cast)
                    .map(RiskUiUtils::getEffectiveRisk).collect(Collectors.toSet());
            if (!effectiveRisksFromLinkedThreats.isEmpty()) {
                Optional<EffectiveRisk> risk = determineRiskForTargetObject(
                        effectiveRisksFromLinkedThreats);
                if (risk.isPresent()) {
                    return risk.get();
                }
            }
        }
        return EffectiveRisk.UNKNOWN;
    }

    private static Optional<EffectiveRisk> determineRiskForTargetObject(
            Set<EffectiveRisk> effectiveRisksFromLinkedThreats) {
        if (effectiveRisksFromLinkedThreats.size() == 1
                && effectiveRisksFromLinkedThreats.contains(EffectiveRisk.TREATED)) {
            return Optional.of(EffectiveRisk.TREATED);
        } else if (effectiveRisksFromLinkedThreats.contains(EffectiveRisk.UNKNOWN)) {
            return Optional.of(EffectiveRisk.UNKNOWN);
        }

        EffectiveRisk maximumRisk = Collections.max(effectiveRisksFromLinkedThreats,
                Comparator.comparing(EffectiveRisk::getRiskId));
        if (!EffectiveRisk.TREATED.equals(maximumRisk)) {
            return Optional.of(maximumRisk);
        }
        return Optional.empty();
    }
}
