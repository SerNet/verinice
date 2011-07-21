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

	@Override
	boolean isRenderOutput() {
		return true;
	}

}
