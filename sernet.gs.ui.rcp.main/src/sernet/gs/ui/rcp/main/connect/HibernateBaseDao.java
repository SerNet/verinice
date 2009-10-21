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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.connect;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import sernet.gs.ui.rcp.gsimport.AttachDbFileTask;
import sernet.gs.ui.rcp.main.common.model.CascadingTransaction;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.LoopException;

public class HibernateBaseDao<T, ID extends Serializable> extends HibernateDaoSupport 
	implements IBaseDao<T, ID> {
		 private Class<T> type;

		 public HibernateBaseDao(Class<T> type){
		     this.type = type;
		 }
		 
		 // FIXME akoderman implement security check for write permission on server here
		 
		 /*
		  * It has to be noted that updates can happen without any call being made to any of these methods.
		  * When an object has been loaded into the session, any changes made by a command are persisted
		  * by hibernate into the database directly.
		  * 
		  * Therefore a hibernate interceptor should be used for to check authorization during object's lifecycle events.
		  */
		 
		 /*
		  * For more complex methods, i.e. all that execute a SQL query, the caller (command, webservice or similar,  but always on the server side!) 
		  * may be responsible for checking security. To ensure this contract, a security callback object must be passed to every such method, the caller must implement the method hasWritePersmission():
		  * 
				 interface SecurityCallback {
					 public void hasWritePermission() throws AuthorizationException;
				 }
		  * 
		  */
		 
		 public void saveOrUpdate(T entity) {
		     getHibernateTemplate().saveOrUpdate(entity);
		     if (entity instanceof CnATreeElement) {
		    	 CnATreeElement elmt = (CnATreeElement) entity;
		    	 fireChange(elmt);
		     }
		 }
		 public void delete(T entity) {
			 // TODO akoderman update protection requirements on delete (see how it's done during merge())
			 Logger.getLogger(this.getClass()).debug("Deleting element " + entity);
		     getHibernateTemplate().delete(entity);
		 }

		 public List findAll() {
//			 DetachedCriteria criteria = DetachedCriteria.forClass(type);
//			 List results = getHibernateTemplate().findByCriteria(criteria, 0, 1000);
//			 return results;
			 return getHibernateTemplate().loadAll(type);
		 }

		 public T findById(ID id) {
			 return (T) getHibernateTemplate().load(type, id);
		 }

		public List findByQuery(String hqlQuery, Object[] values) {
			return getHibernateTemplate().find(hqlQuery, values);
		}
		
		public List findByCallback(HibernateCallback hcb) {
			return getHibernateTemplate().executeFind(hcb);
		}

		public Object executeCallback(HibernateCallback hcb) {
			return getHibernateTemplate().execute(hcb);
		}

		public int updateByQuery(String hqlQuery, Object[] values) {
			return getHibernateTemplate().bulkUpdate(hqlQuery, values);
		}

		public void initialize(Object proxy) {
			getHibernateTemplate().initialize(proxy);
		}

		public void flush() {
			getHibernateTemplate().flush();
		}

		public T merge(T entity, boolean fireChange) {
			T mergedElement = (T) getHibernateTemplate().merge(entity);
			
			if (fireChange
					&& mergedElement instanceof CnATreeElement) {
				CnATreeElement elmt = (CnATreeElement) mergedElement;
				fireChange(elmt);
			}
			
			if (fireChange && mergedElement instanceof CnALink) {
				CnALink link = (CnALink) mergedElement;
				fireChange(link.getDependency());
			}
			
			
			return mergedElement;
		}
		
		public T merge(T entity) {
			return merge(entity, false);
		}
		
		public void refresh(T element) {
			getHibernateTemplate().refresh(element);
		}
		
		public void reload(T element, Serializable id) {
			getHibernateTemplate().load(element, id);
		}

		/**
		 * Causes changes in protection level (schutzbedarf) to be propagated.
		 * 
		 * @param elmt the element that had its protection level or protection level description changed.
		 */
		private void fireChange(CnATreeElement elmt) {
				Object loopedObject = null;
				CascadingTransaction ta;
				
				ta = new CascadingTransaction();
				elmt.fireIntegritaetChanged(ta);
				if (ta.hasLooped())
					loopedObject = ta.getLoopedObject();
				
				ta = new CascadingTransaction();
				elmt.fireVerfuegbarkeitChanged(ta);
				if (ta.hasLooped())
					loopedObject = ta.getLoopedObject();
				
				ta = new CascadingTransaction();
				elmt.fireVertraulichkeitChanged(ta);
				if (ta.hasLooped())
					loopedObject = ta.getLoopedObject();
				
		}

		public Class<T> getType() {
			return this.type;
		}
}
