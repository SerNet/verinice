package sernet.gs.ui.rcp.main.service;

import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;

public class HibernateHuiService implements IHuiService {
	
	private IBaseDao<Property, Integer> propertyDao;
	private IBaseDao<Entity, Integer>   entityDao;
	private IBaseDao<Person, Integer>   personDao;
	
	private static final String QUERY_BY_PROP_ID ="select list, index(list) " +
			"from Entity entity " +
			"join entity.typedPropertyLists list " +
			"where index(list) = ? ";

	public List findAllPropertiesForTypeId(String propertyTypeId) {
		List<Object[]> findByQuery = propertyDao.findByQuery(QUERY_BY_PROP_ID, new String[] {propertyTypeId});
		List result = new ArrayList();
		for (Object[] array : findByQuery) {
			PropertyList list = (PropertyList)array[0];
			propertyDao.initialize(list.getProperties());
			for (Property prop: list.getProperties()) {
				propertyDao.initialize(prop.getParent());
			}
			result.addAll(list.getProperties());
		}
		return result;
	}



	public void setPropertyDao(IBaseDao<Property, Integer> propertyDao) {
		this.propertyDao = propertyDao;
	}


	private Person findPerson(String personName) {
		List<Person> all = personDao.findAll();
		for (Person person : all) {
			if (person.getTitel().equals(personName))
				return person;
		}
		return null;
	}

	public void migratePersonsForTypeId(String oldPropTypeID, String newPropTypeID) {
		List<Property> properties = findAllPropertiesForTypeId(oldPropTypeID);
		for (Property property : properties) {
			// find
			Person personToLink = findPerson(property.getPropertyValue());
			if (personToLink != null) {
				// save new DB reference to found person:
				Entity entity = property.getParent();
				PropertyType newType = HUITypeFactory.getInstance().getEntityType(entity.getEntityType())
					.getPropertyType(newPropTypeID);
				entity.createNewProperty(newType, personToLink.getEntity().getDbId().toString());
				//entityDao.persist(entity);
			}
		}
	}



	public void setEntityDao(IBaseDao<Entity, Integer> entityDao) {
		this.entityDao = entityDao;
	}



	public void setPersonDao(IBaseDao<Person, Integer> personDao) {
		this.personDao = personDao;
	}



}
