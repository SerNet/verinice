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

import static org.apache.commons.io.FilenameUtils.concat;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
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
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
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
import sernet.verinice.model.report.ReportTemplate;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * Downloads reports from verinice server. Therefore a set of report metadata
 * which contain checksums of the report template and the properties files which
 * belong to the template is calculated in the
 * {@link IReportTemplateService#REPORT_DEPOSIT_CLIENT_REMOTE} folder and on the
 * server side folder.
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
public class ReportTemplateSync extends WorkspaceJob implements IModelLoadListener {

    private static volatile IModelLoadListener modelLoadListener;

    private Logger LOG = Logger.getLogger(ReportTemplateSync.class);

    private IReportTemplateService reportDepositCache;

    private ReportTemplateSync() {
        super("sync reports");
        reportDepositCache = new ReportDepositCache();
    }

    public static void sync() {
        if (CnAElementFactory.isModelLoaded()) {
            startSync();
        } else if (modelLoadListener == null) {
            CnAElementFactory.getInstance().addLoadListener(new ReportTemplateSync());
        }
    }

    private static void startSync() {

        Activator.inheritVeriniceContextState();

        WorkspaceJob syncReportsJob = new ReportTemplateSync();
        JobScheduler.scheduleInitJob(syncReportsJob);
    }

    private void syncReportTemplates(String locale) throws ReportTemplateServiceException, ReportDepositException, IOException {

        Set<ReportTemplateMetaData> cachedTemplates = reportDepositCache.getReportTemplates(locale);
        Set<ReportTemplateMetaData> remoteServerTemplates = getIReportDepositService().getReportTemplates(locale);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found\t" + cachedTemplates.size() + "\tTemplates in local repo (" + CnAWorkspace.getInstance().getRemoteReportTemplateDir() + ") (server mirror) before the sync");
            LOG.debug("Found\t" + remoteServerTemplates.size() + "\tTemplates in server repo, which need to be synced");
            LOG.debug("Syncing will take place with following locale:\t" + locale);
        }

        // download or update reports
        syncReports(cachedTemplates, remoteServerTemplates, locale);

        // delete reports only in server mode
        deleteReportsInServerMode(reportDepositCache, remoteServerTemplates, locale);

    }

    private void syncReports(Set<ReportTemplateMetaData> cachedTemplates, Set<ReportTemplateMetaData> remoteServerTemplates, String locale) throws ReportTemplateServiceException, ReportDepositException, IOException {
        int i = 0;
        for (ReportTemplateMetaData remoteTemplateMetaData : remoteServerTemplates) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Syncing:\t" + remoteTemplateMetaData.getFilename() + "\t(" + String.valueOf(i) + ")");
            }
            if (!cachedTemplates.contains(remoteTemplateMetaData)) {
                syncTemplate(remoteTemplateMetaData, locale);
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Template\t" + remoteTemplateMetaData.getOutputname() + "\twill not be synced, since it's already existant on client");
            }
            i++;
        }
    }

    private void deleteReportsInServerMode(IReportTemplateService cachedTemplateService, Set<ReportTemplateMetaData> remoteServerTemplates, String locale) throws ReportTemplateServiceException {
        if (isNotStandalone()) {
            for (ReportTemplateMetaData localTemplateMetaData : cachedTemplateService.getReportTemplates(locale)) {
                if (!remoteServerTemplates.contains(localTemplateMetaData)) {
                    deleteRptdesignAndPropertiesFiles(localTemplateMetaData.getFilename());
                }
            }
        }
    }

    private boolean isNotStandalone() {
        return Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_REMOTE_SERVER);
    }

    private IReportDepositService getIReportDepositService() {
        return ServiceFactory.lookupReportDepositService();
    }

    private void syncTemplate(ReportTemplateMetaData metadata, String locale) throws ReportDepositException, ReportTemplateServiceException, IOException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Syncing:\t" + metadata.getOutputname());
        }

        deleteRptdesignAndPropertiesFiles(metadata.getFilename());

        ReportTemplate template = getIReportDepositService().getReportTemplate(metadata, locale);
        String directory = CnAWorkspace.getInstance().getRemoteReportTemplateDir();
        File rptdesignTemplate = new File(concat(directory, template.getMetaData().getFilename()));
        FileUtils.writeByteArrayToFile(rptdesignTemplate, template.getRptdesignFile());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Template:\t" + metadata.getFilename() + " written to:\t" + rptdesignTemplate.getAbsolutePath());
        }

        for (Entry<String, byte[]> e : template.getPropertiesFiles().entrySet()) {
            FileUtils.writeByteArrayToFile(new File(concat(directory, e.getKey())), e.getValue());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Propertyfile:\t" + concat(directory, e.getKey()) + "\twritten");
            }
        }
    }

    private void deleteRptdesignAndPropertiesFiles(String fileName) {

        String filePath = CnAWorkspace.getInstance().getRemoteReportTemplateDir();
        if (!filePath.endsWith(String.valueOf(File.separatorChar))) {
            filePath = filePath + File.separatorChar;
        }
        filePath = filePath + fileName;
        File rptdesign = new File(filePath);

        if (rptdesign.exists()) {
            rptdesign.delete();
            if (LOG.isDebugEnabled()) {
                LOG.debug("TemplateFile:\t" + rptdesign.getAbsolutePath() + "\tdeleted");
            }
        }

        // delete properties files
        Iterator<File> iter = reportDepositCache.listPropertiesFiles(fileName);
        while (iter.hasNext()) {
            File f = iter.next();
            String path = f.getAbsolutePath();
            f.delete();
            if (LOG.isDebugEnabled()) {
                LOG.debug("PropertyFile:\t" + path + "\tdeleted");
            }
        }
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {

        IStatus status = Status.OK_STATUS;

        try {
            Activator.inheritVeriniceContextState();
            String locale = getLocale();

            syncReportTemplates(locale);
        } catch (ReportDepositException e) {
            status = errorHandler(e);
        } catch (IOException e) {
            status = errorHandler(e);
        } catch (ReportTemplateServiceException e) {
            status = errorHandler(e);
        }

        return status;
    }

    private String getLocale() {
        return Locale.getDefault().getLanguage();
    }

    private IStatus errorHandler(Exception e) {
        IStatus status;
        String msg = "error while syncing report templates:\t" + e.getLocalizedMessage() + e.getStackTrace();
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
