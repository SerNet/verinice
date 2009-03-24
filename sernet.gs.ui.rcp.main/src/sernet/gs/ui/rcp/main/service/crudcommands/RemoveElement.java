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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.FindRiskAnalysisListsByParentID;

public class RemoveElement<T extends CnATreeElement> extends GenericCommand
	implements IChangeLoggingCommand {

	private T element;
	private String stationId;
	private Class<? extends CnATreeElement> elementClass;
	private Integer elementId;

	public RemoveElement(T element) {
		// only transfer id of element to keep footprint small:
		elementClass = element.getClass();
		elementId = element.getDbId();
		
		this.stationId = ChangeLogEntry.STATION_ID;
	}
	
	public void execute() {
			try {
				// load element from DB:
				this.element = (T) getDaoFactory().getDAO(elementClass).findById(elementId);
				
				if (element instanceof Person)
					removeConfiguration((Person) element);
				
				int listsDbId = 0;
				if (element instanceof GefaehrdungsUmsetzung) {
					listsDbId = element.getParent().getDbId();
				}
				
				IBaseDao dao =  getDaoFactory().getDAOForObject(element);
				element = (T) dao.findById(element.getDbId());

				if (element instanceof ITVerbund) {
					Set<CnATreeElement> personen = ((ITVerbund) element).getCategory(PersonenKategorie.TYPE_ID).getChildren();
					for (CnATreeElement elmt : personen) {
						removeConfiguration((Person)elmt);
					}
				}
				
				if (element instanceof FinishedRiskAnalysis) {
					FinishedRiskAnalysis analysis = (FinishedRiskAnalysis) element;
					remove(analysis);
				}
				
				if (element instanceof GefaehrdungsUmsetzung) {
					GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) element;
					removeFromLists(listsDbId, gef);
				}

				element.remove();
				dao.delete(element);
			} catch (CommandException e) {
				throw new RuntimeCommandException(e);
			}
			
		
// FIXME server: create bulk delete to speed up deletion of objects, also have another look into hibernate option on-delete="cascade"
//		String query = "delete from CnATreeElement as elmt where elmt.dbId = ?";
//		Integer dbId = element.getDbId();
//		int rows = dao.updateByQuery(
//				query, 
//				new Object[] { dbId } );
//		Logger.getLogger(this.getClass()).debug("Deleted rows: " + rows);
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		element = null;
	}

	/**
	 * @param analysis
	 * @throws CommandException 
	 */
	private void remove(FinishedRiskAnalysis analysis) throws CommandException {
		Set<CnATreeElement> children = analysis.getChildren();
		for (CnATreeElement child : children) {
			if (child instanceof GefaehrdungsUmsetzung) {
				GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) child;
				removeFromLists(gef.getParent().getDbId(), gef);
			}
		}
	}

	/**
	 * Remove from all referenced lists.
	 * 
	 * @param element2
	 * @throws CommandException 
	 */
	private void removeFromLists(int analysisId, GefaehrdungsUmsetzung gef) throws CommandException {
		FindRiskAnalysisListsByParentID command = new FindRiskAnalysisListsByParentID(analysisId);
		getCommandService().executeCommand(command);
		FinishedRiskAnalysisLists lists = command.getFoundLists();
		lists.removeGefaehrdungCompletely(gef);
	}

	private void removeConfiguration(Person person) throws CommandException {
		LoadConfiguration command = new LoadConfiguration(person);
		command = getCommandService().executeCommand(command);
		Configuration conf = command.getConfiguration();
		if (conf != null) {
			IBaseDao<Configuration, Serializable> confDAO = getDaoFactory().getDAO(Configuration.class);
			confDAO.delete(conf);
		}
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType()
	 */
	public int getChangeType() {
		return ChangeLogEntry.TYPE_DELETE;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
		result.add(element);
		return result;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId()
	 */
	public String getStationId() {
		return stationId;
	}

}
