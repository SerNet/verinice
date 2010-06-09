package sernet.verinice.report.service.output;

import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.PDFRenderOption;

import sernet.verinice.report.service.impl.IOutputFormat;

public class PDFOutputFormat implements IOutputFormat {

	public String getFileSuffix() {
		return "pdf";
	}

	public String getId() {
		return "pdf";
	}

	public String getLabel() {
		return "Portable Document Format (PDF)";
	}
	
	public IRenderOption createBIRTRenderOptions()
	{
		PDFRenderOption pdfOptions = new PDFRenderOption();
		pdfOptions.setOutputFileName("output/bsh-networking.pdf");
		pdfOptions.setOutputFormat("pdf");
		pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE | IPDFRenderOption.OUTPUT_TO_MULTIPLE_PAGES);

		return pdfOptions;
	}

}
