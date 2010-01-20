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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.AuditView;
import sernet.gs.ui.rcp.main.bsi.views.TodoView;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.connect.RetrieveInfo;
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
@SuppressWarnings("serial")
public class FindMassnahmenForITVerbund extends GenericCommand {
	
	private static final Logger log = Logger.getLogger(FindMassnahmenForITVerbund.class);

	private List<TodoViewItem> all = new ArrayList<TodoViewItem>(2000);
	private Integer itverbundDbId = null;

	private Integer massnahmeId = null;
	
	private Set<String> executionSet;
	
	private Set<String> sealSet;
	
	@SuppressWarnings("serial")
	private class FindMassnahmenForITVerbundCallback implements
			HibernateCallback, Serializable {
		
		
		private Integer itverbundID;

		FindMassnahmenForITVerbundCallback(Integer itverbundID) {
			this.itverbundID = itverbundID;
		}

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			
			Query query = session.createQuery(
					"from MassnahmenUmsetzung mn " +
					"join fetch mn.entity " +
					"join fetch mn.parent.parent.entity " +
					"join fetch mn.parent.parent.parent.parent.entity " +
					"where mn.parent.parent.parent.parent = :id " +
					"or mn.parent.parent = :id2")
					.setInteger("id", itverbundID)
					.setInteger("id2", itverbundID);
			query.setReadOnly(true);
			List result = query.list();
			
			return result;
		}

	}
	
	public FindMassnahmenForITVerbund(Integer dbId) {
		this(dbId, null);
	}
	
	public FindMassnahmenForITVerbund(Integer dbId, Integer massnahmeId) {
		Logger.getLogger(this.getClass()).debug("Looking up Massnahme for IT-Verbund " + dbId);
		this.itverbundDbId = dbId;
		this.massnahmeId = massnahmeId;
	}

	public void execute() {
		try {
			long start = System.currentTimeMillis();
			List<MassnahmenUmsetzung> list = new ArrayList<MassnahmenUmsetzung>();
			IBaseDao<MassnahmenUmsetzung, Serializable> dao = getDaoFactory().getDAO(
					MassnahmenUmsetzung.class);
			list =  
				dao.findByCallback(new FindMassnahmenForITVerbundCallback(itverbundDbId));
			
			// create display items:
			fillList(list);
			if(log.isDebugEnabled()) {
				long runtime = System.currentTimeMillis() - start;
				log.debug("FindMassnahmenForITVerbund runtime: " + runtime + " ms.");
			}
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
		int count = 0;
		Set<UnresolvedItem> unresolvedItems = new HashSet<UnresolvedItem>();
		Set<MassnahmenUmsetzung> unresolvedMeasures = new HashSet<MassnahmenUmsetzung>();
		
		for (MassnahmenUmsetzung mn : alleMassnahmen) {
//			log.debug("Processing Massnahme: " + count);
//			hydrate(mn);
			
			String umsetzung = mn.getUmsetzung();
			String siegelStufe = String.valueOf(mn.getStufe());
			
			if((getExecutionSet()==null || getExecutionSet().contains(umsetzung)) &&
			   (getSealSet()==null || getSealSet().contains(siegelStufe))) {
			
				TodoViewItem item = new TodoViewItem();
	
				if (mn.getParent() instanceof GefaehrdungsUmsetzung)
					item.setParentTitle( // risikoanalyse.getparent()
							mn.getParent().getParent().getParent().getTitle());
				else
					item.setParentTitle(
							mn.getParent().getParent().getTitle());
				
				item.setTitel(mn.getTitle());
				item.setUmsetzung(umsetzung);
				item.setUmsetzungBis(mn.getUmsetzungBis());
				item.setNaechsteRevision(mn.getNaechsteRevision());
				
				item.setStufe(siegelStufe.charAt(0));
				item.setUrl(mn.getUrl());
				item.setStand(mn.getStand());
				item.setDbId(mn.getDbId());
			
				unresolvedItems.add(new UnresolvedItem(item, mn.getDbId(), 
						mn.getEntity().getProperties(MassnahmenUmsetzung.P_UMSETZUNGDURCH_LINK),
						mn.getEntity().getProperties(MassnahmenUmsetzung.P_NAECHSTEREVISIONDURCH_LINK)));
				
			}
		}

		// find persons linked directly:
		FindLinkedPersons findCommand = new FindLinkedPersons(unresolvedItems);
		findCommand = this.getCommandService().executeCommand(findCommand);
		all.addAll(findCommand.getResolvedItems());
		unresolvedItems = findCommand.getUnresolvedItems();
		
		
		
		// find persons according to roles and relation:
		FindResponsiblePersons command = new FindResponsiblePersons(unresolvedItems, 
				MassnahmenUmsetzung.P_VERANTWORTLICHE_ROLLEN_UMSETZUNG);
		command = this.getCommandService().executeCommand(command);
		unresolvedItems = command.getResolvedItems();
		for (UnresolvedItem resolvedItem : unresolvedItems) {
			all.add(resolvedItem.getItem());
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
//		IBaseDao<MassnahmenUmsetzung, Serializable> dao = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
//		dao.initialize(mn);
	}

	public List<TodoViewItem> getAll() {
		return all;
	}

	public Set<String> getExecutionSet() {
		return executionSet;
	}

	public void setExecutionSet(Set<String> umsetzungSet) {
		this.executionSet = umsetzungSet;
	}

	public Set<String> getSealSet() {
		return sealSet;
	}

	public void setSealSet(Set<String> sealSet) {
		this.sealSet = sealSet;
	}

}
