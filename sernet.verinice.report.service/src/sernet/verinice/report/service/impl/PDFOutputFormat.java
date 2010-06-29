package sernet.verinice.report.service.impl;

import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.PDFRenderOption;


class PDFOutputFormat extends AbstractOutputFormat {

	@Override
	public String getFileSuffix() {
		return "pdf";
	}

	@Override
	public String getId() {
		return "pdf";
	}

	@Override
	public String getLabel() {
		return "Portable Document Format (PDF)";
	}
	
	@Override
	IRenderOption createBIRTRenderOptions()
	{
		PDFRenderOption pdfOptions = new PDFRenderOption();
		pdfOptions.setOutputFormat("pdf");
		pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.OUTPUT_TO_MULTIPLE_PAGES);

		return pdfOptions;
	}

	@Override
	boolean isRenderOutput() {
		return true;
	}

}
