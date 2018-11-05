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

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.bp.risk.RiskDeductionUtil;

public final class RiskInputValueChanged extends SelectionAdapter {
    private final CnATreeElement element;

    public RiskInputValueChanged(CnATreeElement element) {
        this.element = element;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        RiskDeductionUtil.deduceRisk((BpThreat) element);
    }
}