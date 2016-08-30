/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
package sernet.verinice.service.linktable;

import java.util.List;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 * Creates from a given {@link VeriniceGraph} and
 * {@link ILinkTableConfiguration} a table.
 * 
 *
 * <p>
 * The {@link ILinkTableConfiguration} contains the actual query, which is more
 * or less composed description of paths through the verinice object tree:
 * </p>
 *
 * <pre>
 *
 * "columnPaths": [
 *     "assetgroup.assetgroup_name AS Titel",
 *     "assetgroup>asset.asset_name AS Titel",
 *    "assetgroup>assetgroup>asset.asset_name AS Titel"
 *   ]
 * </pre>
 *
 * <p>
 * The structure of the verinice data are as follows:
 * </p>
 *
 * <pre>
 *  Organization
 *       |________Asset Group 1
 *                     |____  Asset 1
 *                     |____  Asset 2
 *                     |_____ Asset Group 2
 *                                 |___ Asset 3
 *                                 |___ Asset 4
 * </pre>
 *
 * <p>
 * And this produces the following result.
 * </p>
 *
 * <pre>
 * "Titel"          "Titel"    "Titel"
 * "Asset Group 1"  ""         "Asset 3"
 * "Asset Group 1"  ""         "Asset 4"
 * "Asset Group 1"  "Asset 1"  ""
 * "Asset Group 1"  "Asset 2"  ""
 * "Asset Group 2"  "Asset 3"  ""
 * "Asset Group 2"  "Asset 4"  ""
 * </pre>
 *
 * <p>
 * There some assumptions you can make about the output:
 * <ul>
 * <li>The columns are sorted by the order of paths in the query.</li>
 * <li>The rows are sorted by the first column with the
 * {@link NumericStringComparator}, then the by second column and so on.</li>
 * </p>
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface LinkedTableCreator {

    /**
     * Creates table which contains all column pathes for a given
     * {@link VeriniceGraph}.
     *
     * @param veriniceGraph
     *            The graph the table is created for.
     * @param conf
     *            The configuration. Contains the column pathes, which are the
     *            header of the table.
     * @return The table is represented as a list of rows. Every row is a list
     *         of strings.
     */
    List<List<String>> createTable(VeriniceGraph veriniceGraph, ILinkTableConfiguration conf);
}
