package sernet.gs.ui.rcp.main.bsi.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

import sernet.gs.ui.rcp.main.ExceptionUtil;

/**
 * Download action for cheatsheet "first steps".
 *  
 * @author koderman@sernet.de
 *
 */
public class DownloadBsiCataloguesAction extends Action implements ICheatSheetAction {
	private IWorkbenchPart targetPart;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

	public void run(String[] params, ICheatSheetManager manager) {
		try {
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
				.openURL(new URL("http://www.bsi.de/gshb/deutsch/download/"));
		} catch (PartInitException e) {
			ExceptionUtil.log(e, "Konnte BSI URL nicht öffnen.");
		} catch (MalformedURLException e) {
			ExceptionUtil.log(e, "Konnte BSI URL nicht öffnen.");
		}
		
	}

}
