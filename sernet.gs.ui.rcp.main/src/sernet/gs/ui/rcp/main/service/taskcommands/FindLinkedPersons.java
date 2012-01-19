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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Person;

/**
 * Search responsible persons and auditors for measures fast by loading all referenced person's entities first
 * and then manually setting the name in the DTO. 
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class FindLinkedPersons extends GenericCommand {

	private Set<UnresolvedItem> unresolvedItems;
	private Set<TodoViewItem> resolvedItems;

	/**
	 * @param unresolvedItems
	 * @param fieldToSearch
	 */
	public FindLinkedPersons(Set<UnresolvedItem> unresolvedItems) {
		this.unresolvedItems = unresolvedItems;
		resolvedItems = new HashSet<TodoViewItem>();
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	@SuppressWarnings("unchecked")
	public void execute() {
		// preload Entities:
		final Set<Integer> dbIds = new HashSet<Integer>();
		for (UnresolvedItem item : unresolvedItems) {
			dbIds.addAll(getEntityIDs(item.getUmsetzungDurchLinks()));
			dbIds.addAll(getEntityIDs(item.getRevisionDurchLinks()));
		}
		
		if (dbIds.size()==0)
			return;
		
		IBaseDao<Person, Serializable> dao = getDaoFactory().getDAO(Person.class);
		List<Entity> personenEntities = dao.findByCallback(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery(
						"from Entity e " +
						"where e.dbId in (:dbids)")
						.setParameterList("dbids", dbIds);
				String queryString = query.getQueryString();
				query.setReadOnly(true);
				List result = query.list();
				return result;
			}
		});
		Map<Integer, Entity> personenMap = new HashMap<Integer, Entity>();
		for (Entity entity : personenEntities) {
			personenMap.put(entity.getDbId(), entity);
		}
		
		// set auditors:
		for (UnresolvedItem unresolvedItem : unresolvedItems) {
			Set<Integer> entityIDs = getEntityIDs(unresolvedItem.getRevisionDurchLinks());
			String names = getNames(personenMap, entityIDs);
			if (names.length()>0) {
				unresolvedItem.getItem().setRevisionDurch(names);
			}
		}

		// set responsible persons:
		for (Iterator<UnresolvedItem> iterator = unresolvedItems.iterator(); iterator
				.hasNext();) {
			UnresolvedItem unresolvedItem = iterator.next();
			Set<Integer> entityIDs = getEntityIDs(unresolvedItem.getUmsetzungDurchLinks());
			String names = getNames(personenMap, entityIDs);
			if (names.length()>0) {
				unresolvedItem.getItem().setUmsetzungDurch(names);
				resolvedItems.add(unresolvedItem.getItem());
				// do not try to find person by role later:
				iterator.remove();
			}
			
		}
	}

	/**
	 * @param personenMap
	 * @param entityIDs
	 * @return
	 */
	private String getNames(Map<Integer, Entity> personenMap,
			Set<Integer> entityIDs) {
		StringBuffer result = new StringBuffer();
		for (Integer dbId : entityIDs) {
			Entity entity = personenMap.get(dbId);
			if (entity != null) {
				if (result.length() > 0)
					result.append(", " + Person.getTitel(entity));
				else
					result.append(Person.getTitel(entity));
			}
		}
		return result.toString();
	}

	/**
	 * @param umsetzungDurchLinks
	 * @return
	 */
	private Set<Integer> getEntityIDs(PropertyList links) {
		if (links == null || links.getProperties() == null || links.getProperties().size() == 0)
			return new HashSet<Integer>(0);
		
		HashSet<Integer> result = new HashSet<Integer>();
		List<Property> properties = links.getProperties();
		for (Property property : properties) {
			result.add(Integer.parseInt(property.getPropertyValue()));
		}
		return result;
	}

	/**
	 * @return
	 */
	public Collection<? extends TodoViewItem> getResolvedItems() {
		return resolvedItems;
	}

	/**
	 * @return
	 */
	public Set<UnresolvedItem> getUnresolvedItems() {
		return unresolvedItems;
	}

}
