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
package sernet.verinice.report.service.commands;

import sernet.verinice.interfaces.GenericCommand;

/**
 * Loads and returns the report's title.
 * 
 * TODO: Somehow allow setting and storing the report's title in the application and then
 * provide access to that value via this command.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class LoadReportTitleCommand extends GenericCommand {

	public String getResult() {
		return "<h1>Information Technologie (IT)</h1><h1>Security Assessment at VW TEST - Company 1</h1><h1>Final Report</h1>";
	}

	@Override
	public void execute() {
		// TODO: Implement me.
	}

}
