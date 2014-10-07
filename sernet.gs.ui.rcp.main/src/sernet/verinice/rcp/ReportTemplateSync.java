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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import sernet.gs.service.ReportTemplateUtil;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplate;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ReportTemplateSync extends WorkspaceJob implements IModelLoadListener {

    private ReportTemplateUtil clientServerReportTemplateUtil = new ReportTemplateUtil(CnAWorkspace.getInstance().getRemoteReportTemplateDir());

    private static volatile IModelLoadListener modelLoadListener;

    private Logger LOG = Logger.getLogger(ReportTemplateSync.class);

    private ReportTemplateSync() {
        super("sync reports");
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

    private void syncReportTemplates() throws IOException, ReportMetaDataException, PropertyFileExistsException {

        String[] fileNames = clientServerReportTemplateUtil.getReportTemplateFileNames();
        Set<ReportTemplateMetaData> localServerTemplates = clientServerReportTemplateUtil.getReportTemplates(fileNames);
        IReportDepositService rds = getIReportDepositService();
        Set<ReportTemplateMetaData> remoteSeverTemplates = getIReportDepositService().getServerReportTemplates();

        for (ReportTemplateMetaData remoteTemplateMetaData : remoteSeverTemplates) {
            if (!localServerTemplates.contains(remoteTemplateMetaData)) {
                syncTemplate(remoteTemplateMetaData);
            }
        }
    }

    private IReportDepositService getIReportDepositService() {
        return ServiceFactory.lookupReportDepositService();
    }

    private void syncTemplate(ReportTemplateMetaData metadata) throws IOException {
        ReportTemplate template = getIReportDepositService().getReportTemplate(metadata);
        String directory = CnAWorkspace.getInstance().getRemoteReportTemplateDir();
        File rptdesignTemplate = new File(concat(directory, template.getMetaData().getFilename()));
        FileUtils.writeByteArrayToFile(rptdesignTemplate, template.getRptdesignFile());

        for (Entry<String, byte[]> e : template.getPropertiesFiles().entrySet()) {
            FileUtils.writeByteArrayToFile(new File(concat(directory, e.getKey())), e.getValue());
        }
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {

        IStatus status = Status.OK_STATUS;

        try {
            Activator.inheritVeriniceContextState();
            syncReportTemplates();
        } catch (IOException e) {
            status = errorHandler(e);
        } catch (ReportMetaDataException e) {
            status = errorHandler(e);
            e.printStackTrace();
        } catch (PropertyFileExistsException e) {
            status = errorHandler(e);
        }

        return status;
    }

    private IStatus errorHandler(Exception e) {
        IStatus status;
        String msg = "error while syncing report templates";
        LOG.error(msg);
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

}
