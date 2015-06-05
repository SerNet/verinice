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
package sernet.verinice.service.commands;

import java.util.List;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;
import sernet.verinice.model.common.HydratorUtil;

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
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		HydratorUtil.hydrateElement(getDaoFactory().getDAO(FinishedRiskAnalysisLists.class), foundLists);
	}

	public FinishedRiskAnalysisLists getFoundLists() {
		return foundLists;
	}

}
