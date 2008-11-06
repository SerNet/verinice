package sernet.gs.ui.rcp.main.connect;

import java.io.Serializable;
import java.util.List;

import org.hibernate.collection.PersistentCollection;

public interface IBaseDao<T, ID extends Serializable> {

		 public T saveOrUpdate(T entity);

		 public void delete(T entity);

		 public T findById(ID id);

		 public List<T> findAll();
		 
		 public List findByQuery(String hqlQuery, Object[] params);

		 public void initialize(Object collection);
		 
		 public void persist(T entity);
		 
		 public void flush();
		   
}
