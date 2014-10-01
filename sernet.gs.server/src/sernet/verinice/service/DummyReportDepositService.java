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

import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 *
 */
public class DummyReportDepositService implements IReportDepositService {


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getReportTemplates(java.lang.String[], boolean)
     */
    @Override
    public ReportTemplateMetaData[] getReportTemplates(String[] rptDesignFiles, boolean isServer) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return new ReportTemplateMetaData[]{};
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getMetaData(java.io.File, boolean)
     */
    @Override
    public ReportTemplateMetaData getMetaData(File rptDesign, boolean isServer) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return null;
    }

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
    public ReportTemplateMetaData[] getServerReportTemplates() throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return new ReportTemplateMetaData[]{};
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getOutputFormat(sernet.verinice.interfaces.IReportDepositService.OutputFormat)
     */
    @Override
    public IOutputFormat getOutputFormat(OutputFormat format) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getOutputFormats(sernet.verinice.interfaces.IReportDepositService.OutputFormat[])
     */
    @Override
    public IOutputFormat[] getOutputFormats(OutputFormat[] format) {
        return null;
    }

}
