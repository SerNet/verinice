/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
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
