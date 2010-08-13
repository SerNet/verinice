/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
@SuppressWarnings("serial")
public class LoadChildrenAndMassnahmen extends GenericCommand implements ILoadChildren {

	private static final Logger log = Logger.getLogger(LoadChildrenAndMassnahmen.class);

	private final Comparator<CnATreeElement> cnAComparator = new CnAComparator();
	
	private Set<TodoViewItem> massnahmen = new HashSet<TodoViewItem>(20);
	
	private List<CnATreeElement> gebaeudeList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> raumList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> clienteList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> serverList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> netzList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> anwendungList = new ArrayList<CnATreeElement>(10);
	private List<CnATreeElement> personList = new ArrayList<CnATreeElement>(10);
	
	private Set<UnresolvedItem> unresolvedItems = new HashSet<UnresolvedItem>();
	
	private Integer id = null;

	private Set<String> executionSet;

	private Set<String> sealSet;

	@SuppressWarnings("serial")
	private class LoadChildrenAndMassnahmenCallback implements HibernateCallback, Serializable {

		private Integer id;

		LoadChildrenAndMassnahmenCallback(Integer id) {
			this.id = id;
		}

		public Object doInHibernate(Session session) throws HibernateException, SQLException {

			Query query = session.createQuery(
					"from CnATreeElement el " + 
					"join fetch el.children.children.entity " + 
					"where el.dbId = :id ");
			query.setReadOnly(true);
			return query.list();
		}

	}
	
	public LoadChildrenAndMassnahmen() {
	}

	public LoadChildrenAndMassnahmen(Integer dbId) {
		this.id = dbId;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.taskcommands.ILoadChildren#execute()
	 */
	public void execute() {
		try {
			long start = System.currentTimeMillis();
			List<CnATreeElement> list = new ArrayList<CnATreeElement>();
			IBaseDao<ITVerbund, Serializable> dao = getDaoFactory().getDAO(ITVerbund.class);
			DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class);
			crit.add(Restrictions.eq("dbId", id));
			crit.setFetchMode("children",FetchMode.JOIN);
			crit.setFetchMode("children.children",FetchMode.JOIN);
			crit.setFetchMode("children.children.entity",FetchMode.JOIN);
			crit.setFetchMode("children.children.entity.typedPropertyLists",FetchMode.JOIN);
			crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			list = dao.findByCriteria(crit);
			if (list != null && list.size() > 0) {
				// create display items:
				fillLists(list.get(0));
			}
			if (log.isDebugEnabled()) {
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
	private void fillLists(CnATreeElement el) throws CommandException {
		for (CnATreeElement cl : el.getChildren()) {
			for (CnATreeElement clcl : cl.getChildren()) {
				if (clcl instanceof MassnahmenUmsetzung) {
					TodoViewItem item = createToDoItem((MassnahmenUmsetzung) clcl);
					if(item!=null) {
						massnahmen.add(item);
					}
				} else {
					hydrate(clcl);
					String entityTypeId = clcl.getTypeId();
					if(Anwendung.TYPE_ID.equals(entityTypeId)) {
						anwendungList.add(clcl);
					} else if(Client.TYPE_ID.equals(entityTypeId)) {
						clienteList.add(clcl);
					} else if(Gebaeude.TYPE_ID.equals(entityTypeId)) {
						gebaeudeList.add(clcl);
					} else if(Person.TYPE_ID.equals(entityTypeId)) {
						personList.add(clcl);
					} else if(NetzKomponente.TYPE_ID.equals(entityTypeId)) {
						netzList.add(clcl);
					} else if(Raum.TYPE_ID.equals(entityTypeId)) {
						raumList.add(clcl);
					} else if(Server.TYPE_ID.equals(entityTypeId)) {
						serverList.add(clcl);
					} else  {
						log.error("Unknown object type: " + entityTypeId);
					}
				}
			}
		}
		
		Collections.sort(anwendungList, cnAComparator);
		Collections.sort(clienteList, cnAComparator);
		Collections.sort(gebaeudeList, cnAComparator);
		Collections.sort(personList, cnAComparator);
		Collections.sort(netzList, cnAComparator);
		Collections.sort(raumList, cnAComparator);
		Collections.sort(serverList, cnAComparator);
		
		// find persons according to roles and relation:
		FindResponsiblePersons command = new FindResponsiblePersons(unresolvedItems, MassnahmenUmsetzung.P_VERANTWORTLICHE_ROLLEN_UMSETZUNG);
		command = this.getCommandService().executeCommand(command);
		unresolvedItems = command.getResolvedItems();
		for (UnresolvedItem resolvedItem : unresolvedItems) {
			massnahmen.add(resolvedItem.getItem());
		}
		
	}

	private TodoViewItem createToDoItem(MassnahmenUmsetzung el) {
		String umsetzung = el.getUmsetzung();
		String siegelStufe = String.valueOf(el.getStufe());
		TodoViewItem item = null;
		if ((getExecutionSet() == null || getExecutionSet().contains(umsetzung)) 
				&& (getSealSet() == null || getSealSet().contains(siegelStufe))) {

			item = new TodoViewItem();

			if (el.getParent() instanceof GefaehrdungsUmsetzung)
				item.setParentTitle(el.getParent().getParent().getParent().getTitle());
			else
				item.setParentTitle(el.getParent().getParent().getTitle());

			item.setTitel(el.getTitle());
			item.setUmsetzung(umsetzung);
			item.setUmsetzungBis(el.getUmsetzungBis());
			item.setNaechsteRevision(el.getNaechsteRevision());
			item.setRevisionDurch(el.getRevisionDurch());

			item.setStufe(siegelStufe.charAt(0));
			item.setUrl(el.getUrl());
			item.setStand(el.getStand());
			item.setDbId(el.getDbId());

			if (el.getUmsetzungDurch() != null && el.getUmsetzungDurch().length() > 0) {
				item.setUmsetzungDurch(el.getUmsetzungDurch());
				massnahmen.add(item);
			} else {
				unresolvedItems.add(new UnresolvedItem(item, el));
			}
		}
		return item;
	}
	
	public void hydrate(CnATreeElement el) {
		el.getTitle();
	}

	public Set<TodoViewItem> getMassnahmen() {
		return massnahmen;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.taskcommands.ILoadChildren#getGebaeudeList()
	 */
	public List<CnATreeElement> getGebaeudeList() {
		return gebaeudeList;
	}

	public void setGebaeudeList(List<CnATreeElement> gebaeudeList) {
		this.gebaeudeList = gebaeudeList;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.taskcommands.ILoadChildren#getRaumList()
	 */
	public List<CnATreeElement> getRaumList() {
		return raumList;
	}

	public void setRaumList(List<CnATreeElement> raumList) {
		this.raumList = raumList;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.taskcommands.ILoadChildren#getClienteList()
	 */
	public List<CnATreeElement> getClienteList() {
		return clienteList;
	}

	public void setClienteList(List<CnATreeElement> clienteList) {
		this.clienteList = clienteList;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.taskcommands.ILoadChildren#getServerList()
	 */
	public List<CnATreeElement> getServerList() {
		return serverList;
	}

	public void setServerList(List<CnATreeElement> serverList) {
		this.serverList = serverList;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.taskcommands.ILoadChildren#getNetzList()
	 */
	public List<CnATreeElement> getNetzList() {
		return netzList;
	}

	public void setNetzList(List<CnATreeElement> netzList) {
		this.netzList = netzList;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.taskcommands.ILoadChildren#getAnwendungList()
	 */
	public List<CnATreeElement> getAnwendungList() {
		return anwendungList;
	}

	public void setAnwendungList(List<CnATreeElement> anwendungList) {
		this.anwendungList = anwendungList;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.taskcommands.ILoadChildren#getPersonList()
	 */
	public List<CnATreeElement> getPersonList() {
		return personList;
	}

	public void setPersonList(List<CnATreeElement> personList) {
		this.personList = personList;
	}

	public void setMassnahmen(Set<TodoViewItem> massnahmen) {
		this.massnahmen = massnahmen;
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
	
	public class CnAComparator implements Comparator<CnATreeElement> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(CnATreeElement o1, CnATreeElement o2) {
			int result = -1;
			if(o1!=null && o1.getTitle()!=null) {
				if(o2==null || o2.getTitle()==null) {
					result = 1;
				} else {
					result = o1.getTitle().compareTo(o2.getTitle());
				}
			} else if(o2==null || o2.getTitle()==null) {
				result = 0;
			}
			return result;
		}
		
	}

}
