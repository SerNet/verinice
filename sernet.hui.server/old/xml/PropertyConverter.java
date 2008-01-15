package sernet.hui.server.connect.xml;

import sernet.hui.common.connect.Property;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PropertyConverter implements Converter {

	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext context) {
//		Property prop = (Property) obj;
//        writer.startNode("type");
//        writer.setValue(prop.getPropertyTypeID());
//        writer.endNode();
//
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return null;
//		Property prop = new Property();
//		reader.moveDown();
//		prop.setEntityId(reader.getValue());
//		reader.moveUp();
//		return prop;
	}

	public boolean canConvert(Class clazz) {
		return clazz.equals(Property.class);
	}

}
