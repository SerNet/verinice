/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.logging;

import java.io.File;

public interface LogDirectoryProvider {
    
    public static final String LOG4J_CONFIGURATION_JVM_ENV_KEY = "log4j.configuration";
    public static final String LOGGING_PATH_KEY = "logging.file";
    public static final String LOG_FOLDER = "log" + File.separator;
    public static final String DEFAULT_VERINICE_LOG = "verinice-client.log";
    public static final String WORKSPACE_PROPERTY_KEY = "osgi.instance.area";
    
    public String getLogDirectory();
}
