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
package sernet.verinice.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.model.report.ExcelOutputFormat;
import sernet.verinice.model.report.HTMLOutputFormat;
import sernet.verinice.model.report.ODSOutputFormat;
import sernet.verinice.model.report.ODTOutputFormat;
import sernet.verinice.model.report.PDFOutputFormat;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplate;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.model.report.WordOutputFormat;

/**
 *
 */
public class DummyReportDepositService implements IReportDepositService {

    
    private static final Logger LOG = Logger.getLogger(DummyReportDepositService.class);

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#addToServerDeposit(sernet.verinice.model.report.ReportTemplateMetaData, byte[])
     */
    @Override
    public void addToServerDeposit(ReportTemplateMetaData metadata, byte[] file) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#removeFromServer(sernet.verinice.model.report.ReportTemplateMetaData)
     */
    @Override
    public void removeFromServer(ReportTemplateMetaData metadata) throws IOException {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getServerReportTemplates()
     */
    @Override
    public Set<ReportTemplateMetaData> getServerReportTemplates() throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return new TreeSet<ReportTemplateMetaData>();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getOutputFormat(sernet.verinice.interfaces.IReportDepositService.OutputFormat)
     */
    @Override
    public IOutputFormat getOutputFormat(OutputFormat formatLabel) {
        switch(formatLabel){
        case PDF: return new PDFOutputFormat();
        case HTML: return new HTMLOutputFormat();
        case ODS: return new ODSOutputFormat();
        case ODT: return new ODTOutputFormat();
        case XLS: return new ExcelOutputFormat();
        case DOC: return new WordOutputFormat();
        default: return null;
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getOutputFormats(sernet.verinice.interfaces.IReportDepositService.OutputFormat[])
     */
    @Override
    public IOutputFormat[] getOutputFormats(OutputFormat[] formatLabel) {
        List<IOutputFormat> list = new ArrayList<IOutputFormat>(formatLabel.length);
        for (OutputFormat s : formatLabel) {
            IOutputFormat format = getOutputFormat(s);
            if (format != null) {
                list.add(format);
            } else {
                LOG.warn("Report output format:\t" + s + " not available in verinice");
            }
        }
        return list.toArray(new IOutputFormat[list.size()]);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getDepositLocation()
     */
    @Override
    public String getDepositLocation() throws IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getReportTemplates(java.lang.String[])
     */
    @Override
    public Set<ReportTemplateMetaData> getReportTemplates(String[] rptDesignFiles) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getMetaData(java.io.File)
     */
    @Override
    public ReportTemplateMetaData getMetaData(File rptDesign) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#updateInServerDeposit(sernet.verinice.model.report.ReportTemplateMetaData)
     */
    @Override
    public void updateInServerDeposit(ReportTemplateMetaData metadata) throws IOException {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getReportTemplate(sernet.verinice.model.report.ReportTemplateMetaData)
     */
    @Override
    public ReportTemplate getReportTemplate(ReportTemplateMetaData metadata) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
