package sernet.verinice.report.service.impl;

import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;


public class HTMLOutputFormat extends AbstractOutputFormat {

	@Override
	public String getFileSuffix() {
		return "html";
	}

	@Override
	public String getId() {
		return "html";
	}

	@Override
	public String getLabel() {
		return "Hypertext Markup Language (HTML)";
	}
	
	@Override
	IRenderOption createBIRTRenderOptions()
	{
		HTMLRenderOption htmlOptions = new HTMLRenderOption();
		htmlOptions.setHtmlPagination(false);
		htmlOptions.setOutputFormat("html");
		htmlOptions.setImageDirectory(".");

		return htmlOptions;
	}

}
