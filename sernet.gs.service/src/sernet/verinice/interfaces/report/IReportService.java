/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.report;

import static org.apache.commons.io.FilenameUtils.concat;

import java.io.IOException;

import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplateMetaData;

public interface IReportService {
    
    String VERINICE_REPORTS_LOCAL = concat(concat(System.getProperty(
            IVeriniceConstants.USER_HOME),
            "verinice"),
            "report_templates_local"); //$NON-NLS-1$
    
    String VERINICE_REPORTS_REMOTE = "report_templates_remote"; //$NON-NLS-1$


	IReportType[] getReportTypes();
	
	IOutputFormat getOutputFormat(String formatLabel);
	
	IOutputFormat[] getOutputFormats(String[] formatLabel);
	
	ReportTemplateMetaData[] getReportTemplates(String[] rptDesignFiles) throws IOException, ReportMetaDataException, PropertyFileExistsException;
	
	public Object getRenderOptions(String format);
}
