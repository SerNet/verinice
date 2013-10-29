/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.impl;

import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;


class ExcelOutputFormat extends AbstractOutputFormat {

	@Override
	public String getFileSuffix() {
		return "xls";
	}

	@Override
	public String getId() {
		return "xls";
	}

	@Override
	public String getLabel() {
		return "Excel Format (XLS)";
	}
	
	@Override
	IRenderOption createBIRTRenderOptions()
	{
	    EXCELRenderOption excelOptions = new EXCELRenderOption();
	    excelOptions.setOutputFormat("xls");
		return excelOptions;
	}

	@Override
	boolean isRenderOutput() {
		return true;
	}

}
