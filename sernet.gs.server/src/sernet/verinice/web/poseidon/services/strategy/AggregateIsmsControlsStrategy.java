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

import java.util.Map;

import sernet.verinice.model.iso27k.ControlGroup;

/**
 * Aggregate data over Isms catalogs.
 *
 * All controls which belongs to a catalog are children of a
 * {@link ControlGroup} which is tagged as a catalog.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface AggregateIsmsControlsStrategy {

    /**
     * Calculates implementation status of ISMS controls.
     *
     * @param veriniceGraph
     *            Contains all necessary controls.
     * @param catalogId
     *            The database id of the root elment of the catalog.
     * @return A map which uses implementation status as key. The keys can be
     *         lookup in the SNCA.xml.
     */
    Map<String, Number> getData();
}
