package sernet.verinice.report.service.impl;

import java.net.URL;

import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

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

public class SamtComplianceReport implements IReportType {

	@Override
	public String getId() {
		return "ControlMaturityReport";
	}

	@Override
	public String getLabel() {
		return Messages.SamtComplianceReport_2;
		
	}

	@Override
	public String getDescription() {
		return Messages.SamtComplianceReport_0;
	}

	@Override
	public IOutputFormat[] getOutputFormats() {
		return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat(), new ExcelOutputFormat(), new WordOutputFormat(), new ODTOutputFormat(), new ODSOutputFormat() };
	}

	@Override
	public void createReport(IReportOptions reportOptions) {
		BIRTReportService brs = new BIRTReportService();
		
		URL reportDesign = ControlMaturityReport.class.getResource("samt-report-compliance.rptdesign"); //$NON-NLS-1$
		
		if (((AbstractOutputFormat) reportOptions.getOutputFormat()).isRenderOutput())
		{
			IRunAndRenderTask task = brs.createTask(reportDesign);
			brs.render(task, reportOptions);
		}
		else
		{
			IDataExtractionTask task = brs.createExtractionTask(reportDesign);
			brs.extract(task, reportOptions, 1);
		}
	}

	@Override
	public String getReportFile() {
		return Messages.SamtComplianceReport_3;
	}

	@Override
	public void setReportFile(String file) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getUseCaseID() {
		return IReportType.USE_CASE_ID_ALWAYS_REPORT;
	}
	
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportType#createReport(sernet.verinice.model.report.ReportTemplate)
     */
    @Override
    public void createReport(ReportTemplateMetaData report) {
        // nothing here
    }


}
