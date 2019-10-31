/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.Messages;
import sernet.gs.ui.rcp.main.bsi.editors.EditorUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.ProgressAdapter;
import sernet.verinice.interfaces.IInternalServer;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.rcp.StatusResult;

/**
 * This initializer is executed after the workbench initializes. Initializer is
 * configured in plugin.xml of the bundle in extension point
 * org.eclipse.ui.startup.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class Initializer implements IStartup {

    private static final Logger log = Logger.getLogger(Initializer.class);

    @Override
    public void earlyStartup() {
        registerEditorCleaner();
        final StatusResult result = Initializer.startServer();
        Initializer.createModel(JobScheduler.getInitMutex(), result);
    }

    public static StatusResult startServer() {
        return startServer(new StatusResult());
    }

    /**
     * Tries to start the internal server via a workspace thread and returns a
     * result object for that operation.
     */
    private static StatusResult startServer(final StatusResult result) {
        final IInternalServer internalServer = Activator.getDefault().getInternalServer();
        if (!internalServer.isRunning()) {
            WorkspaceJob job = new WorkspaceJob("") { //$NON-NLS-1$
                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) {
                    Activator.inheritVeriniceContextState();
                    try {
                        if (!internalServer.isRunning()) {
                            monitor.beginTask(Messages.Activator_1, IProgressMonitor.UNKNOWN);
                            internalServer.start();
                        }
                        result.status = Status.OK_STATUS;
                    } catch (Exception e) {
                        ExceptionUtil.log(e, Messages.Activator_2);
                        result.status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                Messages.Activator_3, e);
                    } finally {
                        monitor.done();
                    }
                    return result.status;
                }
            };
            JobScheduler.scheduleJob(job, JobScheduler.getInitMutex(),
                    JobScheduler.getInitProgressMonitor());
        } else {
            result.status = Status.OK_STATUS;
        }
        return result;
    }

    public static void createModel() {
        createModel(JobScheduler.getInitMutex(), new StatusResult());
    }

    public static void createModel(ISchedulingRule mutex, final StatusResult serverStartResult) {
        WorkspaceJob job = new WorkspaceJob(Messages.Activator_LoadModel) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    // If server could not be started for whatever reason do not
                    // try to
                    // load the model either.
                    if (serverStartResult.status == Status.CANCEL_STATUS) {
                        status = Status.CANCEL_STATUS;
                    }
                    Activator.inheritVeriniceContextState();
                    monitor.beginTask(Messages.Activator_LoadModel, IProgressMonitor.UNKNOWN);
                    monitor.setTaskName(Messages.Activator_LoadModel);
                    CnAElementFactory.getInstance().loadOrCreateModel(new ProgressAdapter(monitor));
                    CnAElementFactory.getInstance().getISO27kModel();
                    CnAElementFactory.getInstance().getBpModel();
                    CnAElementFactory.getInstance().getCatalogModel();
                } catch (Exception e) {
                    log.error("Error while loading model.", e); //$NON-NLS-1$
                    if (e.getCause() != null && e.getCause().getLocalizedMessage() != null) {
                        setName(e.getCause().getLocalizedMessage());
                    }
                    status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.Activator_31,
                            e);
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleJob(job, mutex, JobScheduler.getInitProgressMonitor());
    }

    /**
     * Calls clean old editors. When the Workbench windows is not active at the
     * time, register a listener to be called when it is ready.
     */
    private void registerEditorCleaner() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window != null) {
            EditorUtil.cleanOldEditors();
        } else {
            workbench.addWindowListener(new IWindowListener() {

                @Override
                public void windowOpened(IWorkbenchWindow window) {
                    // no impl
                }

                @Override
                public void windowDeactivated(IWorkbenchWindow window) {
                    // no impl
                }

                @Override
                public void windowClosed(IWorkbenchWindow window) {
                    // no impl
                }

                @Override
                public void windowActivated(IWorkbenchWindow window) {
                    EditorUtil.cleanOldEditors();
                    workbench.removeWindowListener(this);
                }
            });
        }
    }

}
