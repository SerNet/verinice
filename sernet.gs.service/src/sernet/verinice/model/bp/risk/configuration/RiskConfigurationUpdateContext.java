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
import java.util.List;

import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;

/**
 * This class contains all objects that are required for updating a risk
 * configuration.
 */
public class RiskConfigurationUpdateContext implements Serializable {

    private static final long serialVersionUID = -779219773426955378L;

    private String uuidItNetwork;
    private transient ItNetwork itNetwork;
    private RiskConfiguration riskConfiguration;

    private List<Frequency> deletedFrequencies;
    private List<Impact> deletedImpacts;
    private List<Risk> deletedRisks;

    public RiskConfigurationUpdateContext(String uuidItNetwork,
            RiskConfiguration riskConfiguration) {
        super();
        this.uuidItNetwork = uuidItNetwork;
        this.riskConfiguration = riskConfiguration;
    }

    public String getUuidItNetwork() {
        return uuidItNetwork;
    }

    public void setUuidItNetwork(String uuidItNetwork) {
        this.uuidItNetwork = uuidItNetwork;
    }

    public RiskConfiguration getRiskConfiguration() {
        return riskConfiguration;
    }

    public void setRiskConfiguration(RiskConfiguration riskConfiguration) {
        this.riskConfiguration = riskConfiguration;
    }

    public List<Frequency> getDeletedFrequencies() {
        return deletedFrequencies;
    }

    public void setDeletedFrequencies(List<Frequency> deletedFrequencies) {
        this.deletedFrequencies = deletedFrequencies;
    }

    public List<Impact> getDeletedImpacts() {
        return deletedImpacts;
    }

    public void setDeletedImpacts(List<Impact> deletedImpacts) {
        this.deletedImpacts = deletedImpacts;
    }

    public List<Risk> getDeletedRisks() {
        return deletedRisks;
    }

    public void setDeletedRisks(List<Risk> deletedRisks) {
        this.deletedRisks = deletedRisks;
    }

    public ItNetwork getItNetwork() {
        return itNetwork;
    }

    public void setItNetwork(ItNetwork itNetwork) {
        this.itNetwork = itNetwork;
    }

}
