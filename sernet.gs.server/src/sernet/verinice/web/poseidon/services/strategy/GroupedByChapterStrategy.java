/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web.poseidon.services.strategy;

import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 *
 * Support different strategies to calculate {@link MassnahmenUmsetzung} charts.
 *
 * Default strategy is {@link GroupByStrategySum}.
 *
 * @see GroupByStrategy
 * @see GroupByStrategyNormalized
 * @see GroupByStrategySum
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class GroupedByChapterStrategy {

    private String crunchStrategy;

    public GroupedByChapterStrategy(String strategy) {
        this.crunchStrategy = strategy;
    }

    public GroupedByChapterStrategy() {
        this.crunchStrategy = GroupByStrategySum.GET_PARAM_IDENTIFIER;
    }

    public String getCrunchStrategy() {
        return crunchStrategy;
    }

    public void setCrunchStrategy(String crunchStrategy) {
        this.crunchStrategy = crunchStrategy;
    }

    public GroupByStrategy getStrategy() {
        if (GroupByStrategySum.GET_PARAM_IDENTIFIER.equals(crunchStrategy)) {
            return new GroupByStrategySum();
        } else if (GroupByStrategyNormalized.GET_PARAM_IDENTIFIER.equals(crunchStrategy)) {
            return new GroupByStrategyNormalized();
        } else {
            return new GroupByStrategySum();
        }
    }
}
