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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.views.AuditView;
import sernet.gs.ui.rcp.main.bsi.views.TodoView;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

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
@SuppressWarnings({ "serial", "unchecked" })
public class FindMassnahmenForITVerbund extends FindMassnahmenAbstract {
	
	private static final Logger log = Logger.getLogger(FindMassnahmenForITVerbund.class);

	private Integer itverbundDbId = null;
	
	
	public FindMassnahmenForITVerbund(Integer dbId) {
		super();
		Logger.getLogger(this.getClass()).debug("Looking up Massnahme for IT-Verbund " + dbId);
		this.itverbundDbId = dbId;
	}
	
	public void execute() {
		try {
			long start = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("FindMassnahmenForITVerbund, itverbundDbId: " + itverbundDbId);
			}
			List<MassnahmenUmsetzung> list = new ArrayList<MassnahmenUmsetzung>();
			IBaseDao<MassnahmenUmsetzung, Serializable> dao = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
			list = dao.findByCallback(new FindMassnahmenForITVerbundCallback(itverbundDbId));
			
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
	
	private class FindMassnahmenForITVerbundCallback implements HibernateCallback, Serializable {
		
		private Integer itverbundID;

		FindMassnahmenForITVerbundCallback(Integer itverbundID) {
			this.itverbundID = itverbundID;
		}

		public Object doInHibernate(Session session) throws HibernateException, SQLException {	
			Query query = session.createQuery(
					"from MassnahmenUmsetzung mn " +
					"join fetch mn.entity " +
					"left join fetch mn.parent.parent.entity " +
					"left join fetch mn.parent.parent.parent.parent.entity " +
					"where mn.parent.parent.parent.parent = :id " +
					"or mn.parent.parent = :id2")
					.setInteger("id", itverbundID)
					.setInteger("id2", itverbundID);
			query.setReadOnly(true);
			List<MassnahmenUmsetzung> result = query.list();
			
			return result;
		}

	}

}
