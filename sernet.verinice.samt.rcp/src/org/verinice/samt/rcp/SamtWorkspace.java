/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package org.verinice.samt.rcp;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.CnAWorkspace;

/**
 * @author Daniel Murygin <dm@sernet.de> // TODO dm: Externalize Strings
 */
public class SamtWorkspace extends CnAWorkspace {

    private static final Logger LOG = Logger.getLogger(SamtWorkspace.class);

    private static SamtWorkspace instance;

    /**
     * Classloader-relative path to the resources folder of this bundle without
     * a path separator in the beginning and in the end
     */
    public static final String RESOURCES_PATH = "resources";

    /**
     * Workspace-relative path to the verinice conf folder without a path
     * separator in the beginning and in the end
     */
    public static final String CONF_PATH = "conf";

    /**
     * File name of the self-assessment CSV catalog
     */
    public static final String SAMT_CATALOG_FILE_NAME = "samt-catalog.csv";

    private SamtWorkspace() {
        // use getInstance
    }

    public static SamtWorkspace getInstance() {
        if (instance == null) {
            instance = new SamtWorkspace();
        }
        return instance;
    }

    public synchronized void createSelfAssessmemtCatalog() throws NullPointerException, IOException {
        final String inputPath = RESOURCES_PATH + File.separatorChar + SAMT_CATALOG_FILE_NAME;
        final String ouputPath = CONF_PATH + File.separatorChar + SAMT_CATALOG_FILE_NAME;

        try {
            createTextFile(inputPath, getWorkdir(), ouputPath, null);
        } catch (RuntimeException e) {
            LOG.error("Error while saving samt catalog file in conf dir. Input path: " + inputPath + ", output path: " + ouputPath, e);
            throw e;
        } catch (Exception e) {
            LOG.error("Error while saving samt catalog file in conf dir. Input path: " + inputPath + ", output path: " + ouputPath, e);
            throw new RuntimeException(e);
        }
    }
}
