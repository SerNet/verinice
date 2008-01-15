package sernet.hui.server.connect;

import java.util.List;

import junit.framework.TestCase;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.connect.HUITypeFactory;


public class TestEntityHomeSQL extends TestCase {

	public void testSaveNew() throws Exception {
		try {
			HUITypeFactory
					.initialize("/home/aprack/java/workspace/sernet.hui.common/"
							+ "xml/opengt.xml");
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
