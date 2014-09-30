package sernet.verinice.model.report;

import sernet.verinice.interfaces.report.IOutputFormat;


public class ODTOutputFormat implements IOutputFormat {

	@Override
	public String getLabel() {
		return "Open Document Text (ODT) ";
	}

	@Override
	public String getId() {
		return "odt";
	}

	@Override
	public String getFileSuffix() {
		return "odt";
	}

}
