package sernet.verinice.report.service.impl;

import org.eclipse.birt.report.engine.api.DataExtractionOption;
import org.eclipse.birt.report.engine.api.IDataExtractionOption;


class CSVOutputFormat extends AbstractOutputFormat {

	@Override
	public String getFileSuffix() {
		return "csv";
	}

	@Override
	public String getId() {
		return "csv";
	}

	@Override
	public String getLabel() {
		return "Comma-separated values (CSV)";
	}
	
	@Override
	IDataExtractionOption createBIRTExtractionOptions()
	{
		DataExtractionOption options = new DataExtractionOption();
		options.setOutputFormat("csv");

		return options;
	}

	@Override
	boolean isRenderOutput() {
		return false;
	}

}
