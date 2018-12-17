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
package sernet.verinice.service.bp.risk;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateContext;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateResult;

/**
 * A service to run and configure an IT base protection (ITBP) risk analysis.
 */
public interface RiskService {

    /**
     * Updates the risk configuration of an IT network.
     * 
     * @param updateContext
     *            Context which contains all objects that are required for
     *            updating
     * @return The result of the update
     */
    public RiskConfigurationUpdateResult updateRiskConfiguration(
            RiskConfigurationUpdateContext updateContext);

    /**
     * Return the risk configuration for a given IT network's ID. If there is no
     * explicit configuration for the given network, <code>null</code> is
     * returned.
     */
    RiskConfiguration findRiskConfiguration(Integer itNetworkID) throws CommandException;
}
