/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.parser;

import java.io.Serializable;

import sernet.verinice.interfaces.IBSIConfig;

/**
 * A IBSIConfig for operating mode standalone.
 * On the client you can use sernet.gs.ui.rcp.main.bsi.model.BSIConfigFactory
 * to create instances of this class.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class BSIConfigurationStandalone implements IBSIConfig, Serializable {
	
    private static final long serialVersionUID = 4369805673754690041L;

    private String bpCatalogFilePath;
	private String privacyCatalogFilePath;
	private boolean fromZipFile;
	private String cacheDir;

    public BSIConfigurationStandalone(String gsPath, String dsPath, boolean fromZipFile,
            String cacheDir) {
        super();
        this.bpCatalogFilePath = gsPath;
        this.privacyCatalogFilePath = dsPath;
        this.fromZipFile = fromZipFile;
        this.cacheDir = cacheDir;
    }   

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IBSIConfig#getDsPath()
     */
    @Override
    public String getDsPath() {
		return privacyCatalogFilePath;
	}

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IBSIConfig#getGsPath()
     */
    @Override
    public String getGsPath() {
		return bpCatalogFilePath;
	}

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IBSIConfig#isFromZipFile()
     */
    @Override
	public boolean isFromZipFile() {
		return fromZipFile;
	}

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IBSIConfig#getCacheDir()
     */
    @Override
	public String getCacheDir() {
		return cacheDir;
	}

}
