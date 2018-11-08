/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin
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
package sernet.verinice.service.bp.migration;

import java.util.Collection;

import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Migrates the requirements, safeguards and threats of an element. See
 * ModelingMigrationServiceImpl for more details.
 */
public interface MigrateElementJob {

    /**
     * Performs the migration. Before the call, the element and the graph must
     * be set with the setters.
     * 
     * @param element
     *            An element for migration
     * @param veriniceGraph
     *            A graph containing all elements that are to be migrated
     */
    void migrateModeling(CnATreeElement element, VeriniceGraph veriniceGraph);

    /**
     * Returns the items that can be deleted after migration.
     */
    Collection<CnATreeElement> getElementsToDelete();

}