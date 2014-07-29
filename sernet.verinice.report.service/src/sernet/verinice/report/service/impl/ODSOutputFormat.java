package sernet.verinice.report.service.impl;

import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

public class ODSOutputFormat extends AbstractOutputFormat {

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

	@Override
	boolean isRenderOutput() {
		return true;
	}
	
	@Override
	IRenderOption createBIRTRenderOptions()
	{
	    RenderOption options = new RenderOption();
	    options.setOutputFormat("ods");
		
	    return options;
	}

}
