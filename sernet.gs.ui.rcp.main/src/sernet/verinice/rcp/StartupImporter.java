/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IRightsServiceClient;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.sync.VeriniceArchive;

/**
 * StartupImporter is part of the server connection toggling process started by action
 * {@link ServerConnectionToggleAction}.
 * 
 * Static method importVna imports verinice archives from a specific folder (if there are any):
 * <CLIENT_WORKSPACE>/conf/client-server-transport-<N>.vna
 * 
 * Import is done by a {@link WorkspaceJob} at program start initialized by {@link Activator}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class StartupImporter {

    private static final Logger LOG = Logger.getLogger(StartupImporter.class);

    public static final String SERVER_TRANSPORT_BASENAME = "client-server-transport"; //$NON-NLS-1$

    private static volatile IModelLoadListener modelLoadListener;

    public static synchronized void importVna() {
        if (!isRestart()) {
            deleteFiles();
            return;
        }
        if (CnAElementFactory.isModelLoaded()) {
            startImportJob();
        } else if (modelLoadListener == null) {
            // model is not loaded yet: add a listener to load data when it's
            // loaded
            modelLoadListener = new IModelLoadListener() {
                @Override
                public void closed(BSIModel model) {
                }

                public void loaded(BSIModel model) {
                }

                @Override
                public void loaded(ISO27KModel model) {
                    startImportJob();
                }
            };
            CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
        }
    }

    private static boolean isRestart() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        return getImportFile(0).exists() && prefs.getBoolean(PreferenceConstants.RESTART);
    }

    protected static void startImportJob() {
        if(!getRightsService().isEnabled(ActionRightIDs.XMLIMPORT)) {
            deleteFiles();
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Import", "Data transfer failed. You are are not allowed to import data.");
                }
            });
            return;
        }
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.StartupImporter_0) {
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.StartupImporter_1, IProgressMonitor.UNKNOWN);
                    doImport();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.StartupImporter_2, e); //$NON-NLS-1$ 
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    protected static void doImport() {
        try {
            boolean fileExists = true;
            int i = 0;
            while (fileExists) {
                File archive = getImportFile(i);
                if (fileExists = archive.exists()) {
                    doImport(archive);
                }
                i++;
            }
        } finally {
            CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
            IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
            prefs.setValue(PreferenceConstants.RESTART, false);
        }
    }

    private static void doImport(File archive) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting import of file: " + archive.getAbsolutePath()); //$NON-NLS-1$
            }
            byte[] fileData = FileUtils.readFileToByteArray(archive);
            Activator.inheritVeriniceContextState();
            SyncCommand command = new SyncCommand(new SyncParameter(true, true, false, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV), fileData);
            getCommandService().executeCommand(command);
        } catch (Exception e) {
            LOG.error("Error while importing.", e); //$NON-NLS-1$
        } finally {
            if (archive != null && archive.exists()) {
                FileUtils.deleteQuietly(archive);
            }
        }
    }

    private static File getImportFile(int i) {
        File archive;
        StringBuilder sb = new StringBuilder();
        sb.append(CnAWorkspace.getInstance().getConfDir()).append(File.separatorChar).append(SERVER_TRANSPORT_BASENAME);
        sb.append("-").append(i).append(VeriniceArchive.EXTENSION_VERINICE_ARCHIVE);
        archive = new File(sb.toString());
        return archive;
    }
    

    
    private static void deleteFiles() {
        boolean fileExists = true;
        int i = 0;
        while (fileExists) {
            File archive = getImportFile(i);
            if (fileExists = archive.exists()) {
                FileUtils.deleteQuietly(archive);
            }
            i++;
        }
    }
    
    private static ICommandService getCommandService() {
        return ServiceFactory.lookupCommandService();
    }
    
    private static IRightsServiceClient getRightsService() {
        return (IRightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
    }
}
