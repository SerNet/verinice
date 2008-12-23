package sernet.gs.ui.rcp.main.service;

import java.io.Serializable;
import java.util.HashMap;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;

public class DAOFactory {
	
	// injected by spring
	private HashMap<Class, IBaseDao> daos = new HashMap<Class, IBaseDao>(); 
	
	public void setEntityDao(IBaseDao<Entity, Integer> entityDao) {
		daos.put(Entity.class, entityDao);
	}
	
	public void setPropertyDao(IBaseDao<Property, Integer> propertyDao) {
		daos.put(Property.class, propertyDao);
	}

	public void setCnATreeElementDao(IBaseDao<CnATreeElement, Integer> cnaDao) {
		daos.put(CnATreeElement.class, cnaDao);
	}
	
	public <T> IBaseDao<T, Serializable> getDAO(Class<T> daotype) {
		return  daos.get(daotype);
	}
}
