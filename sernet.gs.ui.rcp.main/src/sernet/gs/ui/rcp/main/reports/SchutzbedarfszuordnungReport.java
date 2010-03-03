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

import java.util.Properties;

/**
 * This report prints out the association of
 * protection levels with
 * IT assets such as clients, applications, rooms etc.
 * 
 * Programmatically this is just an asset report
 * with different output columns.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class SchutzbedarfszuordnungReport
	extends StrukturanalyseReport
	implements IBSIReport {

	
	public SchutzbedarfszuordnungReport(Properties reportProperties) {
		super(reportProperties); 
		// TODO Auto-generated constructor stub
	}

	public String getTitle() {
		return "[BSI] Schutzbedarfszuordnung";
	}

}
