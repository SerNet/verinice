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
package sernet.gs.server.commands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.ui.rcp.main.service.crudcommands.LoadPolymorphicCnAElementById;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Retrieves all {@link CnATreeElement} instances which have been changed since
 * the given key date.
 * 
 * <p>Each instance appears only once in the result even if multiple changes
 * happened.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
@SuppressWarnings("serial")
class GetChangedElementsSince extends GenericCommand {

	private List<CnATreeElement> changedElements;
	
	private String[] classNames;
	private Date keydate;
	private int type;

	public GetChangedElementsSince(Calendar keydate, int type, Class<?> klass) {
		this(keydate, type, new String[] { klass.getName() });
	}
	
	public GetChangedElementsSince(Calendar keydate, int type, String[] classNames) {
		this.keydate = keydate.getTime();
		this.classNames = classNames;
		this.type = type;
	}
	
	public List<CnATreeElement> getChangedElements()
	{
		return changedElements;
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		
		IBaseDao<ChangeLogEntry, Serializable> dao = getDaoFactory().getDAO(
				ChangeLogEntry.class);
		
		List<ChangeLogEntry> entries = (List<ChangeLogEntry>) 
			dao.findByCallback(new Callback(keydate, type, classNames));
		
		try
		{
			hydrateChangedItems(entries);
		}
		catch (CommandException e)
		{
			throw new RuntimeException("Error retrieving changed elements.", e);
		}
	}

	private void hydrateChangedItems(List<ChangeLogEntry> entries)
			throws CommandException {

		Set<Integer> ids = new HashSet<Integer>(entries.size());

		for (ChangeLogEntry logEntry : entries) {
			if (logEntry.getElementId() != null)
				ids.add(logEntry.getElementId());
		}

		if (ids.isEmpty())
			changedElements = Collections.emptyList();
		else
		{
			Integer[] idArray = (Integer[]) ids.toArray(new Integer[ids.size()]);
			LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(idArray);
			command = getCommandService().executeCommand(command);
	
			changedElements = command.getElements();
		}
	}

	private static class Callback implements HibernateCallback, Serializable {
		
		Date keydate;
		
		String[] classNames;
		
		int type;
		
		Callback(Date keydate, int type, String[] classNames)
		{
			this.keydate = keydate;
			this.type = type;
			this.classNames = classNames;
		}

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			
			Query query = session.createQuery(
					"from ChangeLogEntry entry "
					+ "where entry.changetime > :keydate "
					+ "and entry.elementId is not null "
					+ "and entry.change= :type "
					+ "and entry.elementClass in (:classNames)")
					.setDate("keydate", keydate)
					.setInteger("type", type)
					.setParameterList("classNames", classNames);
			
			return query.list();
		}

	}
}
