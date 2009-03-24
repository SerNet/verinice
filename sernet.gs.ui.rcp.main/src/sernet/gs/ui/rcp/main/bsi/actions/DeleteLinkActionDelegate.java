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

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Delete items on user request.
 * 
 * @author akoderman@sernet.de
 *
 */
public class DeleteLinkActionDelegate implements IObjectActionDelegate {


	private IWorkbenchPart targetPart;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		
		if (! MessageDialog.openQuestion(
				(Shell)targetPart.getAdapter(Shell.class),
				"Wirklich entfernen?",
				"Alle markierten Verknüpfungen werden entfernt. Die referenzierten" +
				" Objekte bleiben erhalten.\n\n"+ 
		"Wirklich entfernen?")) {
			return;
		}
		
		// close editors first:
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		.getActivePage().closeAllEditors(true /* ask save */);

		IStructuredSelection selection = ((IStructuredSelection)targetPart.getSite()
				.getSelectionProvider().getSelection());
		
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object sel = (Object) iter.next();
		
			if (sel instanceof CnALink) {
				CnALink link = (CnALink) sel;
				//link.remove();
				try {
					CnAElementHome.getInstance().remove(link);
					CnAElementFactory.getLoadedModel().linkRemoved(link);
				} catch (Exception e) {
					ExceptionUtil.log(e, "Fehler beim Löschen von Verknüpfung.");
				}
			}
			
		}
	
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

}
