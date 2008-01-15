package sernet.hui.server.connect;

import java.util.List;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.connect.PropertyTypeFactory;
import sernet.hui.server.connect.xml.XmlSerializer;
import sernet.snutils.DBException;
import junit.framework.TestCase;

public class TestSerializer extends TestCase {

	public void testSerialize() throws DBException {
		PropertyTypeFactory.initialize("/home/aprack/java/workspace/sernet.hui.common/" +
		"xml/opengt.xml");
		
		EntityType system = PropertyTypeFactory.getInstance().getEntityType("system");
		Entity entity = new Entity("system");
		entity.setEntityId("system123");
		PropertyType propertyType = system.getPropertyType("leitungsnr");
		entity.createNewProperty(propertyType, "4711");
		entity.createNewProperty(propertyType, "0815");
		
		XmlSerializer toXML = new XmlSerializer();
		String entityXml = toXML.serialize(entity);
		
		Entity ent = toXML.deserialize(entityXml);
		List<Property> properties = ent.getProperties("leitungsnr");
		
		assertEquals("Deserializing of test entity failed.", 
				"system123", ent.getEntityId());
		assertEquals("Deserializing of test entity failed", "4711", 
				properties.get(0).getPropertyValue());
		assertEquals("Deserializing of test entity failed", "0815", 
				properties.get(1).getPropertyValue());
	}
}
