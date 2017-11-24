/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman.
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
 *     Alexander Koderman - initial API and implementation
 *     Daniel Murygin - Refactoring
 ******************************************************************************/
package sernet.gs.ui.rcp.main;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import sernet.gs.service.SecurityException;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * Helper class to handle and display exceptions.
 * 
 * @author Alexander Koderman
 * @author Daniel Murygin
 */
public final class ExceptionUtil {

    private static final Logger LOG = Logger.getLogger(ExceptionUtil.class);

    private ExceptionUtil() {
    }

    @SuppressWarnings({ "restriction", "deprecation" })
    public static void log(Throwable e, final String exceptionTitle) {
        // log the error with log4j
        LOG.error(exceptionTitle, e);

        if (e instanceof SecurityException || e.getCause() instanceof SecurityException) {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(),
                            Messages.ExceptionUtil_2, Messages.ExceptionUtil_3);
                }
            });
            return;
        }

        if (Activator.getDefault().getPluginPreferences()
                .getBoolean(PreferenceConstants.ERRORPOPUPS)) {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), exceptionTitle,
                            "An error has occurred. Information about the cause of the error can be found in the log files.");
                }
            });
        }
    }

}
