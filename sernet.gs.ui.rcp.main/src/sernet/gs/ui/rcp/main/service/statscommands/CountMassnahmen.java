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
 *     Robert Schuster <r.schuster@tarent.de> - use custom HQL query
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.statscommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.BSIModel;

@SuppressWarnings("serial")
public class CountMassnahmen extends GenericCommand {
	
	private static final Logger LOG = Logger.getLogger(CountMassnahmen.class);

	private int totalCount;
	
	private static final HibernateCallback HCB = new Callback();

	@SuppressWarnings("unchecked")
	public void execute() {
		
		List<Long> result = (List<Long>) getDaoFactory().getDAO(BSIModel.class).findByCallback(HCB);
		
		totalCount = result.get(0).intValue();
	}

	public int getTotalCount() {
		return totalCount;
	}

	private static class Callback implements HibernateCallback, Serializable
	{

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			
			Query query = session.createQuery(
					"select count(m)"
					+ "from MassnahmenUmsetzung m ");
			
			if (LOG.isDebugEnabled()){
				LOG.debug("hql query: " + query.getQueryString());
			}
			return query.list();
		}
		
	}
}
