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
 * Daniel Murygin - Refactoring
 ******************************************************************************/
package sernet.gs.ui.rcp.main;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import sernet.gs.service.SecurityException;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.rcp.Preferences;

/**
 * Helper class to handle and display exceptions.
 * 
 * @author Alexander Koderman
 * @author Daniel Murygin
 */
@SuppressWarnings("restriction")
public final class ExceptionUtil {

    private static final Logger LOG = Logger.getLogger(ExceptionUtil.class);

    private static final String WORKSPACE_PATH_DEFAULT = "<USER_HOME>/verinice/workspace"; //$NON-NLS-1$
    private static final String METADATA_LOG_FILE_PATH_RELATIVE = "/.metadata/.log"; //$NON-NLS-1$
    private static final String CLIENT_LOG_FILE_PATH_RELATIVE = "/log/verinice-client.log"; //$NON-NLS-1$

    private static final String METADATA_LOG_FILE_PATH_DEFAULT = WORKSPACE_PATH_DEFAULT
            + METADATA_LOG_FILE_PATH_RELATIVE;
    private static final String CLIENT_LOG_FILE_PATH_DEFAULT = WORKSPACE_PATH_DEFAULT
            + CLIENT_LOG_FILE_PATH_RELATIVE;
    private static final String SERVER_LOG_FILE_PATH_DEFAULT = "/usr/share/tomcat6/logs/verinice-server.log"; //$NON-NLS-1$

    private ExceptionUtil() {
    }

    public static void log(Throwable e, final String exceptionTitle) {
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

        if (Activator.getDefault().getPreferenceStore()
                .getBoolean(PreferenceConstants.ERRORPOPUPS)) {
            final String message = getMessage(exceptionTitle);
            openErrorDialog(Messages.ExceptionUtilErrorPopupTitle, message);
        }
    }

    private static String getMessage(String exceptionMessage) {
        if (exceptionMessage == null) {
            exceptionMessage = "";
        }
        final String clientLogFilePath = getClientLogFilePath();
        final String metadataLogFilePath = getMetadataLogFilePath();
        if (Preferences.isStandalone()) {
            return NLS.bind(Messages.ExceptionUtilErrorMessageStandalone,
                    new Object[] { exceptionMessage, clientLogFilePath, metadataLogFilePath });
        } else {
            return NLS.bind(Messages.ExceptionUtilErrorMessageServerMode, new Object[] {
                    exceptionMessage, clientLogFilePath, metadataLogFilePath,
                    SERVER_LOG_FILE_PATH_DEFAULT });
        }
    }

    public static String getClientLogFilePath() {
        try {
            return getClientLogFilePathNotFailsave();
        } catch (Exception e) {
            LOG.error("Error while getting client log file path. Returning default path.", e); //$NON-NLS-1$
            return getClientLogFilePathDefault();
        }
    }

    @SuppressWarnings("unchecked")
    private static String getClientLogFilePathNotFailsave() {
        String logFileName = getClientLogFilePathDefault();
        Enumeration<Appender> appenders = Logger.getRootLogger().getAllAppenders();
        while (appenders.hasMoreElements()) {
            Appender appender = appenders.nextElement();
            if (appender instanceof FileAppender) {
                FileAppender fileAppender = (FileAppender) appender;
                logFileName = fileAppender.getFile();
            }
        }
        return logFileName;
    }

    public static String getClientLogFilePathDefault() {
        return CLIENT_LOG_FILE_PATH_DEFAULT;
    }

    public static String getMetadataLogFilePath() {
        try {
            return getMetadataLogFilePathNotFailsave();
        } catch (Exception e) {
            LOG.error("Error while getting metadata log file path. Returning default path.", e); //$NON-NLS-1$
            return getMetadataLogFilePathDefault();
        }
    }

    private static String getMetadataLogFilePathNotFailsave() {
        String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
        return new StringBuilder(workspacePath).append(METADATA_LOG_FILE_PATH_RELATIVE).toString();
    }

    public static String getMetadataLogFilePathDefault() {
        return METADATA_LOG_FILE_PATH_DEFAULT;
    }

    private static void openErrorDialog(final String exceptionTitle, final String message) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(Display.getDefault().getActiveShell(), exceptionTitle,
                        message);
            }
        });
    }

}
