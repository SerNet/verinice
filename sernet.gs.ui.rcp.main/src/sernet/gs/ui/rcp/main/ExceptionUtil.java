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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.GenericJDBCException;

import sernet.gs.service.SecurityException;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.CommandException;

/**
 * Helper class to handle and display exceptions.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov
 *          2007) $ $LastChangedBy: koderman $
 * 
 */
public class ExceptionUtil {

	private static final Logger LOG = Logger.getLogger(ExceptionUtil.class);
	
	public static void log(Throwable e, final String msg) {
		// log the error with log4j
		LOG.error("An error occured: " + msg, e);
		
		if (e instanceof StaleObjectStateException) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.ExceptionUtil_0, Messages.ExceptionUtil_1);
				}
			});
			return;
		}

		if (e instanceof SecurityException) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.ExceptionUtil_2, Messages.ExceptionUtil_3);
				}
			});
			return;
		}

		if (e instanceof CommandException && e.getCause() != null) {
			try {
				e = e.getCause();
			} catch (Exception castException) {
				// keep original exception
			}
		}

		String text = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : Messages.ExceptionUtil_4;

		if (Activator.getDefault() == null) {
			// RCP not initialized
			return;
		}

		final MultiStatus errorStatus = new MultiStatus(Activator.getDefault().getBundle().getSymbolicName(), IStatus.ERROR, text, e);

		Status status;
		if (e instanceof GenericJDBCException) {
			GenericJDBCException jdbcEx = (GenericJDBCException) e;
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), IStatus.ERROR, jdbcEx.getSQLException().getMessage(), e);

		} else if (e.getStackTrace() != null) {
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), IStatus.ERROR, stackTraceToString(e), e);

		} else {
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), IStatus.ERROR, e.getMessage(), e);

		}

		errorStatus.add(status);
		Activator.getDefault().getLog().log(status);

		if (Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.ERRORPOPUPS)) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(Display.getDefault().getActiveShell(), Messages.ExceptionUtil_5, msg, errorStatus);
				}
			});
		}
	}

	private static String stackTraceToString(Throwable e) {
		StringBuffer buf = new StringBuffer();
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (StackTraceElement stackTraceElement : stackTrace) {
			buf.append(stackTraceElement.toString() + "\n"); //$NON-NLS-1$
		}
		return buf.toString();
	}
}
