package sernet.gs.ui.rcp.main;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class StatusLine {

	private static IStatusLineManager getStatusLine() {
		try {
			IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
			.getActivePart();
			if (activePart instanceof IViewPart) {
				return ((IViewPart)activePart).getViewSite().getActionBars().getStatusLineManager();
			}
			if (activePart instanceof IEditorPart) {
				return ((IEditorPart)activePart).getEditorSite().getActionBars().getStatusLineManager();
			}
		} catch (RuntimeException e) {
		}
		return null;
	}
	
	public static void setMessage(String message) {
		if (getStatusLine() != null) {
			getStatusLine().setMessage(message);
		}
	}

	public static void setErrorMessage(String message) {
		if (getStatusLine() != null) {
			getStatusLine().setErrorMessage(message);
		}
	}

}
