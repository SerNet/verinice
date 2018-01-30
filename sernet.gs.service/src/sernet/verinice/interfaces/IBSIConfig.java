/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 * Alexander Koderman - initial API and implementation
 * Daniel Murygin
 ******************************************************************************/
package sernet.verinice.interfaces;

/**
 * Configuration for BSIMassnahmenModel to load IT baseline protection (ITBP)
 * catalogs.
 *
 * @author Alexander Koderman
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public interface IBSIConfig {

    /**
     * @return The path to ITBP catalog file
     */
	String getGsPath();

    /**
     * @return The path to the privacy catalog file
     */
	String getDsPath();

    /**
     * @return True if the ITBP catalog is a ZIP file
     */
	boolean isFromZipFile();

    /**
     * @return The path to the cache directory
     */
	String  getCacheDir();

}
