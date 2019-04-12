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

import java.util.Optional;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.service.bp.risk.RiskDeductionUtil;

public class ComputeRisk extends SelectionAdapter {
    private final BpThreat threat;
    private final String frequencyProperty;
    private final String impactProperty;
    private final String riskProperty;

    public ComputeRisk(BpThreat threat, String frequencyProperty, String impactProperty,
            String riskProperty) {
        this.threat = threat;
        this.frequencyProperty = frequencyProperty;
        this.impactProperty = impactProperty;
        this.riskProperty = riskProperty;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        RiskConfiguration riskConfiguration = ((ItNetwork) CnATreeElementScopeUtils
                .getScope(threat)).getRiskConfigurationOrDefault();
        Optional<String> risk = RiskDeductionUtil.calculateRisk(riskConfiguration,
                threat.getEntity().getRawPropertyValue(frequencyProperty),
                threat.getEntity().getRawPropertyValue(impactProperty));
        threat.setPropertyValue(riskProperty, risk.orElse(null));
    }

}