/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.verinice.bp.rcp;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.swt.widgets.Composite;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.swt.widgets.IHuiControl;
import sernet.hui.swt.widgets.IHuiControlFactory;
import sernet.hui.swt.widgets.SingleSelectionControl;
import sernet.verinice.bp.rcp.risk.ui.RiskMatrixConfigurator;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnATreeElement;

public final class FrequencyControlFactory implements IHuiControlFactory {
    private final CnATreeElement element;
    private final boolean excludeMaximumValue;

    public FrequencyControlFactory(CnATreeElement element,
            boolean excludeMaximumValue) {
        this.element = element;
        this.excludeMaximumValue = excludeMaximumValue;
    }

    @Override
    public IHuiControl createControl(Entity entity, PropertyType propertyType, boolean editable,
            Composite parent, boolean focus, boolean showValidationHint,
            boolean useValidationGuiHints) {
        return new SingleSelectionControl(entity, propertyType, parent, editable,
                showValidationHint, useValidationGuiHints) {
            @Override
            protected List<IMLPropertyOption> getOptions() {
                RiskConfiguration riskConfiguration = RiskMatrixConfigurator
                        .getRiskConfiguration(element);
                List<Frequency> frequencyValues = riskConfiguration
                        .getFrequencies();
                Stream<Frequency> stream = frequencyValues.stream();
                if (excludeMaximumValue) {
                    stream = stream.limit(frequencyValues.size() - 1l);
                }
                return stream.map(item -> new PropertyOption(item.getId(), item.getLabel()))
                        .collect(Collectors.toList());
            }
        };
    }
}