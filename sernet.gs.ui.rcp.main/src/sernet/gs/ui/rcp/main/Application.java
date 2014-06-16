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
package sernet.gs.ui.rcp.main;

import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.impl.util.Log;

/**
 * This class controls all aspects of the application's execution
 */
@SuppressWarnings("restriction")
public class Application implements IApplication {

    private static final String USER_CONFIG_PROPERTY_KEY = "logging.file";
    private static final String DEFAULT_VERINICE_LOG = "log/verinice.log";
    private static final String WORKSPACE_PROPERTY_KEY = "osgi.instance.area";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
     * IApplicationContext)
     */
    public Object start(IApplicationContext context) throws Exception {

        setLog4jFilePathSystemEnvValue();
        ConfigurationLogger.logStart();

        Activator.getDefault().startApplication();
        Activator.inheritVeriniceContextState();

        Display display = PlatformUI.createDisplay();
        try {
            int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return IApplication.EXIT_RESTART;
            }
            return IApplication.EXIT_OK;
        } finally {
            ConfigurationLogger.logStop();
            display.dispose();
        }
    }

    private void setLog4jFilePathSystemEnvValue() throws IOException {
        String p = getLoggingPath();
        p = replaceInvalidSuffix(p);
        configureFileAppender(p);
    }

    private void configureFileAppender(String p) {

        Logger log = Logger.getRootLogger();
        FileAppender fileAppender = (FileAppender) log.getAppender("FILE");
        fileAppender.setFile(p);
        fileAppender.activateOptions(); // without this call, the changes does
                                        // have no effect
        log.addAppender(fileAppender);
    }

    private String getLoggingPath() {

        String p = readFromVeriniceIniFile();

        if (p == null) {
            p = System.getProperty(WORKSPACE_PROPERTY_KEY);
            return p + DEFAULT_VERINICE_LOG;
        }

        return p;
    }

    private String readFromVeriniceIniFile() {
        return System.getProperty(USER_CONFIG_PROPERTY_KEY);
    }

    private String replaceInvalidSuffix(String path) {
        if (path.startsWith("file:/")) {
            path = path.replaceFirst("file:", "");
        }

        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.equinox.app.IApplication#stop()
     */
    public void stop() {
        // nothing to do
    }

}
