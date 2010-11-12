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
 *     Robert Schuster <r.schuster> - use custom SQL query
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.statscommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

@SuppressWarnings("serial")
public class CompletedStepsSummary extends MassnahmenSummary {
	
	private static Logger log = Logger.getLogger(CompletedStepsSummary.class);

	private static HibernateCallback hcb = new Callback();

	public void execute() {
		setSummary(getCompletedStufenSummary());
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Integer> getCompletedStufenSummary() {
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
	
	private static class Callback implements HibernateCallback, Serializable
	{

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			
			// Returns all the seal level and the amount of entries for a specific
			// seal level for all MassnahmenUmsetzung instances which have been
			// fullfilled (see {@link MassnahmenUmsetzung#isCompleted).			
			Query query = session.createSQLQuery(
					"select p1.propertyvalue as pv, count(p1.propertyvalue) as amount "
					+ "from properties p1 inner join propertylist pl1 on p1.properties_id=pl1.dbid, "
					+ "properties p2 inner join propertylist pl2 on p2.properties_id=pl2.dbid "
					+ "where pl1.typedlist_id = pl2.typedlist_id "
					+ "and p1.propertytype = :p1type "
					+ "and p2.propertytype = :p2type "
					+ "and p2.propertyvalue in (:p2values) "
					+ "group by p1.propertyvalue ")
					.addScalar("pv", Hibernate.STRING)
					.addScalar("amount", Hibernate.INTEGER)
					.setString("p1type", MassnahmenUmsetzung.P_SIEGEL)
					.setString("p2type", MassnahmenUmsetzung.P_UMSETZUNG)
					.setParameterList("p2values", new Object[] { MassnahmenUmsetzung.P_UMSETZUNG_JA, MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH }, Hibernate.STRING);
			
			if (log.isDebugEnabled())
				log.debug("generated query:" + query.getQueryString());
					
			return query.list();
		}
		
	}
	
}
