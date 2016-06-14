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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.VariableElementHandle;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.model.report.AbstractOutputFormat;
import sernet.verinice.model.report.ExcelOutputFormat;
import sernet.verinice.model.report.HTMLOutputFormat;
import sernet.verinice.model.report.ODSOutputFormat;
import sernet.verinice.model.report.ODTOutputFormat;
import sernet.verinice.model.report.PDFOutputFormat;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.model.report.WordOutputFormat;
import sernet.verinice.report.service.impl.security.ReportSecurityManager;
import sernet.verinice.security.report.ReportSecurityContext;

/**
 * with introduction of the the report-repository, this class gets annotated 
 * deprecated, because the user defined report is not existant anymore. the user is now
 * able to define the content of the report combo box of GenerateReportDialog itself, and every
 * selection of that combo gets executed with {@link GenericReportType} 
 * @author Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */

@Deprecated
public class UserReportType implements IReportType {
	
	private static final Logger LOG = Logger.getLogger(UserReportType.class);
	
	private static final String VAR_ENGINE_ITERATIONS = "engineIterations";
	private static final String PROPERTYHANDLE_PAGEVARIABLE = "pageVariables";
	
    private URL reportDocument;
	
	private String reportFile = ""; //$NON-NLS-1$

	/**
     * @return the reportFile
     */
    @Override
    public String getReportFile() {
        return reportFile;
    }

    /**
     * @param reportFile the reportFile to set
     */
    @Override
    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }
    
    @Override
    public String getDescription() {
		return Messages.UserReportType_1;
	}

	@Override
    public String getId() {
		return USER_REPORT_ID; 
	}

	@Override
    public String getLabel() {
		return Messages.UserReportType_2;
	}
	
	@Override
    public IOutputFormat[] getOutputFormats() {
        return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat(), new ExcelOutputFormat(), new WordOutputFormat(), new ODTOutputFormat(), new ODSOutputFormat() };
    }

	@Override
    public void createReport(IReportOptions reportOptions) {
		BIRTReportService brs = new BIRTReportService();
	    URL reportDesign;

        try {
            reportDesign = (new File(reportFile)).toURI().toURL();
            
        } catch (MalformedURLException e) {
            LOG.error("Could not load user supplied report file.", e); //$NON-NLS-1$
            throw new RuntimeException("Could not load user report file.", e); //$NON-NLS-1$
        } catch (IOException e) {
            LOG.error("Could not create reportDocument tmpFile.", e); //$NON-NLS-1$
            throw new RuntimeException("Could create tmpFile for reportDocument.", e); //$NON-NLS-1$
        }

        if (((AbstractOutputFormat) reportOptions.getOutputFormat()).isRenderOutput())
        {
            ReportSecurityContext reportSecurityContext = new ReportSecurityContext(reportOptions, reportDesign, brs.getLogfile(), null);
            ReportSecurityManager secureReportExecutionManager = new ReportSecurityManager(reportSecurityContext);
            IRunAndRenderTask task = brs.createTask(reportSecurityContext.getRptDesignUrl());
            task = brs.prepareTaskForRendering(task, reportOptions);
            int iterations = getEngineIterations(task);
            if(LOG.isDebugEnabled()){
                LOG.debug("EngineIterations for UserTypeReport:\t" + iterations);
            }
            for(int i = 0;i < iterations; i++){
                brs.performRenderTask(task, secureReportExecutionManager);
            }

		    // just in case a toc was generated, the next toc should start emtpy again
		    TocHelper2.reset();
		}
		else
		{
		    // this should be @deprecated, since CSV output format isn't supported anymore
			IDataExtractionTask task = brs.createExtractionTask(reportDesign);
			// in a user report, only one table should be present for the CSV report (first one found is used):
			brs.extract(task, reportOptions, 1);
		}
	}
	
	private IRenderTask runAndRenderReport(int timesToRun, int timesToRender, IRunTask runTask, IReportOptions reportOptions, BIRTReportService brs){
	    try{
	        for(int i = 0;i < timesToRun; i++){
	            brs.run(runTask, reportOptions);
	        }
	    } catch (Exception e){
	            LOG.error("error while running report", e);
	    }
	    IRenderTask renderTask = brs.createRenderTask(reportDocument);
	    for(int i = 0;i < timesToRender; i++){
	        brs.render(renderTask, reportOptions);
	    } 
	    return renderTask;

	}

	@Override
	public String getUseCaseID() {
		return IReportType.USE_CASE_ID_ALWAYS_REPORT;
	}
	
	
	/**
	 * if a report should run more than once (n-times), the user has to define a ReportVariable called
	 * "engineIterations" and set it's value to n. This method extracts n.
	 * more than one engine iterations are needed if the user wants to fill a toc in his report (e.g.)
	 * @param task
	 * @return
	 */
	private int getEngineIterations(IRunAndRenderTask task){
	    ReportDesignHandle dh = (ReportDesignHandle)task.getReportRunnable().getDesignHandle();
	    Iterator<Object> iter = dh.getPropertyIterator();
	    try{
	        while(iter.hasNext()){
	            Object o = iter.next();
	            PropertyHandle ph = (PropertyHandle)o;
	            if(ph.getPropertyDefn().getName().equals(PROPERTYHANDLE_PAGEVARIABLE)){
	                for(Object item : ph.getContents()){
	                    if(item instanceof VariableElementHandle){
	                        VariableElementHandle veh = (VariableElementHandle)item;
	                        if(veh.getVariableName().equals(VAR_ENGINE_ITERATIONS)){
	                            return Integer.parseInt(veh.getValue());
	                        }
	                    }
	                }
	            }
	        }
	    } catch (NumberFormatException t){
	        LOG.error("Error while determing number of engine iterations", t);
	    }
	    return 1; // default / no ReportVariable found
	}
	
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#createReport(sernet.verinice.model.report.ReportTemplate)
     */
    @Override
    public void createReport(ReportTemplateMetaData report) {
        // nothing here
    }
}
