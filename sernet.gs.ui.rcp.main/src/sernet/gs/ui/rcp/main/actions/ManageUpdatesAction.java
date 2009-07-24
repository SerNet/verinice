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
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.update.ui.UpdateManagerUI;

public class ManageUpdatesAction extends Action implements IAction {
	private IWorkbenchWindow window;
	
	public ManageUpdatesAction(IWorkbenchWindow window) {
		this.window = window;
		setId("sernet.gs.ui.rcp.main.actions.manageupdatesaction");
		setText("Installierte Updates anzeigen...");
		setToolTipText("Anzeigen / Bearbeiten installierter Programmbestandteile");
		
	}
	
	@Override
	public void run() {
		BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				UpdateManagerUI.openConfigurationManager(window.getShell());
			}
		});
	}
}
