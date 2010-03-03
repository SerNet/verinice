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
package sernet.hui.server.connect;

import java.net.URL;
import java.util.List;

import junit.framework.TestCase;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;


public class TestEntityHomeSQL extends TestCase {

	public void testSaveNew() throws Exception {
		try {
			HUITypeFactory htf = HUITypeFactory
					.createInstance(new URL("/home/aprack/java/workspace/sernet.hui.common/"
							+ "xml/opengt.xml"));
			EntityType system = HUITypeFactory.getInstance()
					.getEntityType("system");

			Entity entity = new Entity("system");
			PropertyType propertyType = system.getPropertyType("leitungsnr");
			entity.createNewProperty(propertyType, "4711");
			entity.createNewProperty(propertyType, "0815");

			EntityHome.getInstance().open();
			EntityHome.getInstance().create(entity);
			int id = entity.getDbId();

			Entity loadedEntity = EntityHome.getInstance().findEntityById(id);
			Property prop = loadedEntity.getProperties("leitungsnr").getProperty(0);
			assertEquals("Property is wrong.", "4711", prop.getPropertyValue());

			// change value, save and reload:
			prop.setPropertyValue("232323232323");
			EntityHome.getInstance().update(loadedEntity);
			Entity loadedEntity2 = EntityHome.getInstance().findEntityById(id);
			Property prop2 = loadedEntity.getProperties("leitungsnr").getProperty(0);
			assertEquals("Property is wrong.", "232323232323", prop.getPropertyValue());
			assertTrue("Property parent is missing.", prop.getParent() != null);
			assertTrue("Property parent is wrong class.",prop.getParent() instanceof Entity);

			// find by entity type:
			List<Entity> allSystems = EntityHome.getInstance().findByCategory("system");
			assertTrue("Find by category failed.", allSystems.size() > 0);
			for (Entity entity2 : allSystems) {
				System.out.println("EntityHome saved entities: " + entity2.getDbId() + ": ");
				System.out.println(entity2.getProperties("leitungsnr").getProperty(0).getPropertyValue());
				System.out.println("---");
			}
			
			
		} finally {
			EntityHome.getInstance().close();
		}

	}

}
