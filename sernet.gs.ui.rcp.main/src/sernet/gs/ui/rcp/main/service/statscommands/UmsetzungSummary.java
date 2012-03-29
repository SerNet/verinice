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
 *     Robert Schuster <r.schuster@tarent.de> - use custom SQL query
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.statscommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

@SuppressWarnings("serial")
public class UmsetzungSummary extends MassnahmenSummary {

	private static final HibernateCallback hcb = new UmsetzungenSummaryCallback();

	public void execute() {
		super.execute();
		setSummary(getUmsetzungenSummary());
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Integer> getUmsetzungenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
		
		// List of Object[] array:
		// index 0: property value
		// index 1: count of occurence of the above value in the db
		List<Object[]> list = (List<Object[]>) dao.findByCallback(hcb);

		// Puts SQL result in a object structure the user of this class expects.  
		for (Object[] element : list)
		{
			result.put(
					(String) element[0],
					(Integer) element[1]);
		}
		
		return result;
	}
	

	private static class UmsetzungenSummaryCallback implements HibernateCallback, Serializable
	{

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {

			/**
			 * Queries all properties whose name is MassnahmenUmsetzung.P_UMSETZUNG
			 * and groups the results by the value and determines the amount of
			 * entries per group.
			 * 
			 * Et voila, this is our statistic.
			 */
			Query query = session.createSQLQuery(
					"select propertyvalue, count(propertyvalue) as amount "
					+ "from properties "
					+ "where propertytype = :type "
					+ "group by propertyvalue ")
					.addScalar("propertyvalue", Hibernate.STRING)
					.addScalar("amount", Hibernate.INTEGER)
					.setString("type", MassnahmenUmsetzung.P_UMSETZUNG);
					
			return query.list();
		}
		
	}
}
