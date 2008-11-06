package sernet.gs.ui.rcp.main.connect;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateBaseDao<T, ID extends Serializable> extends HibernateDaoSupport 
	implements IBaseDao<T, ID> {
		 private Class<T> type;

		 public HibernateBaseDao(Class<T> type){
		     this.type = type;
		 }

		 public T saveOrUpdate(T entity) {
		     getHibernateTemplate().saveOrUpdate(entity);
		     return entity;
		 }

		 public void delete(T entity) {
		     getHibernateTemplate().delete(entity);
		 }

		 public List findAll() {
		     return getHibernateTemplate().loadAll(type);
		 }

		 public T findById(ID id) {
		     return (T)getHibernateTemplate().load(type.getClass(), id);
		 }

		public List findByQuery(String hqlQuery, Object[] values) {
			return getHibernateTemplate().find(hqlQuery, values);
		}

		@Override
		public void initialize(Object collection) {
			getHibernateTemplate().initialize(collection);
		}

		@Override
		public void persist(T entity) {
			getHibernateTemplate().persist(entity);
		}

		@Override
		public void flush() {
			getHibernateTemplate().flush();
		}

}
