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

import org.eclipse.birt.report.engine.api.DataExtractionOption;
import org.eclipse.birt.report.engine.api.IDataExtractionOption;


class CSVOutputFormat extends AbstractOutputFormat {

	@Override
	public String getFileSuffix() {
		return "xls";
	}

	@Override
	public String getId() {
		return "csv";
	}

	@Override
	public String getLabel() {
		return "Excel export (CSV)";
	}
	
	@Override
	IDataExtractionOption createBIRTExtractionOptions()
	{
		DataExtractionOption options = new DataExtractionOption();
		options.setOutputFormat("csv");

		return options;
	}

	@Override
	boolean isRenderOutput() {
		return false;
	}

}
