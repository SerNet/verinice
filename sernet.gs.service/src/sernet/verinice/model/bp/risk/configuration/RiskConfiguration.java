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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.RiskPropertyValue;

public class RiskConfiguration implements Serializable {

    private static final long serialVersionUID = 8715506771760384220L;
    private static final Logger logger = Logger.getLogger(RiskConfiguration.class);

    private final List<Frequency> frequencies;
    private final List<Impact> impacts;
    private final List<Risk> risks;

    /**
     * A two-dimensional array that stores risk indices, the first dimension is
     * the impact, the second the frequency
     *
     * We need to use Integer[][] since our tomcat6 setup in combination with
     * spring magic throws a ClassNotFoundException "[[I" on (de)serialization.
     */
    private final Integer[][] configuration;

    /**
     * @param configuration
     *            A two-dimensional array that stores risk indices, the first
     *            dimension is the impact, the second the frequency
     */
    public RiskConfiguration(List<Frequency> frequencyValues, List<Impact> impactValues,
            List<Risk> risks, Integer[][] configuration) {

        validateParameter(frequencyValues, impactValues, risks, configuration);

        // wrap the passes list to make sure they are serializable.
        this.frequencies = Collections.unmodifiableList(new ArrayList<>(frequencyValues));
        this.impacts = Collections.unmodifiableList(new ArrayList<>(impactValues));
        this.risks = Collections.unmodifiableList(new ArrayList<>(risks));

        int numberOfRiskValues = risks.size();

        this.configuration = new Integer[impactValues.size()][frequencyValues.size()];

        for (int impactIndex = 0; impactIndex < configuration.length; impactIndex++) {

            Integer[] configurationForImpact = configuration[impactIndex];
            for (int frequencyIndex = 0; frequencyIndex < configurationForImpact.length; frequencyIndex++) {
                int riskIndex = configuration[impactIndex][frequencyIndex];
                if (riskIndex > numberOfRiskValues) {
                    throw new IllegalArgumentException("Risk value for index [" + impactIndex + "]["
                            + frequencyIndex + "] is invalid, value is " + riskIndex
                            + " but configuration contains only " + numberOfRiskValues + " values");
                }
                this.configuration[impactIndex][frequencyIndex] = riskIndex;
            }

        }
    }

    protected void validateParameter(List<Frequency> frequencyValues, List<Impact> impactValues,
            List<Risk> risks, Integer[][] configuration) {
        int numberOfImpactValues = impactValues.size();
        int numberOfFrequencyValues = frequencyValues.size();
        int numberOfRiskValues = risks.size();

        if (numberOfImpactValues == 0) {
            throw new IllegalArgumentException("Configuration contains no values for impact");
        }
        if (numberOfFrequencyValues == 0) {
            throw new IllegalArgumentException("Configuration contains no values for frequency");
        }
        if (numberOfRiskValues == 0) {
            throw new IllegalArgumentException("Configuration contains no risks");
        }
        if (configuration.length != numberOfImpactValues) {
            throw new IllegalArgumentException(
                    "Risk matrix size does not match number values for impact, expecting "
                            + numberOfImpactValues + " but got " + configuration.length);
        }
        for (int impactIndex = 0; impactIndex < configuration.length; impactIndex++) {
            if (configuration[impactIndex].length != numberOfFrequencyValues) {
                throw new IllegalArgumentException(
                        "Risk matrix size does not match number values for frequencies, expecting "
                                + numberOfFrequencyValues + " but got "
                                + configuration[impactIndex].length);
            }
        }
    }

    public List<Frequency> getFrequencies() {
        return frequencies;
    }

    public List<Impact> getImpacts() {
        return impacts;
    }

    public List<Risk> getRisks() {
        return risks;
    }

    public Risk getRisk(Frequency frequency, Impact impact) {
        int frequencyIndex = getFrequencyIndex(frequency);
        int impactIndex = getImpactIndex(impact);
        int riskIndex = configuration[impactIndex][frequencyIndex];
        if (riskIndex == -1) {
            return null;
        }
        return risks.get(riskIndex);

    }

    public Risk getRisk(String frequencyId, String impactId) {
        int frequencyIndex = getFrequencyIndex(
                frequencies.stream().filter(frequency -> frequency.getId().equals(frequencyId))
                        .findFirst().orElse(null));
        int impactIndex = getImpactIndex(impacts.stream()
                .filter(impact -> impact.getId().equals(impactId)).findFirst().orElse(null));
        int riskIndex = configuration[impactIndex][frequencyIndex];
        if (riskIndex == -1) {
            return null;
        }
        return risks.get(riskIndex);

    }

    public RiskConfiguration withRisk(Frequency frequency, Impact impact, Risk risk) {
        int frequencyIndex = getFrequencyIndex(frequency);
        int impactIndex = getImpactIndex(impact);
        Integer[][] newConfiguration = clone(configuration);

        if (risk == null) {
            newConfiguration[impactIndex][frequencyIndex] = -1;
        } else {
            int riskIndex = risks.indexOf(risk);
            if (riskIndex == -1) {
                throw new IllegalArgumentException("Unknown risk category: " + risk);
            }
            newConfiguration[impactIndex][frequencyIndex] = riskIndex;
        }
        return new RiskConfiguration(frequencies, impacts, risks, newConfiguration);
    }

    public @NonNull RiskConfiguration withValues(List<Frequency> frequencies, List<Impact> impacts,
            List<Risk> riskCategories) {
        Integer[][] newConfiguration = configurationWithSize(impacts.size(), frequencies.size(),
                riskCategories.size());
        return new RiskConfiguration(frequencies, impacts, riskCategories, newConfiguration);
    }

    /**
     * Create a new configuration matrix out of the given dimensions and of the
     * given configuration. If a dimension of the new matrix is greater than the
     * corresponding dimension of the given matrix, new entries are set to -1.
     */
    private Integer[][] configurationWithSize(int nImpacts, int nFrequencies, int nRisks) {
        Integer[][] newConfiguration = newMatrix(nImpacts, nFrequencies, -1);

        for (int impactIndex = 0; impactIndex < Math.min(configuration.length,
                newConfiguration.length); impactIndex++) {
            for (int frequencyIndex = 0; frequencyIndex < Math.min(configuration[0].length,
                    newConfiguration[0].length); frequencyIndex++) {
                int riskValue = configuration[impactIndex][frequencyIndex];
                if (riskValue < nRisks) {
                    newConfiguration[impactIndex][frequencyIndex] = riskValue;
                }
            }
        }
        return newConfiguration;
    }

    /**
     * Creates a new Integer[rows][cols] with each value set to `initialValue`.
     */
    private Integer[][] newMatrix(int rows, int cols, Integer initialValue) {
        Integer[][] newConfiguration = new Integer[rows][cols];
        for (int i = 0; i < newConfiguration.length; i++) {
            for (int j = 0; j < newConfiguration[0].length; j++) {
                newConfiguration[i][j] = initialValue;
            }
        }
        return newConfiguration;
    }

    private static Integer[][] clone(Integer[][] configuration) {
        Integer[][] result = configuration.clone();
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].clone();
        }
        return result;
    }

    private int getImpactIndex(Impact impact) {
        int impactIndex = impacts.indexOf(impact);

        if (impactIndex == -1) {
            throw new IllegalArgumentException("Unknown impact: " + impact);
        }
        return impactIndex;
    }

    private int getFrequencyIndex(Frequency frequency) {
        int frequencyIndex = frequencies.indexOf(frequency);
        if (frequencyIndex == -1) {
            throw new IllegalArgumentException("Unknown frequency: " + frequency);
        }
        return frequencyIndex;
    }

    @Override
    public String toString() {
        return "RiskConfiguration [risks=" + risks + ", frequencies=" + frequencies + ", impacts="
                + impacts + "]";
    }

    public boolean deepEquals(Object obj) {
        logger.debug("deepEquals");
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        RiskConfiguration other = (RiskConfiguration) obj;
        if (!Arrays.deepEquals(configuration, other.configuration))
            return false;
        if (frequencies == null) {
            if (other.frequencies != null)
                return false;
        } else if (!Frequency.deepEquals(frequencies, other.frequencies))
            return false;
        if (impacts == null) {
            if (other.impacts != null)
                return false;
        } else if (!Impact.deepEquals(impacts, other.impacts))
            return false;
        if (risks == null) {
            if (other.risks != null)
                return false;
        } else if (!Risk.deepEquals(risks, other.risks))
            return false;
        return true;
    }

    public String getLabelForFrequency(String frequencyId) {
        return getLabelForValue(frequencies, frequencyId);
    }

    public String getLabelForImpact(String impactId) {
        return getLabelForValue(impacts, impactId);
    }

    public String getLabelForRisk(String riskId) {
        return getLabelForValue(risks, riskId);
    }

    private String getLabelForValue(List<? extends RiskPropertyValue> riskPropertyValues,
            String valueId) {
        return riskPropertyValues.stream().filter(item -> item.getId().equals(valueId)).findFirst()
                .orElseThrow(IllegalArgumentException::new).getLabel();
    }
}
