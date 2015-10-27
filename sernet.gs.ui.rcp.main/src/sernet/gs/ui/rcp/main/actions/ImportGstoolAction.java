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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.gsimport.IProgress;
import sernet.gs.ui.rcp.gsimport.ImportNotesTask;
import sernet.gs.ui.rcp.gsimport.ImportRisikoanalysenTask;
import sernet.gs.ui.rcp.gsimport.ImportTask;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.GSImportDialog;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;

public class ImportGstoolAction extends RightsEnabledAction {

    private static final Logger LOG = Logger.getLogger(ImportGstoolAction.class);
    
	public static final String ID = "sernet.gs.ui.rcp.main.importgstoolaction";

	private GSImportDialog dialog;
	
	private Shell shell;
	
	private String sourceId;

    public ImportGstoolAction(IWorkbenchWindow window, String label) {
        setText(label);
        setId(ID);
        setEnabled(true); // now works in standalone again
        setRightID(ActionRightIDs.GSTOOLIMPORT);
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
    }
    

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        try {
            this.shell = Display.getCurrent().getActiveShell();
            dialog = new GSImportDialog(shell);
			if (dialog.open() != InputDialog.OK){
                return;
			}
			importZielobjekte();
      	
			if (dialog.isNotizen()) {
			    importNotes();
			}
			
			if (dialog.isRisikoanalysen()) {
                importRiskAnalyses();          
			}			
		} catch (Exception e) {
			ExceptionUtil.log(e, "Import aus dem Gstool fehlgeschlagen.");
		} 
    }
    
    private void importZielobjekte() throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                Activator.inheritVeriniceContextState();
                ImportTask importTask = new ImportTask(
                        dialog.isBausteine(),
                        dialog.isMassnahmenPersonen(),
                        dialog.isZielObjekteZielobjekte(),
                        dialog.isSchutzbedarf(),
                        dialog.isRollen(),
                        dialog.isKosten(),
                        dialog.isUmsetzung(),
                        dialog.isBausteinPersonen()
                        );         
                long importTaskStart = System.currentTimeMillis();
                importTask.execute(ImportTask.TYPE_SQLSERVER, new EclipseProgressMonitorDelegator(monitor));
                if(LOG.isDebugEnabled()){
                    LOG.debug("Time for ImportTask:\t" + String.valueOf((System.currentTimeMillis() - importTaskStart)/1000 ) + " seconds");
                }             
                sourceId = importTask.getSourceId();
            }
        });
    }
    
    private void importNotes() throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().
        busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                Activator.inheritVeriniceContextState();            
                long importNotesStart = System.currentTimeMillis();
                ImportNotesTask importTask = new ImportNotesTask();
                importTask.execute(ImportTask.TYPE_SQLSERVER, new EclipseProgressMonitorDelegator(monitor));
                if(LOG.isDebugEnabled()){
                    LOG.debug("Time for ImportNotesTask:\t" + String.valueOf((System.currentTimeMillis() - importNotesStart)/1000 ) + " seconds");
                }
            }
        });
    }


    private void importRiskAnalyses() throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().
        busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                Activator.inheritVeriniceContextState();

                long importRAStart = System.currentTimeMillis();
                ImportRisikoanalysenTask importTask = new ImportRisikoanalysenTask(sourceId);
                importTask.execute(ImportTask.TYPE_SQLSERVER, new EclipseProgressMonitorDelegator(monitor));
                if(LOG.isDebugEnabled()){
                    LOG.debug("Time for ImportRATask:\t" + String.valueOf((System.currentTimeMillis() - importRAStart)/1000 ) + " seconds");
                }
            }
        });
    }
    
    /**
     * This IProgress implementation delegates all method
     * calls to a Eclipse IProgressMonitor.
     * 
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     */
    private final class EclipseProgressMonitorDelegator implements IProgress {

        private final IProgressMonitor monitor;

        private EclipseProgressMonitorDelegator(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void done() {
            monitor.done();
        }

        @Override
        public void worked(int work) {
            monitor.worked(work);
        }

        @Override
        public void beginTask(String name, int totalWork) {
            monitor.beginTask(name, totalWork);
        }

        @Override
        public void subTask(String name) {
            monitor.subTask(name);
        }
    }
}
