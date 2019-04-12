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

import java.util.HashMap;
import java.util.Map;

import sernet.hui.swt.widgets.HitroUIComposite;
import sernet.hui.swt.widgets.IHuiControlFactory;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This class contains help functions for the UI classes of the base protection
 * risk analysis.
 */
public final class RiskUiUtils {

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
        if (element instanceof BpRequirement) {
            overrides.put(BpRequirement.PROP_SAFEGUARD_STRENGTH_FREQUENCY,
                    new FrequencyControlFactory(element, true));
            overrides.put(BpRequirement.PROP_SAFEGUARD_STRENGTH_IMPACT,
                    new ImpactControlFactory(element, true));
        } else if (element instanceof Safeguard) {
            overrides.put(Safeguard.PROP_STRENGTH_FREQUENCY,
                    new FrequencyControlFactory(element, true));
            overrides.put(Safeguard.PROP_STRENGTH_IMPACT, new ImpactControlFactory(element, true));
        } else if (element instanceof BpThreat) {
            overrides.put(BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    new FrequencyControlFactory(element, false));
            overrides.put(BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    new ImpactControlFactory(element, false));
            overrides.put(BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    new RiskValueControlFactory(element));
            overrides.put(BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS,
                    new FrequencyControlFactory(element, false));
            overrides.put(BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS,
                    new ImpactControlFactory(element, false));
            overrides.put(BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS,
                    new RiskValueControlFactory(element));
        }
        return overrides;
    }

    /**
     * Add selection listeners to a HUI composite for a given element.
     */
    public static void addSelectionListener(HitroUIComposite huiComposite, CnATreeElement element) {
        if (element instanceof BpThreat) {
            BpThreat threat = (BpThreat) element;
            addRiskComputeListeners(huiComposite, threat,
                    BpThreat.PROP_FREQUENCY_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITHOUT_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_RISK_WITHOUT_ADDITIONAL_SAFEGUARDS);

            addRiskComputeListeners(huiComposite, threat,
                    BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS,
                    BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS);
        }
    }

    private static void addRiskComputeListeners(HitroUIComposite huiComposite, BpThreat threat,
            String frequencyProperty, String impactProperty, String riskProperty) {
        ComputeRisk listener = new ComputeRisk(threat, frequencyProperty, impactProperty,
                riskProperty);
        huiComposite.addSelectionListener(frequencyProperty, listener);
        huiComposite.addSelectionListener(impactProperty, listener);
    }
}
