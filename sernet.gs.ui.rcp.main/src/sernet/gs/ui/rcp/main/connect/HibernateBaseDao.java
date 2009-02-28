package sernet.gs.ui.rcp.main.connect;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import sernet.gs.ui.rcp.main.common.model.CascadingTransaction;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.LoopException;

public class HibernateBaseDao<T, ID extends Serializable> extends HibernateDaoSupport 
	implements IBaseDao<T, ID> {
		 private Class<T> type;

		 public HibernateBaseDao(Class<T> type){
		     this.type = type;
		 }

		 public void saveOrUpdate(T entity) {
		     getHibernateTemplate().saveOrUpdate(entity);
		     fireChange(entity);
		 }

		 public void delete(T entity) {
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
			if (fireChange)
				fireChange(mergedElement);
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

		private void fireChange(T element) {
			if (element instanceof CnATreeElement) {
				CnATreeElement elmt = (CnATreeElement) element;
				CascadingTransaction ta;
				Object loopedObject = null;
				
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
				
				if (loopedObject != null) {
					throw new LoopException(loopedObject);
				}
			}
		}

		public Class<T> getType() {
			return this.type;
		}
}
