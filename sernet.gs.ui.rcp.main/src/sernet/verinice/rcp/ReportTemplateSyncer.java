/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.reports.ReportDepositCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.IReportTemplateService;
import sernet.verinice.interfaces.ReportDepositException;
import sernet.verinice.interfaces.ReportTemplateServiceException;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.report.FileMetaData;

/**
 * Downloads reports & related resources from verinice server. Each file in the
 * {@link IReportTemplateService#REPORT_DEPOSIT_CLIENT_REMOTE} folder and in the
 * server side folder is hashed so only new or changed files can be copied.
 *
 * <h2>Server mode:</h2>
 *
 * Only metadata objects are compared and if the client folder does not contain
 * a metadata object or it differs, the template file is deleted and the server
 * template will be downloaded.
 *
 * <h2>Client mode:</h2>
 *
 * If the verinice client operates in standalone mode, no files are deleted,
 * since new files which are added by the report deposit should also be
 * accessible in this mode.
 *
 * <h2>Note:</h2>
 *
 *
 * <p>
 * 1. The folder {@link IReportTemplateService#REPORT_DEPOSIT_CLIENT_LOCAL} is
 * not affected by this. The content of this folder lives only on the client
 * machine and is under full controll of the end user.
 * </p>
 *
 * <p>
 * 2. Syncing is only done from server to client, means it is an one way
 * syncing.
 * </p>
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ReportTemplateSyncer extends WorkspaceJob implements IModelLoadListener {

    private static volatile IModelLoadListener modelLoadListener;

    private static final Logger LOG = Logger.getLogger(ReportTemplateSyncer.class);

    private IReportTemplateService localReportDeposit;

    private ReportTemplateSyncer() {
        super("sync reports");
        localReportDeposit = new ReportDepositCache();
    }

    public static void sync() {
        if (CnAElementFactory.isModelLoaded()) {
            startSync();
        } else if (modelLoadListener == null) {
            CnAElementFactory.getInstance().addLoadListener(new ReportTemplateSyncer());
        }
    }

    private static void startSync() {
        Activator.inheritVeriniceContextState();

        WorkspaceJob syncReportsJob = new ReportTemplateSyncer();
        JobScheduler.scheduleInitJob(syncReportsJob);
    }

    private void syncReportFiles(Locale locale)
            throws ReportTemplateServiceException, ReportDepositException, IOException {

        Set<FileMetaData> localFiles = localReportDeposit.getAllResources();
        Set<FileMetaData> remoteFiles = getRemoteReportDeposit().getAllResources(locale);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found\t" + localFiles.size() + "\tfiles in local repo ("
                    + CnAWorkspace.getInstance().getRemoteReportTemplateDir()
                    + ") (server mirror) before the sync");
            LOG.debug("Found\t" + remoteFiles.size()
                    + "\tfiles in server repo, which need to be synced");
            LOG.debug("Syncing will take place with following locale:\t" + locale);
        }

        // download or update files
        sync(localFiles, remoteFiles);

        // delete files only in server mode
        if (Preferences.isServerMode()) {
            cleanupLocalFiles(localReportDeposit.getAllResources(), remoteFiles);
        }
    }

    private void sync(Set<FileMetaData> localFiles, Set<FileMetaData> remoteFiles)
            throws ReportTemplateServiceException, ReportDepositException, IOException {
        for (FileMetaData file : remoteFiles) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Syncing:\t" + file.getFilename());
            }
            if (!localFiles.contains(file)) {
                download(file.getFilename());
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("File\t" + file.getFilename()
                        + "\twill not be synced, since it's already existant on client");
            }
        }
    }

    private void cleanupLocalFiles(Set<FileMetaData> localFiles, Set<FileMetaData> remoteFiles)
            throws ReportTemplateServiceException {
        for (FileMetaData file : localFiles) {
            if (!remoteFiles.contains(file)) {
                deleteLocalFile(file.getFilename());
            }
        }
    }

    private IReportDepositService getRemoteReportDeposit() {
        return ServiceFactory.lookupReportDepositService();
    }

    private void download(String filename)
            throws ReportDepositException, ReportTemplateServiceException, IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Syncing:\t" + filename);
        }

        deleteLocalFile(filename);

        byte[] content = getRemoteReportDeposit().readResource(filename);
        File directory = CnAWorkspace.getInstance().getRemoteReportTemplateDir();
        File rptdesignTemplate = new File(directory, filename);
        FileUtils.writeByteArrayToFile(rptdesignTemplate, content);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Template:\t" + filename + " written to:\t"
                    + rptdesignTemplate.getAbsolutePath());
        }
    }

    private void deleteLocalFile(String fileName) {
        File filePath = CnAWorkspace.getInstance().getRemoteReportTemplateDir();

        File file = new File(filePath, fileName);

        if (file.exists()) {
            file.delete();
            if (LOG.isDebugEnabled()) {
                LOG.debug("TemplateFile:\t" + file.getAbsolutePath() + "\tdeleted");
            }
        }
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {
        IStatus status = Status.OK_STATUS;

        try {
            Activator.inheritVeriniceContextState();

            syncReportFiles(Locale.getDefault());
        } catch (ReportDepositException e) {
            status = errorHandler(e);
        } catch (IOException e) {
            status = errorHandler(e);
        } catch (ReportTemplateServiceException e) {
            status = errorHandler(e);
        }

        return status;
    }

    private IStatus errorHandler(Exception e) {
        IStatus status;
        String msg = "error while syncing report templates:\t" + e.getLocalizedMessage()
                + e.getStackTrace();
        LOG.error(msg, e);
        status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", msg);
        return status;
    }

    @Override
    public void loaded(BSIModel model) {
    }

    @Override
    public void loaded(ISO27KModel model) {
        sync();
    }

    @Override
    public void closed(BSIModel model) {
    }

    @Override
    public void loaded(BpModel model) {
        // nothing to do
    }

    @Override
    public void loaded(CatalogModel model) {
        // nothing to do
    }

}
