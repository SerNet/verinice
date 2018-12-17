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
package sernet.verinice.model.bp.risk.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sernet.verinice.model.bp.risk.DefaultFrequency;
import sernet.verinice.model.bp.risk.DefaultImpact;
import sernet.verinice.model.bp.risk.DefaultRisk;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.Risk.Color;

public class DefaultRiskConfiguration extends RiskConfiguration {

    static {
        List<Frequency> defaultFrequencyValues = new ArrayList<>();
        defaultFrequencyValues.add(new DefaultFrequency(Frequency.getPropertyKeyForIndex(1)));
        defaultFrequencyValues.add(new DefaultFrequency(Frequency.getPropertyKeyForIndex(2)));
        defaultFrequencyValues.add(new DefaultFrequency(Frequency.getPropertyKeyForIndex(3)));
        defaultFrequencyValues.add(new DefaultFrequency(Frequency.getPropertyKeyForIndex(4)));

        List<Impact> defaultImpactValues = new ArrayList<>();
        defaultImpactValues.add(new DefaultImpact(Impact.getPropertyKeyForIndex(1)));
        defaultImpactValues.add(new DefaultImpact(Impact.getPropertyKeyForIndex(2)));
        defaultImpactValues.add(new DefaultImpact(Impact.getPropertyKeyForIndex(3)));
        defaultImpactValues.add(new DefaultImpact(Impact.getPropertyKeyForIndex(4)));

        List<Risk> defaultRisks = new ArrayList<>(4);
        defaultRisks
                .add(new DefaultRisk(Risk.getPropertyKeyForIndex(1), new Color(160, 207, 17)));
        defaultRisks
                .add(new DefaultRisk(Risk.getPropertyKeyForIndex(2), new Color(255, 255, 19)));
        defaultRisks
                .add(new DefaultRisk(Risk.getPropertyKeyForIndex(3), new Color(255, 142, 67)));
        defaultRisks.add(new DefaultRisk(Risk.getPropertyKeyForIndex(4), new Color(255, 18, 18)));

        Integer[][] defaultConfiguration = new Integer[][] { new Integer[] { 0, 0, 0, 0 },
                new Integer[] { 0, 0, 1, 2 }, new Integer[] { 1, 1, 2, 3 },
                new Integer[] { 1, 2, 3, 3 } };

        INSTANCE = new DefaultRiskConfiguration(
                Collections.unmodifiableList(defaultFrequencyValues),
                Collections.unmodifiableList(defaultImpactValues),
                Collections.unmodifiableList(defaultRisks), defaultConfiguration);
    }

    private DefaultRiskConfiguration(List<Frequency> frequencyValues,
            List<Impact> impactValues, List<Risk> risks,
            Integer[][] configuration) {
        super(frequencyValues, impactValues, risks, configuration);
    }

    private static final DefaultRiskConfiguration INSTANCE;

    public static DefaultRiskConfiguration getInstance() {
        return INSTANCE;
    }

}
