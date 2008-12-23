package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.DAOFactory;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;

public class MigratePersonContacts extends GenericCommand {

	private IBaseDao<Person, Serializable> personDao;
	private IBaseDao<Property, Serializable> propertyDao;
	private String oldPropTypeID;
	private String newPropTypeID;

	private static final String QUERY_BY_PROP_ID = "select list, index(list) "
			+ "from Entity entity " + "join entity.typedPropertyLists list "
			+ "where index(list) = ? ";

	public MigratePersonContacts(String oldPropTypeID, String newPropTypeID) {
		this.oldPropTypeID = oldPropTypeID;
		this.newPropTypeID = newPropTypeID;
	}

	public void execute() {
		personDao = getDaoFactory().getDAO(Person.class);
		propertyDao = getDaoFactory().getDAO(Property.class);

		migratePersonsForTypeId(oldPropTypeID, newPropTypeID);
	}

	public void migratePersonsForTypeId(String oldPropTypeID,
			String newPropTypeID) {
		List<Property> properties = findAllPropertiesForTypeId(oldPropTypeID);
		for (Property property : properties) {
			// find
			Person personToLink = findPerson(property.getPropertyValue());
			if (personToLink != null) {
				// save new DB reference to found person:
				Entity entity = property.getParent();
				PropertyType newType = HUITypeFactory.getInstance()
						.getEntityType(entity.getEntityType()).getPropertyType(
								newPropTypeID);
				entity.createNewProperty(newType, personToLink.getEntity()
						.getDbId().toString());
				// entityDao.persist(entity);
			}
		}
	}

	private Person findPerson(String personName) {
		List<Person> all = personDao.findAll();
		for (Person person : all) {
			if (person.getTitel().equals(personName))
				return person;
		}
		return null;
	}

	public List findAllPropertiesForTypeId(String propertyTypeId) {
		List<Object[]> findByQuery = propertyDao.findByQuery(QUERY_BY_PROP_ID,
				new String[] { propertyTypeId });
		List result = new ArrayList();
		for (Object[] array : findByQuery) {
			PropertyList list = (PropertyList) array[0];
			propertyDao.initialize(list.getProperties());
			for (Property prop : list.getProperties()) {
				propertyDao.initialize(prop.getParent());
			}
			result.addAll(list.getProperties());
		}
		return result;
	}

}
