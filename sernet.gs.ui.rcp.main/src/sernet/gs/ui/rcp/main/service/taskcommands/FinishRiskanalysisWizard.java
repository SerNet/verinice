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

import java.io.Serializable;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisListsHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class Finish xxxRiskanalysisWizard extends GenericCommand {

	private FinishedRiskAnalysisLists finishedRiskLists;
	private FinishedRiskAnalysis finishedRiskAnalysis;
	private boolean previousAnalysis;
	private CnATreeElement cnaElement;

	/**
	 * @param cnaElement 
	 * @param finishedRiskAnalysis
	 * @param finishedRiskLists
	 * @param previousAnalysis 
	 */
	public FinishRiskanalysisWizard(CnATreeElement cnaElement, FinishedRiskAnalysis finishedRiskAnalysis,
			FinishedRiskAnalysisLists finishedRiskLists, boolean previousAnalysis) {	
		this.finishedRiskLists = finishedRiskLists;
		this.finishedRiskAnalysis = finishedRiskAnalysis;
		this.previousAnalysis = previousAnalysis;
		this.cnaElement = cnaElement;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		if (!previousAnalysis) {
			try {				
				SaveElement<FinishedRiskAnalysis> saveCommand 
					= new SaveElement<FinishedRiskAnalysis>(finishedRiskAnalysis);
				saveCommand = getCommandService().executeCommand(saveCommand);
				finishedRiskAnalysis = saveCommand.getElement();

				IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOForObject(cnaElement);
				dao.reload(cnaElement, cnaElement.getDbId());
				cnaElement.addChild(finishedRiskAnalysis);
				
				// set correct parent for display in tree:
				for (GefaehrdungsUmsetzung gefaehrdung : finishedRiskLists.getNotOKGefaehrdungsUmsetzungen()) {
					gefaehrdung.setParent(finishedRiskAnalysis);
				}
				
				finishedRiskLists
						.setFinishedRiskAnalysisId(finishedRiskAnalysis
								.getDbId());
				
				FinishedRiskAnalysisListsHome.getInstance().saveNew(
						finishedRiskLists);
			} catch (Exception e) {
				ExceptionUtil.log(e,
						"Konnte neue Risikoanalyse nicht speichern.");
			}
		} else {
			try {
				for (GefaehrdungsUmsetzung umsetzung : objectsToDelete) {
					umsetzung.remove();
					CnAElementHome.getInstance().remove(umsetzung);
				}
				
				finishedRiskAnalysis = (FinishedRiskAnalysis) CnAElementHome.getInstance().update(finishedRiskAnalysis);
				finishedRiskLists = FinishedRiskAnalysisListsHome.getInstance().update(
						finishedRiskLists);
				
			} catch (Exception e) {
				ExceptionUtil
						.log(e,
								"Konnte Änderungen an vorheriger Risikoanalyse nicht speichern.");
			}
		}
		return true;
	}

}
