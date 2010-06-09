package sernet.verinice.report.service.impl;

import org.eclipse.birt.report.engine.api.IRenderOption;

public interface IOutputFormat {
	
	String getLabel();
	
	String getId();
	
	String getFileSuffix();
	
	IRenderOption createBIRTRenderOptions();
}
