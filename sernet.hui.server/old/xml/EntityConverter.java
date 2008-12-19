package sernet.hui.server.connect.xml;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class EntityConverter implements Converter {

	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		return null;
	}

	public boolean canConvert(Class clazz) {
		return clazz.equals(Entity.class);
	}

}
