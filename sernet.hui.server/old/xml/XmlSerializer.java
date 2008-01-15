package sernet.hui.server.connect.xml;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;

import com.thoughtworks.xstream.XStream;

public class XmlSerializer {
	
	XStream xstream;
	
	public XmlSerializer() {
		xstream = createEntityStream();
	}

	public String serialize(Entity entity) {
		Logger.getLogger(XmlSerializer.class).debug("serialized entity:\n" + xstream.toXML(entity));
		return xstream.toXML(entity);
	}
	
	public Entity deserialize(String xml) {
		//Logger.getLogger(XmlSerializer.class).debug("Creating entity from:\n" + xml);
		Entity entity = (Entity) xstream.fromXML(xml);
		return entity;
	}

	private XStream createEntityStream() {
		XStream xstream = new XStream();
		xstream.alias("entity", Entity.class);
		xstream.aliasAttribute("id", "entityid");
		xstream.aliasAttribute("type", "entType");
		xstream.useAttributeFor("entityid", String.class);
		xstream.useAttributeFor("entType", String.class);
		xstream.omitField(Entity.class, "changeListeners");
		//xstream.addImplicitCollection(Entity.class, "properties");
		
		xstream.alias("property", Property.class);
//		xstream.registerConverter(new PropertyConverter());
		xstream.useAttributeFor("entityId", String.class);
		xstream.useAttributeFor("propertyType", String.class);
		//xstream.omitField(Property.class, "parent");
		return xstream;
	}
}
