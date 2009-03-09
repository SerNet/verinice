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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.List;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahme;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class FindRiskAnalysisListsByParentID extends GenericCommand {

	
	private static final String QUERY_FIND_BY_PARENT_ID = "from "
		+ FinishedRiskAnalysisLists.class.getName() + " as element "
		+ "where element.finishedRiskAnalysisId = ?";
	
	
	private Integer id;

	private FinishedRiskAnalysisLists foundLists;

	public FindRiskAnalysisListsByParentID(Integer id) {
		this.id = id;
	}

	public void execute() {
		List list = getDaoFactory().getDAO(RisikoMassnahme.class).findByQuery(QUERY_FIND_BY_PARENT_ID,
				new Integer[] {id});
		for (Object object : list) {
			this.foundLists = (FinishedRiskAnalysisLists) object;
		}
	}

	public FinishedRiskAnalysisLists getFoundLists() {
		return foundLists;
	}

}
