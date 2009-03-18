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
package sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class StartNewRiskAnalysis extends GenericCommand {

	private CnATreeElement cnaElement;
	private FinishedRiskAnalysis finishedRiskAnalysis;
	private FinishedRiskAnalysisLists finishedRiskLists;

	/**
	 * @param cnaElement
	 */
	public StartNewRiskAnalysis(CnATreeElement cnaElement) {
		this.cnaElement = cnaElement;
		
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		getDaoFactory().getDAOForObject(cnaElement).reload(cnaElement, cnaElement.getDbId());
		finishedRiskAnalysis = new FinishedRiskAnalysis(cnaElement);
		getDaoFactory().getDAO(FinishedRiskAnalysis.class).saveOrUpdate(finishedRiskAnalysis);
		cnaElement.addChild(finishedRiskAnalysis);

		finishedRiskLists = new FinishedRiskAnalysisLists();
		finishedRiskLists.setFinishedRiskAnalysisId(finishedRiskAnalysis.getDbId());
		getDaoFactory().getDAO(FinishedRiskAnalysisLists.class).saveOrUpdate(finishedRiskLists);
	}

	public FinishedRiskAnalysis getFinishedRiskAnalysis() {
		return finishedRiskAnalysis;
	}

	public FinishedRiskAnalysisLists getFinishedRiskLists() {
		return finishedRiskLists;
	}

}
