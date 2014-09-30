package sernet.verinice.model.report;

import sernet.verinice.interfaces.report.IOutputFormat;

public class ODSOutputFormat implements IOutputFormat {

	@Override
	public String getLabel() {
		return "Open Document Spreadsheet (ODS) ";
	}

	@Override
	public String getId() {
		return "ods";
	}

	@Override
	public String getFileSuffix() {
		return "ods";
	}

}
