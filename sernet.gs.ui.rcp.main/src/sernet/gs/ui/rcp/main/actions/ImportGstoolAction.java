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

import sernet.gs.service.TimeFormatter;
import sernet.gs.ui.rcp.gsimport.GstoolImportCanceledException;
import sernet.gs.ui.rcp.gsimport.IProgress;
import sernet.gs.ui.rcp.gsimport.ImportNotesTask;
import sernet.gs.ui.rcp.gsimport.ImportRisikoanalysenTask;
import sernet.gs.ui.rcp.gsimport.ImportTask;
import sernet.gs.ui.rcp.gsimport.ValidateTask;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.GSImportDialog;
import sernet.verinice.interfaces.ActionRightIDs;

/**
 * Rights enables JFace action which starts the GSTOOL import.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ImportGstoolAction extends RightsEnabledAction {

    private static final Logger LOG = Logger.getLogger(ImportGstoolAction.class);
    
	public static final String ID = "sernet.gs.ui.rcp.main.importgstoolaction";

	private GSImportDialog dialog;	
	private Shell shell;	
	private String sourceId;

    public ImportGstoolAction(IWorkbenchWindow window, String label) {
        super(ActionRightIDs.GSTOOLIMPORT, label);
        setId(ID);
        setEnabled(true); // now works in standalone again
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
			validateNonBlocking();
			importZielobjekteNonBlocking();   	
			if (dialog.isNotizen()) {
			    importNotesNonBlocking();
			}		
			if (dialog.isRisikoanalysen()) {
                importRiskAnalysesNonBlocking();          
			}			
        } catch (InvocationTargetException e) {
            handleInvocationTargetException(e);           
        } catch (Exception e) {
			handleException(e);
		} 
    }
    
    /**
     * Checks if all types and subtypes of the Zielobjekte in GSTOOL
     * can be found in the configuration. If an unknown type was found
     * the user is asked to cancel the import. If the user cancels the import
     * a {@link GstoolImportCanceledException} is thrown. {@link GstoolImportCanceledException}
     * is a {@link RuntimeException}.
     * 
     * @param monitor A {@link IProgressMonitor}
     * @throws {@link GstoolImportCanceledException} If user cancels the import after an unknown type was found
     */
    private void validate(final IProgressMonitor monitor) { // throws GstoolImportCanceledException
        Activator.inheritVeriniceContextState();
        ValidateTask validateTask = new ValidateTask(shell);         
        long taskStart = System.currentTimeMillis();
        validateTask.execute(ImportTask.TYPE_SQLSERVER, new EclipseProgressMonitorDelegator(monitor));
        if(LOG.isDebugEnabled()){
            LOG.debug("Time for validating:\t" + TimeFormatter.getHumanRedableTime(System.currentTimeMillis() - taskStart));
        }
    }
    
    /**
     * Imports Zielobjekte from GSTOOL
     * Bausteine, Gefaehrdungen, Massnahmen und relations between
     * these objects are also imported if selected in configuration dialog.
     * 
     * @param monitor A progress monitor
     */
    private void importZielobjekte(final IProgressMonitor monitor) {
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
    
    /**
     * Imports notes from GSTOOL
     * 
     * @param monitor A progress monitor
     */
    private void importNotes(final IProgressMonitor monitor) {
        Activator.inheritVeriniceContextState();            
        long importNotesStart = System.currentTimeMillis();
        ImportNotesTask importTask = new ImportNotesTask();
        importTask.execute(ImportTask.TYPE_SQLSERVER, new EclipseProgressMonitorDelegator(monitor));
        if(LOG.isDebugEnabled()){
            LOG.debug("Time for ImportNotesTask:\t" + String.valueOf((System.currentTimeMillis() - importNotesStart)/1000 ) + " seconds");
        }
    }
    
    /**
     * Imports risk analyses from GSTOOL.
     * 
     * @param monitor A progress monitor
     */
    private void importRiskAnalyses(final IProgressMonitor monitor) {
        Activator.inheritVeriniceContextState();
        long importRAStart = System.currentTimeMillis();
        ImportRisikoanalysenTask importTask = new ImportRisikoanalysenTask(sourceId);
        importTask.execute(ImportTask.TYPE_SQLSERVER, new EclipseProgressMonitorDelegator(monitor));
        if(LOG.isDebugEnabled()){
            LOG.debug("Time for ImportRATask:\t" + String.valueOf((System.currentTimeMillis() - importRAStart)/1000 ) + " seconds");
        }
    }
    
    private void handleInvocationTargetException(InvocationTargetException e) {
        Throwable cause = e.getCause();
        if(cause!=null && cause instanceof GstoolImportCanceledException) {
            LOG.warn("Import from GSTOOL was canceled, cause: ", cause);
        } else {
            if(cause!=null) {
                handleException(cause);
            } else {
                handleException(e);
            }
        }       
    }

    private void handleException(Throwable cause) {
        ExceptionUtil.log(cause, "Import aus dem Gstool fehlgeschlagen.");
    }
    
    private void validateNonBlocking() throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor)  {
                validate(monitor);
            }          
        });
    }    
    private void importZielobjekteNonBlocking() throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                importZielobjekte(monitor);
            } 
        });
    }   
    private void importNotesNonBlocking() throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                importNotes(monitor);
            }          
        });
    }
    private void importRiskAnalysesNonBlocking() throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().
        busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                importRiskAnalyses(monitor);
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
