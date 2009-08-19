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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.AuditView;
import sernet.gs.ui.rcp.main.bsi.views.TodoView;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

/**
 * This command loads {@link MassnahmenUmsetzung} instances out of the databse 
 * and wraps them in {@link TodoViewItem}s.
 * 
 * <p>Lists of those objects are needed in the {@link AuditView} and {@link TodoView}.</p>
 * 
 * <p>Since those views should only show the {@link MassnahmenUmsetzung} items for a specific
 * IT-Verbund, this command reflects this behavior.</p>
 * 
 * @author r.schuster@tarent.de
 *
 */
public class FindMassnahmenForITVerbund extends GenericCommand {
	
	private static final long serialVersionUID = -629395120227759190L;

	private static final Logger log = Logger.getLogger(FindMassnahmenForITVerbund.class);

	private List<TodoViewItem> all = new ArrayList<TodoViewItem>(5000);
	private Integer dbId = null;

	private Integer massnahmeId = null;
	
	public FindMassnahmenForITVerbund(Integer dbId) {
		this(dbId, null);
	}
	
	public FindMassnahmenForITVerbund(Integer dbId, Integer massnahmeId) {
		Logger.getLogger(this.getClass()).debug("Looking up Massnahme for IT-Verbund " + dbId);
		this.dbId = dbId;
		this.massnahmeId = massnahmeId;
	}

	public void execute() {
		try {
			ITVerbund verbund = getDaoFactory().getDAO(ITVerbund.class).findById(dbId);
			
			List<MassnahmenUmsetzung> list = new ArrayList<MassnahmenUmsetzung>();
			retrieveMassnahmen(verbund, list);
			
			// create display items:
			fillList(list);
			
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}
	
	private void retrieveMassnahmen(CnATreeElement parent, List<MassnahmenUmsetzung> list)
	{
		for (CnATreeElement child : parent.getChildren())
		{
			if (child instanceof MassnahmenUmsetzung)
				list.add((MassnahmenUmsetzung) child);
			else
				retrieveMassnahmen(child, list);
		}
	}

	/**
	 * Initialize lazy loaded field values needed for the view.
	 * 
	 * @param all
	 * @throws CommandException 
	 */
	private void fillList(List<MassnahmenUmsetzung> alleMassnahmen) throws CommandException {
		int count = 0;
		for (MassnahmenUmsetzung mn : alleMassnahmen) {
			log.debug("Processing Massnahme: " + ++count);
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
				command = FindMassnahmenForITVerbund.this.getCommandService().executeCommand(command);
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
