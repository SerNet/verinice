/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 * Contributors:
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.risk;

/**
 * Calculates risk values for given parameters.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public interface RiskCalculator {

    /**
     * Returns the risk for a given business impact of an asset and
     * a given probability of occurrence for a incident scenario.
     * 
     * In this method the risk is calculated by addition.
     * Overwrite this method to implement a different risk
     * calculation.
     * 
     * @param businessImpact A business impact of an asset
     * @param probability The probability of occurrence for a incident scenario
     * @return A risk value for the given parameters
     */
    int calculateRiskFromBusinessImpactAndProbability(int businessImpact, int probability);
}
