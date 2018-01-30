/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin <dm{a}sernet{dot}de>.
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
 * Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.File;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.verinice.interfaces.IBSIConfig;
import sernet.verinice.service.parser.BSIConfigurationStandalone;

/**
 * Factory class to create IBSIConfig instances.
 * This factory must only be used on the client.
 * 
 * Do no instantiate this class use public static methods.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public final class BSIConfigFactory {

    private BSIConfigFactory() {
    }

    /**
     * @return A IBSIConfig for operating mode standalone
     */
    public static IBSIConfig createStandaloneConfig() {
        String bpCatalogFilePath = sernet.verinice.rcp.Preferences.getBpCatalogFilePath();
        String privacyCatalogFilePath = sernet.verinice.rcp.Preferences.getPrivacyCatalogFilePath();
        boolean fromZipFile = sernet.verinice.rcp.Preferences.isBpCatalogLoadedFromZipFile();
        String cacheDir = CnAWorkspace.getInstance().getWorkdir() + File.separator + "gscache";
        return new BSIConfigurationStandalone(bpCatalogFilePath, privacyCatalogFilePath, fromZipFile,
                cacheDir);
    }

}
