/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Person;

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
			}
		}
	}

	private Person findPerson(String personName) {
		List<Person> all = personDao.findAll();
		for (Person person : all) {
			if (person.getTitle().equals(personName)){
				return person;
			}
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
