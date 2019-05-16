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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import sernet.hui.common.connect.PropertyOption;
import sernet.verinice.model.bp.risk.RiskPropertyValue;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.common.CnATreeElement;

public class DynamicRiskPropertiesControlFactory extends DynamicValuesControlFactory {

    public DynamicRiskPropertiesControlFactory(CnATreeElement element,
            Function<RiskConfiguration, List<? extends RiskPropertyValue>> propertyValuesExtractor) {
        super(() -> propertyValuesExtractor
                .apply(RiskMatrixConfigurator.getRiskConfiguration(element)).stream().map(item -> {
                    if (item == null) {
                        throw new IllegalStateException(
                                "null value found in the risk configuration");
                    }
                    return new PropertyOption(item.getId(), item.getLabel());

                }).collect(Collectors.toList()));
    }
}