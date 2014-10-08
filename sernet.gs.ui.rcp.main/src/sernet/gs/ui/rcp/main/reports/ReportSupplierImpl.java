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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import sernet.gs.service.ReportTemplateUtil;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.verinice.interfaces.IReportDepositService;
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

    public ReportSupplierImpl() {
    }

    @Override
    public List<ReportTemplateMetaData> getReportTemplates() {
        try {
            return Arrays.asList(getReportMetaData());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReportMetaDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PropertyFileExistsException e) {
            e.printStackTrace();
        }
        return new ArrayList<ReportTemplateMetaData>(0);
    }

    private ReportTemplateMetaData[] getReportMetaData() throws IOException, ReportMetaDataException, PropertyFileExistsException {

        ReportTemplateUtil localReportTemplateUtil = new ReportTemplateUtil(CnAWorkspace.getInstance().getLocalReportTemplateDir());
        ReportTemplateUtil serverReportTemplateUtil = new ReportTemplateUtil(CnAWorkspace.getInstance().getRemoteReportTemplateDir(), true);

        Set<ReportTemplateMetaData> metadata = new HashSet<ReportTemplateMetaData>();
        metadata.addAll(localReportTemplateUtil.getReportTemplates(localReportTemplateUtil.getReportTemplateFileNames()));
        metadata.addAll(serverReportTemplateUtil.getReportTemplates(serverReportTemplateUtil.getReportTemplateFileNames()));

        return metadata.toArray(new ReportTemplateMetaData[metadata.size()]);
    }

}
