package sernet.gs.ui.rcp.main.service;

import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

public class HibernateHuiService implements IHuiService {
	
	private IBaseDao<Property, Integer> propertyDao;
	
	private static final String QUERY_BY_PROP_ID ="select list, index(list) " +
			"from Entity entity " +
			"join entity.typedPropertyLists list " +
			"where index(list) = ?"; 
	
	
		
	public List findAllPropertiesForTypeId(String propertyTypeId) {
		List<Object[]> findByQuery = propertyDao.findByQuery(QUERY_BY_PROP_ID, new String[] {propertyTypeId});
		List result = new ArrayList();
		for (Object[] array : findByQuery) {
			PropertyList list = (PropertyList)array[0];
			result.addAll(list.getProperties());
		}
		return result;
	}



	public void setPropertyDao(IBaseDao<Property, Integer> propertyDao) {
		this.propertyDao = propertyDao;
	}



}
