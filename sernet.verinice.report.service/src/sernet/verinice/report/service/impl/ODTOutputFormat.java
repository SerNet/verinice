package sernet.verinice.report.service.impl;

import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

public class ODTOutputFormat extends AbstractOutputFormat {

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

	@Override
	boolean isRenderOutput() {
		return true;
	}
	
	@Override
	IRenderOption createBIRTRenderOptions()
	{
	    RenderOption options = new RenderOption();
	    options.setOutputFormat("odt");
		
	    return options;
	}

}
