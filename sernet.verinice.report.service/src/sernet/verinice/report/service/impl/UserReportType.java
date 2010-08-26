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
package sernet.verinice.report.service.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;

public class UserReportType implements IReportType {
	
	private static final Logger LOG = Logger.getLogger(UserReportType.class);
	
	private String reportFile = "";

	/**
     * @return the reportFile
     */
    public String getReportFile() {
        return reportFile;
    }

    /**
     * @param reportFile the reportFile to set
     */
    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }
    
// FIXME externalize strings
    
    public String getDescription() {
		return "User supplied custom report.";
	}

	public String getId() {
		return "user";
	}

	public String getLabel() {
		return "Report from file";
	}

	public IOutputFormat[] getOutputFormats() {
		return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat(), new CSVOutputFormat() };
	}

	public void createReport(IReportOptions reportOptions) {
		BIRTReportService brs = new BIRTReportService();
		
		
		URL reportDesign;
        try {
            reportDesign = (new File(reportFile)).toURI().toURL();
        } catch (MalformedURLException e) {
            LOG.error("Could not load user supplied report file.", e);
            throw new RuntimeException("Could not load user report file.", e);
        }
		
		if (((AbstractOutputFormat) reportOptions.getOutputFormat()).isRenderOutput())
		{
			IRunAndRenderTask task = brs.createTask(reportDesign);
			brs.render(task, reportOptions);
		}
		else
		{
			IDataExtractionTask task = brs.createExtractionTask(reportDesign);
			// in a user report, only one table should be present for the CSV report (first one found is used):
			brs.extract(task, reportOptions, 1);
		}
	}

}
