/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
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
