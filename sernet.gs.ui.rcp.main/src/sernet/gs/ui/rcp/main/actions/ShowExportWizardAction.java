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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.wizards.ExportWizard;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;


public class ShowExportWizardAction extends Action {
	
	public static final String ID = "sernet.gs.ui.rcp.main.showexportwizardaction";
	private final IWorkbenchWindow window;
	
	public ShowExportWizardAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
		setId(ID);
		setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.REPORT));
		setEnabled(false);
		
		CnAElementFactory.getInstance().addLoadListener(new IModelLoadListener() {

			public void closed(BSIModel model) {
				setEnabled(false);
			}

			public void loaded(BSIModel model) {
				setEnabled(true);
			}
			
		});
	}
	
	
	public void run() {
		try {
			PlatformUI.getWorkbench().getProgressService().
			busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Öffne OpenOffice Export...", IProgressMonitor.UNKNOWN);
					
					ExportWizard wizard = new ExportWizard();
					wizard.init(window.getWorkbench(), null);
					final WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							dialog.open();		
						}
					});
				}
			});
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Öffnen von OO Export fehlgeschlagen.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Öffnen von OO Export fehlgeschlagen.");
		}
	}
	
}
