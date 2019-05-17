/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import sernet.verinice.model.bp.elements.BpThreat;

public final class ResetRiskPropertiesWithAdditionalSafeguards extends SelectionAdapter {
    private final BpThreat element;

    public ResetRiskPropertiesWithAdditionalSafeguards(BpThreat threat) {
        this.element = threat;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        String riskTreatmentOption = element.getEntity()
                .getRawPropertyValue(BpThreat.PROP_RISK_TREATMENT_OPTION);
        if (!"bp_threat_risk_treatment_option_risk_reduction".equals(riskTreatmentOption)) {
            element.setPropertyValue(BpThreat.PROP_FREQUENCY_WITH_ADDITIONAL_SAFEGUARDS, null);
            element.setPropertyValue(BpThreat.PROP_IMPACT_WITH_ADDITIONAL_SAFEGUARDS, null);
            element.setPropertyValue(BpThreat.PROP_RISK_WITH_ADDITIONAL_SAFEGUARDS, null);
        }
    }
}