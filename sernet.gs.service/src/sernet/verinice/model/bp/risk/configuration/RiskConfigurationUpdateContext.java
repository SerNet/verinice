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

import sernet.verinice.model.bp.risk.Frequency;
import sernet.verinice.model.bp.risk.Impact;
import sernet.verinice.model.bp.risk.Risk;

/**
 * This class contains all objects that are required for updating a risk
 * configuration.
 */
public class RiskConfigurationUpdateContext implements Serializable {

    private static final long serialVersionUID = 7482643391068945935L;
    private final String uuidItNetwork;
    private final RiskConfiguration riskConfiguration;

    private final List<Frequency> deletedFrequencies;
    private final List<Impact> deletedImpacts;
    private final List<Risk> deletedRisks;

    public RiskConfigurationUpdateContext(String uuidItNetwork, RiskConfiguration riskConfiguration,
            List<Frequency> deletedFrequencies, List<Impact> deletedImpacts,
            List<Risk> deletedRisks) {
        this.uuidItNetwork = uuidItNetwork;
        this.riskConfiguration = riskConfiguration;
        this.deletedFrequencies = deletedFrequencies;
        this.deletedImpacts = deletedImpacts;
        this.deletedRisks = deletedRisks;
    }

    public String getUuidItNetwork() {
        return uuidItNetwork;
    }

    public RiskConfiguration getRiskConfiguration() {
        return riskConfiguration;
    }

    public List<Frequency> getDeletedFrequencies() {
        return deletedFrequencies;
    }

    public List<Impact> getDeletedImpacts() {
        return deletedImpacts;
    }

    public List<Risk> getDeletedRisks() {
        return deletedRisks;
    }

}
