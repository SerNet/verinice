/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
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
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.io.File;
import java.util.Calendar;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.Attachment;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class AddFileActionDelegate implements IObjectActionDelegate {

	private IWorkbenchPart targetPart;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		
		try {
			Object sel = ((IStructuredSelection)targetPart.getSite().getSelectionProvider().getSelection()).getFirstElement();
			if(sel instanceof CnATreeElement) {
				CnATreeElement element = (CnATreeElement) sel;
				FileDialog fd = new FileDialog(targetPart.getSite().getShell());
		        fd.setText("Anhang auswählen...");
		        fd.setFilterPath(System.getProperty("user.home"));
		        String selected = fd.open();
		        if(selected!=null && selected.length()>0) {
		        	File file = new File(selected);
		    		if (file.isDirectory())
		    			return;
		    		
					Attachment attachment = new Attachment();
					attachment.setCnATreeElementId(element.getDbId());
					attachment.setCnAElementTitel(element.getTitle());
					attachment.setTitel(file.getName());
					attachment.setDate(Calendar.getInstance().getTime());
					attachment.setFilePath(selected);
					
					attachment.addListener(new Attachment.INoteChangedListener() {
						public void noteChanged() {
							IViewPart page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(FileView.ID);
							if(page!=null) {
								((FileView)page).loadFiles();
							}
							
						}
					});
					
					EditorFactory.getInstance().openEditor(attachment);	
		        }
			}
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler");
		}
	
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
