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
package sernet.verinice.report.rcp;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import static org.apache.commons.io.FilenameUtils.concat;

import sernet.gs.service.ReportTemplateUtil;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplate;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * Copies the remote templates into a the local server report templates folder.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class RemoteReportTemplatesSync {

    private ReportTemplateUtil clientServerReportTemplateUtil = new ReportTemplateUtil(CnAWorkspace.getInstance().getRemoteReportTemplateDir());

    public void syncReportTemplates() throws IOException, ReportMetaDataException, PropertyFileExistsException {

        String[] fileNames = clientServerReportTemplateUtil.getReportTemplateFileNames();
        Set<ReportTemplateMetaData> localServerTemplates = clientServerReportTemplateUtil.getReportTemplates(fileNames);
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
}
