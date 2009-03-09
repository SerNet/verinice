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
import java.util.Iterator;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.gsimport.IProgress;
import sernet.gs.ui.rcp.gsimport.ImportTask;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ICommandIds;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.GSImportDialog;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.bsi.wizards.ExportWizard;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.NullModel;


public class ImportGstoolAction extends Action {
	
	public static final String ID = "sernet.gs.ui.rcp.main.importgstoolaction";
	private final IWorkbenchWindow window;
	
	private IModelLoadListener loadListener = new IModelLoadListener() {
		public void closed(BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setEnabled(false);
				}
			});
		}
		
		public void loaded(final BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setEnabled(true);
				}
			});
		}
	};
	
	public ImportGstoolAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
		setId(ID);
		setEnabled(false);
		CnAElementFactory.getInstance().addLoadListener(loadListener);
	}
	
	
	public void run() {
		try {
			
			final GSImportDialog dialog = new GSImportDialog(Display.getCurrent().getActiveShell());
			if (dialog.open() != InputDialog.OK)
				return;
			
			PlatformUI.getWorkbench().getProgressService().
			busyCursorWhile(new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					ImportTask importTask = new ImportTask(
							dialog.isBausteine(),
							dialog.isMassnahmenPersonen(),
							dialog.isZielObjekteZielobjekte(),
							dialog.isSchutzbedarf(),
							dialog.isRollen(),
							dialog.isKosten(),
							dialog.isUmsetzung(),
							dialog.isBausteinPersonen());
					try {
						importTask.execute(ImportTask.TYPE_SQLSERVER, new IProgress() {
							public void done() {
								monitor.done();
							}
							public void worked(int work) {
								monitor.worked(work);
							}
							public void beginTask(String name, int totalWork) {
								monitor.beginTask(name, totalWork);
							}
							public void subTask(String name) {
								monitor.subTask(name);
							}
						});
					} catch (Exception e) {
						ExceptionUtil.log(e, "Fehler beim Importieren.");
					}
				}
			});
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Import aus dem Gstool fehlgeschlagen.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Import aus dem Gstool fehlgeschlagen.");
		}
	}
	
}
