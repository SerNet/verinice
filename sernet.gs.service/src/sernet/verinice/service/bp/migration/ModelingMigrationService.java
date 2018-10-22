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
package sernet.verinice.service.bp.migration;

/**
 * This service migrates the modeling of an IT network from version 1.16 to
 * 1.17.
 */
public interface ModelingMigrationService {

    /**
     * Return true if the migration of modeling is required
     */
    public boolean isMigrationRequired();

    /**
     * Migrates modeling in the IT network with the given database ID
     * 
     * @param itNetworkDbId
     *            The database ID of an IT network
     */
    public void migrateModelingOfItNetwork(Integer itNetworkDbId);

}
