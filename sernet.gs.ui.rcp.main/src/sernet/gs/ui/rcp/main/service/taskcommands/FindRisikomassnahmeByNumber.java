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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.List;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;

public class FindRisikomassnahmeByNumber extends GenericCommand {

	private String number;

	private RisikoMassnahme massnahme;
	
	private static final String QUERY_FIND_BY_ID = "from "
		+ RisikoMassnahme.class.getName() + " as element "
		+ "where element.number = ?";

	public FindRisikomassnahmeByNumber(String number) {
		this.number = number;
	}

	public void execute() {
		List list = getDaoFactory().getDAO(RisikoMassnahme.class).findByQuery(QUERY_FIND_BY_ID, 
				new String[] {number});
		for (Object object : list) {
			this.massnahme = (RisikoMassnahme) object;
		}
	}

	public RisikoMassnahme getMassnahme() {
		return massnahme;
	}

}
