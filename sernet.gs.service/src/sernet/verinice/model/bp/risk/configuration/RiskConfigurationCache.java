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

import java.util.HashMap;
import java.util.Map;

/**
 * Simple short-lived cache for risk configurations. There is no invalidation
 * done and the class is *not* thread-safe.
 */
public class RiskConfigurationCache {

    private final Map<Integer, RiskConfiguration> cache = new HashMap<>();

    public RiskConfiguration findRiskConfiguration(Integer scopeId) {
        return cache.get(scopeId);
    }

    public void putRiskConfiguration(Integer scopeId, RiskConfiguration riskConfiguration) {
        cache.put(scopeId, riskConfiguration);
    }

}
