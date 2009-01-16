package sernet.gs.ui.rcp.main.connect;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class HibernateBaseDao<T, ID extends Serializable> extends HibernateDaoSupport 
	implements IBaseDao<T, ID> {
		 private Class<T> type;

		 public HibernateBaseDao(Class<T> type){
		     this.type = type;
		 }

		 public void saveOrUpdate(T entity) {
		     getHibernateTemplate().saveOrUpdate(entity);
		     
		 }

		 public void delete(T entity) {
		     getHibernateTemplate().delete(entity);
		 }

		 public List findAll() {
		     return getHibernateTemplate().loadAll(type);
		 }

		 public T findById(ID id) {
			 return (T) getHibernateTemplate().load(type, id);
		 }

		public List findByQuery(String hqlQuery, Object[] values) {
			return getHibernateTemplate().find(hqlQuery, values);
		}

		public void initialize(Object proxy) {
			getHibernateTemplate().initialize(proxy);
		}

		public void flush() {
			getHibernateTemplate().flush();
		}

		public T merge(T entity) {
			return (T) getHibernateTemplate().merge(entity);
		}
		
		public void refresh(T element) {
			getHibernateTemplate().refresh(element);
		}
		
		public void reload(T element, Serializable id) {
			getHibernateTemplate().load(element, id);
		}
		

}
