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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

public class FastLoadCnAElementsByIds extends GenericCommand {

	private List<Integer> dbIDs;
	private ArrayList<Entity> foundItems;


	public FastLoadCnAElementsByIds(List<Integer> dbIDs) {
		this.dbIDs = dbIDs;
	}

	public void execute() {
		IBaseDao<Entity, Serializable> dao = getDaoFactory().getDAO(Entity.class);
		foundItems = new ArrayList<Entity>();
		
		List list = dao.findByCallback(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery(
						"from Entity e " +
						"where e.dbId in (:dbids)")
						.setParameterList("dbids", dbIDs);
				String queryString = query.getQueryString();
				
				query.setReadOnly(true);
				List result = query.list();
				return result;
			}
		});
		
		for (Object object : list) {
			Entity elmt = (Entity) object;
			foundItems.add(elmt);
		}
	}

	public ArrayList<Entity> getFoundItems() {
		return foundItems;
	}

	
	

}
