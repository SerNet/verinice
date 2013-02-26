/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.gsimport.IProgress;
import sernet.gs.ui.rcp.gsimport.ImportNotesTask;
import sernet.gs.ui.rcp.gsimport.ImportTask;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.iso27k.ISO27KModel;


public class ImportGstoolNotesAction extends RightsEnabledAction {
	
	public static final String ID = "sernet.gs.ui.rcp.main.importgstoolnotesaction";
	private final IWorkbenchWindow window;
	
	private IModelLoadListener loadListener = new IModelLoadListener() {
		public void closed(BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
//					setEnabled(false);
				}
			});
		}
		
		public void loaded(final BSIModel model) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
				    // only enable in server mode:
//                    ServiceFactory.lookupAuthService();
//                    if (ServiceFactory.isPermissionHandlingNeeded()) {
//                        setEnabled(true);
//                    }
				}
			});
		}

        @Override
        public void loaded(ISO27KModel model) {
            // nothing to do
            
        }
	};
	
	public ImportGstoolNotesAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
		setId(ID);
		setEnabled(true); // now works in standalone again
		setRightID(ActionRightIDs.GSNOTESIMPORT);
		if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
		    IInternalServerStartListener listener = new IInternalServerStartListener(){
		        @Override
		        public void statusChanged(InternalServerEvent e) {
		            if(e.isStarted()){
		                setEnabled(checkRights());
		            }
		        }

		    };
		    Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
		} else {
		    setEnabled(checkRights());
		}
		CnAElementFactory.getInstance().addLoadListener(loadListener);
	}
	
	public void run() {
		try {
			boolean confirm = MessageDialog.openConfirm(window.getShell(), "Nachträglicher Notizimport", "Notizen werden aus der GSTOOL-Datenbank (siehe \"Bearbeiten\" -> \"Einstellungen\") in vorhandene IT-Verbünde angehängt. Fortfahren?");
			if (!confirm){
				return;
			}
			PlatformUI.getWorkbench().getProgressService().
			busyCursorWhile(new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					Activator.inheritVeriniceContextState();
					
					ImportNotesTask importTask = new ImportNotesTask();
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
			ExceptionUtil.log(e.getCause(), "Notiz-Import aus dem Gstool fehlgeschlagen.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Notiz-Import aus dem Gstool fehlgeschlagen.");
		}
	}
	
}
