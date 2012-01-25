package sernet.verinice.report.service.impl;

import java.net.URL;

import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;

public class ControlMaturityReport implements IReportType {

	@Override
	public String getId() {
		return "ControlMaturityReport";
	}

	@Override
	public String getLabel() {
		return Messages.ControlMaturityReport_2;
	}

	@Override
	public String getDescription() {
		return Messages.ControlMaturityReport_0;
	}

	@Override
	public IOutputFormat[] getOutputFormats() {
        return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat(), new ExcelOutputFormat(), new WordOutputFormat(), new ODTOutputFormat(), new ODSOutputFormat() };
	}

	@Override
	public void createReport(IReportOptions reportOptions) {
		BIRTReportService brs = new BIRTReportService();
		
		URL reportDesign = ControlMaturityReport.class.getResource("Control-Maturity-report.rptdesign"); //$NON-NLS-1$
		
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
		// TODO Auto-generated method stub
		return Messages.ControlMaturityReport_3;
	}

	@Override
	public void setReportFile(String file) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getUseCaseID() {
		return IReportType.USE_CASE_ID_GENERAL_REPORT;
	}

}
