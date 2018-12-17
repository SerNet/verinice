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
import java.util.function.Function;

import org.apache.log4j.Logger;

import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;
import sernet.verinice.model.bp.risk.Risk.Color;

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
    public RiskConfiguration(List<Frequency> frequencyValues,
            List<Impact> impactValues, List<Risk> risks,
            Integer[][] configuration) {

        validateParameter(frequencyValues, impactValues, risks, configuration);

        // wrap the passes list to make sure they are serializable.
        this.frequencies = Collections.unmodifiableList(new ArrayList<>(frequencyValues));
        this.impacts = Collections.unmodifiableList(new ArrayList<>(impactValues));
        this.risks = Collections.unmodifiableList(new ArrayList<>(risks));

        int numberOfFrequencyValues = frequencyValues.size();
        int numberOfRiskValues = risks.size();

        this.configuration = new Integer[impactValues.size()][frequencyValues.size()];

        for (int impactIndex = 0; impactIndex < configuration.length; impactIndex++) {

            Integer[] configurationForImpact = configuration[impactIndex];
            if (configurationForImpact.length != numberOfFrequencyValues) {
                throw new IllegalArgumentException(
                        "Risk matrix size does not match number values for frequency");
            }
            for (int frequencyIndex = 0; frequencyIndex < configurationForImpact.length; frequencyIndex++) {
                int riskIndex = configuration[impactIndex][frequencyIndex];
                if (riskIndex > numberOfRiskValues) {
                    throw new IllegalArgumentException("Risk value for index [" + impactIndex + "]["
                            + frequencyIndex + "] is invalid, value is " + riskIndex
                            + " but configuration contains only " + numberOfRiskValues
                            + " values");
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

    public RiskConfiguration withRisk(Frequency frequency,
            Impact impact, Risk risk) {
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
        return new RiskConfiguration(frequencies, impacts, risks,
                newConfiguration);
    }

    public RiskConfiguration withRiskColor(Risk risk, Color newColor) {
        return withModifiedRisk(risk,
                oldCategory -> new Risk(oldCategory.getId(), oldCategory.getLabel(),
                        oldCategory.getDescription(), newColor));
    }

    public RiskConfiguration withRiskLabel(Risk risk, String newLabel) {
        return withModifiedRisk(risk,
                oldCategory -> new Risk(oldCategory.getId(), newLabel,
                        oldCategory.getDescription(), oldCategory.getColor()));
    }

    public RiskConfiguration withRiskDescription(Risk risk, String newDescription) {
        return withModifiedRisk(risk,
                oldCategory -> new Risk(oldCategory.getId(), oldCategory.getLabel(),
                        newDescription, oldCategory.getColor()));
    }

    public RiskConfiguration withModifiedRisk(Risk risk,
            Function<Risk, Risk> newElementCreator) {
        int riskIndex = risks.indexOf(risk);
        if (riskIndex == -1) {
            throw new IllegalArgumentException("Unknown risk category: " + risk);
        }
        List<Risk> newRisks = new ArrayList<>(risks);
        Risk oldRisk = risks.get(riskIndex);
        Risk newRisk = newElementCreator.apply(oldRisk);
        newRisks.set(riskIndex, newRisk);
        return new RiskConfiguration(frequencies, impacts, newRisks,
                configuration);
    }

    public RiskConfiguration withFrequencyLabel(
            Frequency frequency, String newLabel) {
        return withModifiedFrequency(frequency,
                oldValue -> new Frequency(oldValue.getId(), newLabel,
                        oldValue.getDescription()));
    }

    public RiskConfiguration withFrequencyDescription(
            Frequency frequency, String newDescription) {
        return withModifiedFrequency(frequency,
                oldValue -> new Frequency(oldValue.getId(), oldValue.getLabel(),
                        newDescription));
    }

    public RiskConfiguration withModifiedFrequency(
            Frequency frequency,
            Function<Frequency, Frequency> newElementCreator) {
        int index = getFrequencyIndex(frequency);
        List<Frequency> newFrequencies = new ArrayList<>(frequencies);
        Frequency oldValue = frequencies.get(index);
        Frequency newValue = newElementCreator.apply(oldValue);
        newFrequencies.set(index, newValue);
        return new RiskConfiguration(newFrequencies, impacts, risks,
                configuration);
    }

    public RiskConfiguration withImpactLabel(Impact impact,
            String newLabel) {
        return withModifiedImpact(impact,
                oldValue -> new Impact(oldValue.getId(), newLabel,
                        oldValue.getDescription()));
    }

    public RiskConfiguration withImpactDescription(Impact impact,
            String newDescription) {
        return withModifiedImpact(impact,
                oldValue -> new Impact(oldValue.getId(), oldValue.getLabel(),
                        newDescription));
    }

    public RiskConfiguration withModifiedImpact(Impact impact,
            Function<Impact, Impact> newElementCreator) {
        int index = impacts.indexOf(impact);
        if (index == -1) {
            throw new IllegalArgumentException("Unknown impact: " + impact);
        }
        List<Impact> newImpacts = new ArrayList<>(impacts);
        Impact oldValue = impacts.get(index);
        Impact newValue = newElementCreator.apply(oldValue);
        newImpacts.set(index, newValue);
        return new RiskConfiguration(frequencies, newImpacts, risks, configuration);
    }

    public RiskConfiguration withLastRiskRemoved() {
        if (risks.size() < 2) {
            throw new UnsupportedOperationException("Cannot remove last entry");
        }
        List<Risk> newRisks = new ArrayList<>(risks.subList(0, risks.size() - 1));
        Integer[][] newConfiguration = new Integer[impacts.size()][frequencies.size()];

        for (int impactIndex = 0; impactIndex < configuration.length; impactIndex++) {

            Integer[] configurationForImpact = configuration[impactIndex];
            for (int frequencyIndex = 0; frequencyIndex < configurationForImpact.length; frequencyIndex++) {
                int riskValue = configuration[impactIndex][frequencyIndex];
                if (riskValue >= newRisks.size()) {
                    riskValue = -1;
                }
                newConfiguration[impactIndex][frequencyIndex] = riskValue;
            }

        }
        return new RiskConfiguration(frequencies, impacts, newRisks,
                newConfiguration);
    }

    public RiskConfiguration withRiskAdded() {
        List<Risk> newRisks = new ArrayList<>(risks.size() + 1);
        newRisks.addAll(risks);
        newRisks.add(
                new Risk(Risk.getPropertyKeyForIndex(newRisks.size() + 1), "", "", null));
        return new RiskConfiguration(frequencies, impacts, newRisks,
                configuration);
    }

    public RiskConfiguration withLastFrequencyRemoved() {
        if (frequencies.size() < 2) {
            throw new UnsupportedOperationException("Cannot remove last entry");
        }
        List<Frequency> newFrequencies = new ArrayList<>(
                frequencies.subList(0, frequencies.size() - 1));
        Integer[][] newConfiguration = new Integer[impacts.size()][newFrequencies.size()];

        for (int impactIndex = 0; impactIndex < newConfiguration.length; impactIndex++) {

            Integer[] configurationForImpact = newConfiguration[impactIndex];
            for (int frequencyIndex = 0; frequencyIndex < configurationForImpact.length; frequencyIndex++) {
                int riskValue = configuration[impactIndex][frequencyIndex];
                newConfiguration[impactIndex][frequencyIndex] = riskValue;
            }

        }
        return new RiskConfiguration(newFrequencies, impacts, risks, newConfiguration);
    }

    public RiskConfiguration withFrequencyAdded() {
        List<Frequency> newFrequencies = new ArrayList<>(frequencies.size() + 1);
        newFrequencies.addAll(frequencies);
        newFrequencies
                .add(new Frequency(Frequency.getPropertyKeyForIndex(newFrequencies.size() + 1), "",
                        ""));

        Integer[][] newConfiguration = new Integer[impacts.size()][newFrequencies.size()];

        for (int impactIndex = 0; impactIndex < newConfiguration.length; impactIndex++) {

            Integer[] configurationForImpact = newConfiguration[impactIndex];
            for (int frequencyIndex = 0; frequencyIndex < configurationForImpact.length; frequencyIndex++) {
                int riskValue;
                if (frequencyIndex >= frequencies.size()) {
                    riskValue = -1;
                } else {
                    riskValue = configuration[impactIndex][frequencyIndex];
                }
                newConfiguration[impactIndex][frequencyIndex] = riskValue;
            }

        }

        return new RiskConfiguration(newFrequencies, impacts, risks,
                newConfiguration);
    }

    public RiskConfiguration withLastImpactRemoved() {
        if (impacts.size() < 2) {
            throw new UnsupportedOperationException("Cannot remove last entry");
        }
        List<Impact> newImpacts = new ArrayList<>(impacts.subList(0, impacts.size() - 1));
        Integer[][] newConfiguration = new Integer[newImpacts.size()][frequencies.size()];

        for (int impactIndex = 0; impactIndex < newConfiguration.length; impactIndex++) {

            Integer[] configurationForImpact = newConfiguration[impactIndex];
            for (int frequencyIndex = 0; frequencyIndex < configurationForImpact.length; frequencyIndex++) {
                int riskValue = configuration[impactIndex][frequencyIndex];
                newConfiguration[impactIndex][frequencyIndex] = riskValue;
            }

        }
        return new RiskConfiguration(frequencies, newImpacts, risks, newConfiguration);
    }

    public RiskConfiguration withImpactAdded() {
        List<Impact> newImpacts = new ArrayList<>(impacts.size() + 1);
        newImpacts.addAll(impacts);
        newImpacts.add(new Impact(Impact.getPropertyKeyForIndex(newImpacts.size() + 1), "", ""));

        Integer[][] newConfiguration = new Integer[newImpacts.size()][frequencies.size()];

        for (int impactIndex = 0; impactIndex < configuration.length; impactIndex++) {
            Integer[] configurationForImpact = newConfiguration[impactIndex];
            for (int frequencyIndex = 0; frequencyIndex < configurationForImpact.length; frequencyIndex++) {
                int riskValue = configuration[impactIndex][frequencyIndex];
                newConfiguration[impactIndex][frequencyIndex] = riskValue;
            }
        }
        Arrays.fill(newConfiguration[newImpacts.size() - 1], -1);

        return new RiskConfiguration(frequencies, newImpacts, risks, newConfiguration);
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
        return "RiskConfiguration [risks=" + risks + ", frequencies="
                + frequencies + ", impacts=" + impacts + "]";
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
}
