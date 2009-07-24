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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

public class FindMassnahmenForTodoView extends GenericCommand {

	private List<TodoViewItem> all = new ArrayList<TodoViewItem>(5000);
	private Integer dbId = null;

	
	public FindMassnahmenForTodoView(Integer dbId) {
		Logger.getLogger(this.getClass()).debug("Looking up Massnahme for id " + dbId);
		this.dbId = dbId;
	}
	
	public FindMassnahmenForTodoView() {
		// default constructor
	}

	public void execute() {
		try {
			List<MassnahmenUmsetzung> alleMassnahmen;
			if (dbId != null) {
				MassnahmenUmsetzung found = getDaoFactory().getDAO(MassnahmenUmsetzung.class).findById(dbId);
				alleMassnahmen = new ArrayList<MassnahmenUmsetzung>(1);
				alleMassnahmen.add(found);
			} else {
				// else load all:
				alleMassnahmen = getDaoFactory().getDAO(MassnahmenUmsetzung.class).findAll();
			}
			
			// create display items:
			fillList(alleMassnahmen);
			
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}

	/**
	 * Initialize lazy loaded field values needed for the view.
	 * 
	 * @param all
	 * @throws CommandException 
	 */
	private void fillList(List<MassnahmenUmsetzung> alleMassnahmen) throws CommandException {
		for (MassnahmenUmsetzung mn : alleMassnahmen) {
			hydrate(mn);
			
			TodoViewItem item = new TodoViewItem();

			if (mn.getParent() instanceof GefaehrdungsUmsetzung)
				item.setParentTitle( // risikoanalyse.getparent()
						mn.getParent().getParent().getParent().getTitel());
			else
				item.setParentTitle(
						mn.getParent().getParent().getTitel());
			
			item.setTitel(mn.getTitel());
			item.setUmsetzung(mn.getUmsetzung());
			item.setUmsetzungBis(mn.getUmsetzungBis());
			item.setNaechsteRevision(mn.getNaechsteRevision());
			item.setRevisionDurch(mn.getRevisionDurch());
			
			
			
			if (mn.getUmsetzungDurch() != null && mn.getUmsetzungDurch().length()>0)
				item.setUmsetzungDurch(mn.getUmsetzungDurch());
			else {
				FindResponsiblePerson command = new FindResponsiblePerson(mn.getDbId(), MassnahmenUmsetzung.P_VERANTWORTLICHE_ROLLEN_UMSETZUNG);
				command = getCommandService().executeCommand(command);
				List<Person> foundPersons = command.getFoundPersons();
				if (foundPersons.size()==0)
					item.setUmsetzungDurch("");
				else {
					item.setUmsetzungDurch(getNames(foundPersons));
				}
			}
			
			
			item.setStufe(mn.getStufe());
			item.setUrl(mn.getUrl());
			item.setStand(mn.getStand());
			item.setDbId(mn.getDbId());
			
			
			all.add(item);
		}
	}

	private String getNames(List<Person> persons) {
		StringBuffer names = new StringBuffer();
		for (Iterator iterator = persons.iterator(); iterator.hasNext();) {
			Person person = (Person) iterator.next();
			names.append(person.getFullName());
			if (iterator.hasNext())
				names.append(", "); //$NON-NLS-1$
		}
		return names.toString();
	}

	private void hydrate(MassnahmenUmsetzung mn) {
		IBaseDao<MassnahmenUmsetzung, Serializable> dao = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
		dao.initialize(mn);
	}

	public List<TodoViewItem> getAll() {
		return all;
	}

}
