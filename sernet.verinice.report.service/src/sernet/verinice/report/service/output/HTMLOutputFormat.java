package sernet.verinice.report.service.output;

import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;

import sernet.verinice.report.service.impl.IOutputFormat;

public class HTMLOutputFormat implements IOutputFormat {

	public String getFileSuffix() {
		return ".html";
	}

	public String getId() {
		return "html";
	}

	public String getLabel() {
		return "Hypertext Markup Language (HTML)";
	}
	
	public IRenderOption createBIRTRenderOptions()
	{
		HTMLRenderOption htmlOptions = new HTMLRenderOption();
		htmlOptions.setHtmlPagination(false);
		htmlOptions.setOutputFormat("html");
		htmlOptions.setImageDirectory(".");

		return htmlOptions;
	}

}
