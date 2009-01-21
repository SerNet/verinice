package sernet.gs.ui.rcp.main;

import java.io.PrintStream;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.GenericJDBCException;

import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.commands.CommandException;

/**
 * Helper class to handle and display exceptions.
 * 
 * @author koderman@sernet.de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov 2007) $ 
 * $LastChangedBy: koderman $
 *
 */
public class ExceptionUtil {
	
	public static void log(Throwable e, final String msg) {
		if (e instanceof StaleObjectStateException) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(Display.getDefault().getActiveShell(),
							"Synchronisation",
							"Die Daten wurden zwischenzeitlich durch einen anderen Benutzer verändert und soeben " +
							"aktualisiert. Ihre Aktion wurde abgebrochen um zu verhindern, dass unbeabsichtigt " +
							"Daten überschrieben werden. Bitte überprüfen Sie die Änderungen und führen Sie ihre" +
							"Eingabe ggf. erneut aus.");
				}
			});
			return;
		}
		
		if (e instanceof CommandException
				&& e.getCause() != null) {
			try {
				e = (Exception) e.getCause();
			} catch (Exception castException) {
				// keep original exception
			}
		}
		
		
		String text = e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "siehe Details";
		
		if (Activator.getDefault() == null) {
			// RCP not initialized, skip dialog and just print to stdout:
			System.err.println(msg);
			e.printStackTrace();
			return;
		}
		
		final MultiStatus errorStatus = new MultiStatus(Activator.getDefault().getBundle().getSymbolicName(),
				IStatus.ERROR,
				text,
				e);
		
		
		
		Status status;
		if (e instanceof GenericJDBCException) {
			GenericJDBCException jdbcEx = (GenericJDBCException) e;
			status = new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					IStatus.ERROR,
					jdbcEx.getSQLException().getMessage(),
					e);
			
		}
		else if (e.getStackTrace()!= null) {
			status = new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					IStatus.ERROR,
					stackTraceToString(e),
					e);
			
		}
		else {
			status = new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					IStatus.ERROR,
					e.getMessage(),
					e);
			
		}
		
		errorStatus.add(status );
		Activator.getDefault().getLog().log(status);
		
		if (Activator.getDefault().getPluginPreferences()
				.getBoolean(PreferenceConstants.ERRORPOPUPS)) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(Display.getDefault().getActiveShell(),
							"Fehler",
							msg,
							errorStatus);
				}
			});
		}
	}

	private static String stackTraceToString(Throwable e) {
		StringBuffer buf = new StringBuffer();
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (StackTraceElement stackTraceElement : stackTrace) {
			buf.append(stackTraceElement.toString() + "\n");
		}
		return buf.toString();
	}
}
