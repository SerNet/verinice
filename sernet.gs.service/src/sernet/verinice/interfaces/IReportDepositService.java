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
package sernet.verinice.interfaces;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 *
 */
public interface IReportDepositService {

    public enum OutputFormat{
        PDF, HTML, XLS, ODS, ODT, DOC
    }
    
    public static final String PROPERTIES_FILE_EXTENSION = "properties";
    public static final char EXTENSION_SEPARATOR_CHAR = FilenameUtils.EXTENSION_SEPARATOR;
    
    public static final String PROPERTIES_FILENAME = "filename";
    public static final String PROPERTIES_OUTPUTFORMATS = "outputformats";
    public static final String PROPERTIES_OUTPUTNAME = "outputname";
    
    public static final String REPORT_DEPOSIT_CLIENT_LOCAL = "report_templates_local";
    public static final String REPORT_DEPOSIT_CLIENT_REMOTE = "report_templates_remote";
    
    IOutputFormat getOutputFormat(OutputFormat format);
    IOutputFormat[] getOutputFormats(OutputFormat[] format);
    ReportTemplateMetaData[] getReportTemplates(String[] rptDesignFiles, boolean isServer) throws IOException, ReportMetaDataException, PropertyFileExistsException;
    ReportTemplateMetaData getMetaData(File rptDesign, boolean isServer) throws IOException, ReportMetaDataException, PropertyFileExistsException;
    void addToServerDeposit(ReportTemplateMetaData metadata, byte[] file);
    void removeFromServer(ReportTemplateMetaData metadata) throws IOException;
    ReportTemplateMetaData[] getServerReportTemplates() throws IOException, ReportMetaDataException, PropertyFileExistsException;

}
