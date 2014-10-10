/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.ReportTemplateUtil;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.report.IReportService;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * Collects meta data of verinice reports from 2 locations:
 *
 * 1. {@link IReportService#VERINICE_REPORTS_REMOTE}
 *
 * 2. {@link IReportService#VERINICE_REPORTS_LOCAL}
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ReportSupplierImpl implements IReportSupplier {
   

    private final String ERROR_MESSAGE = "reading report templates failed %s";

    private final Logger LOG = Logger.getLogger(ReportSupplierImpl.class);

    public ReportSupplierImpl() {
    }

    @Override
    public List<ReportTemplateMetaData> getReportTemplates(String locale) {
        try {
            return Arrays.asList(getReportMetaData(locale));
        } catch (IOException e) {
            LOG.error(String.format(ERROR_MESSAGE, e.getLocalizedMessage()), e);
        } catch (ReportMetaDataException e) {
            LOG.error(String.format(ERROR_MESSAGE, e.getLocalizedMessage()), e);
        } catch (PropertyFileExistsException e) {
            LOG.error(String.format(ERROR_MESSAGE, e.getLocalizedMessage()), e);
        }
        return new ArrayList<ReportTemplateMetaData>(0);
    }

    private ReportTemplateMetaData[] getReportMetaData(String locale) throws IOException, ReportMetaDataException, PropertyFileExistsException {

        ReportTemplateUtil localReportTemplateUtil = new ReportTemplateUtil(getLocalReportPath());
        ReportTemplateUtil serverReportTemplateUtil = new ReportTemplateUtil(CnAWorkspace.getInstance().getRemoteReportTemplateDir(), true);

        Set<ReportTemplateMetaData> metadata = new HashSet<ReportTemplateMetaData>();
        int size = 0;
        metadata.addAll(localReportTemplateUtil.getReportTemplates(localReportTemplateUtil.getReportTemplateFileNames(), locale));
        size = metadata.size();
        metadata.addAll(serverReportTemplateUtil.getReportTemplates(serverReportTemplateUtil.getReportTemplateFileNames(), locale));
        if(LOG.isDebugEnabled()){
            LOG.debug(size + " Report templates loaded from workspacefolder:\t" + IReportService.VERINICE_REPORTS_LOCAL);
            LOG.debug(metadata.size() - size + " Report templates loaded from workspacefolder:\t" + IReportService.VERINICE_REPORTS_REMOTE);
        }
        
        return metadata.toArray(new ReportTemplateMetaData[metadata.size()]);
    }

    private String getLocalReportPath() {
        String templateDir = Activator.getDefault().getIReportTemplateDirectoryService().getDirectory();

        if (templateDir == null) {
            templateDir = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.REPORT_LOCAL_TEMPLATE_DIRECTORY);
        } else {
            Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.REPORT_LOCAL_TEMPLATE_DIRECTORY, templateDir);
        }
        return templateDir;
    }

}
