/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;


public class ShowCheatSheetAction extends Action {
	
	private boolean tutorial;
	private static final String ID1 = "sernet.gs.ui.rcp.main.showcheatsheetaction";
	private static final String ID2 = "sernet.gs.ui.rcp.main.showcheatsheetlistaction";
	
	public ShowCheatSheetAction(boolean tutorial, String title) {
        setText(title);
        if (tutorial)
        	setId(ID1);
        else
        	setId(ID2);
		this.tutorial = tutorial;
	}
	
	public void run() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window != null) {	
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()[0]
				           .showView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
				IViewPart part = window.getActivePage()
					.findView("org.eclipse.ui.cheatsheets.views.CheatSheetView");
				if (part != null && tutorial) {
					CheatSheetView view = (CheatSheetView) part;
					view.setInput("sernet.gs.ui.rcp.main.cheatsheet1");
				}
			} catch (PartInitException e) {
				MessageDialog.openError(window.getShell(), "Error", 
						"Error opening view: " 
						+ e.getMessage());
			}
		}
	}
}
