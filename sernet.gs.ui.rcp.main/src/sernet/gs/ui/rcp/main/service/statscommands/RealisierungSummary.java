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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.statscommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.ui.rcp.main.bsi.views.chart.DateValues;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

@SuppressWarnings("serial")
public class RealisierungSummary extends GenericCommand {
	
	private static Logger log = Logger.getLogger(RealisierungSummary.class);

	private DateValues total1;

	private DateValues total2;
	
	private static HibernateCallback hcb = new Callback();

	@SuppressWarnings("unchecked")
	public void execute() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);

		// List of Object[] array:
		// index 0: property value (date)
		// index 1: isCompleted flag
		List<Object[]> list = (List<Object[]>) dao.findByCallback(hcb);
		
		total1 = new DateValues();
		total2 = new DateValues();
		
		for (Object[] r : list) {
			String rawDate = (String) r[0];
			boolean isCompleted = (MassnahmenUmsetzung.P_UMSETZUNG_JA.equals((String)r[1]) 
					|| MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH.equals((String)r[1]));
			
			//fixme umgesetzte sollten datum der umsetzung gesetzt haben! fix in bulk edit
			Date date = (rawDate == null || rawDate.length() == 0)
				? Calendar.getInstance().getTime() : new Date(Long.parseLong(rawDate)); 

			if (isCompleted) {
				total1.add(date);
				total2.add(date);
			}
			else  {
				total2.add(date);
			}
		}

	}

	public DateValues getTotal1()
	{
		return total1;
	}
	
	public DateValues getTotal2()
	{
		return total2;
	}
	
	private static class Callback implements HibernateCallback, Serializable
	{

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			
			Query query = session.createQuery(
					"select p1.propertyValue, p2.propertyValue "
					+ "from PropertyList plist1 inner join plist1.properties as p1, "
					+ "PropertyList plist2 inner join plist2.properties as p2 "
					+ "where plist1.entityId = plist2.entityId "
					+ "and p1.propertyType = :p1type "
					+ "and p2.propertyType = :p2type ")
					.setString("p1type", MassnahmenUmsetzung.P_UMSETZUNGBIS)
					.setString("p2type", MassnahmenUmsetzung.P_UMSETZUNG);
			
			if (log.isDebugEnabled())
				log.debug("generated query: " + query.getQueryString());
					
			return query.list();
		}
		
	}

}
