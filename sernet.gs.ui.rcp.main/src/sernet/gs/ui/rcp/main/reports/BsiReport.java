/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.io.Serializable;
import java.util.Properties;

import sernet.verinice.model.bsi.ITVerbund;

@SuppressWarnings("serial")
public abstract class BsiReport implements Serializable, IBSIReport {

	private Properties reportProperties;
	private ITVerbund itverbund;
	
	public ITVerbund getItverbund() {
		return itverbund;
	}

	public void setItverbund(ITVerbund itverbund) {
		this.itverbund = itverbund;
	}

	public BsiReport(Properties reportProperties) {
		this.reportProperties = reportProperties;
	}

	/**
	 * Check if list of default columns for export contains the given column.
	 * 
	 */
	public boolean isDefaultColumn(String propertyId) {
		String prop = reportProperties.getProperty(getClass().getSimpleName());
		if (prop == null){
			return false;
		}
		return (prop.indexOf(propertyId) > -1 );
	}


}
