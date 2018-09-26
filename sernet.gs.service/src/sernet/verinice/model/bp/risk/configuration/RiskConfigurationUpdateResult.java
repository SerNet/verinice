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
package sernet.verinice.model.bp.risk.configuration;

import java.io.Serializable;

/**
 * This class contains the result of updating a risk configuration
 */
public class RiskConfigurationUpdateResult implements Serializable {

    private static final long serialVersionUID = -7491469845332080130L;

    private int numberOfChangedThreats;
    private int numberOfRemovedFrequencies;
    private int numberOfRemovedImpacts;
    private int numberOfRemovedRisks;

    public RiskConfigurationUpdateResult() {
        super();
    }

    public int getNumberOfChangedThreats() {
        return numberOfChangedThreats;
    }

    public void setNumberOfChangedThreats(int numberOfChangedThreats) {
        this.numberOfChangedThreats = numberOfChangedThreats;
    }

    public int getNumberOfRemovedFrequencies() {
        return numberOfRemovedFrequencies;
    }

    public void setNumberOfRemovedFrequencies(int numberOfRemovedFrequencies) {
        this.numberOfRemovedFrequencies = numberOfRemovedFrequencies;
    }

    public int getNumberOfRemovedImpacts() {
        return numberOfRemovedImpacts;
    }

    public void setNumberOfRemovedImpacts(int numberOfRemovedImpacts) {
        this.numberOfRemovedImpacts = numberOfRemovedImpacts;
    }

    public int getNumberOfRemovedRisks() {
        return numberOfRemovedRisks;
    }

    public void setNumberOfRemovedRisks(int numberOfRemovedRisks) {
        this.numberOfRemovedRisks = numberOfRemovedRisks;
    }

}
