package sernet.gs.ui.rcp.main.connect;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;


public interface IBaseDao<T, ID extends Serializable> {

		 public void saveOrUpdate(T entity);
		 
		 public T merge(T entity);

		 public T merge(T entity, boolean fireUpdates);

		 public void delete(T entity);

		 public T findById(ID id);

		 public List<T> findAll();
		 
		 public List findByQuery(String hqlQuery, Object[] params);
		 
		 public int updateByQuery(String hqlQuery, Object[] values);
		 
		 public void refresh(T element);
		 
		 public void reload(T element, Serializable id);

		 public void initialize(Object collection);
		 
		 public void flush();

		 public Class<T> getType();
		 
}
