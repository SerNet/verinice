/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;

public abstract class Report implements Serializable {

	private BSIModel model;
	protected Properties reportProperties;

	public Report(Properties reportProperties) {
		this.reportProperties = reportProperties;
	}

	/**
	 * Check if list of default columns for export contains the given column.
	 * 
	 */
	public boolean isDefaultColumn(String property_id) {
		String prop = reportProperties.getProperty(getClass().getSimpleName());
		if (prop == null)
			return false;
		return (prop.indexOf(property_id) > -1 );
	}

	public BSIModel getModel() {
		return this.model;
	}
	
	public void setModel(BSIModel model) {
		this.model = model;
	}

}
