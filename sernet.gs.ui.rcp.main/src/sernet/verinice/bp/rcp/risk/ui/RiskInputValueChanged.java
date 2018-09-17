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

import java.util.Optional;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import sernet.gs.ui.rcp.main.common.model.CnATreeElementScopeUtils;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.configuration.DefaultRiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.bp.risk.RiskDeductionUtil;

public final class RiskInputValueChanged extends SelectionAdapter {
    private final CnATreeElement element;

    public RiskInputValueChanged(CnATreeElement element) {
        this.element = element;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        BpThreat threat = (BpThreat) element;

        String frequency = threat.getFrequencyWithoutAdditionalSafeguards();
        String impact = threat.getImpactWithoutAdditionalSafeguards();

        // These checks are done is the RiskDeductionUtil, too
        // but we do them here to for performance reasons. we don't have to
        // fetch the scope in these cases.
        if (frequency == null || frequency.isEmpty()) {
            threat.setFrequencyWithAdditionalSafeguards(null);
            threat.setRiskWithoutAdditionalSafeguards(null);
            threat.setRiskWithAdditionalSafeguards(null);
        } else if (impact == null || impact.isEmpty()) {
            threat.setImpactWithAdditionalSafeguards(null);
            threat.setRiskWithoutAdditionalSafeguards(null);
            threat.setRiskWithAdditionalSafeguards(null);
        } else {
            ItNetwork itNetwork = (ItNetwork) CnATreeElementScopeUtils.getScope(element);
            RiskConfiguration riskConfiguration = Optional
                    .ofNullable(itNetwork.getRiskConfiguration())
                    .orElseGet(DefaultRiskConfiguration::getInstance);
            Risk risk = riskConfiguration.getRisk(frequency, impact);
            threat.setRiskWithoutAdditionalSafeguards(risk.getId());

            RiskDeductionUtil.deduceRisk(threat, riskConfiguration);
        }
    }
}