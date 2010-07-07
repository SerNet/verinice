package de.sernet.sync.sync;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.springframework.remoting.jaxrpc.JaxRpcPortProxyFactoryBean;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;

public class AxisPortProxyFactoryBean extends JaxRpcPortProxyFactoryBean
									 /* implements JaxRpcServicePostProcessor*/ {

	public void postProcessJaxRpcService(Service service) {

		System.out.println("postProcessJaxRpcService");

		TypeMappingRegistry registry = service.getTypeMappingRegistry();

		// mapping for SyncRequest Object
		TypeMapping mapping = registry.createTypeMapping();

		registerBeanMapping(mapping, SyncRequest.class, "SyncRequest");

		registerBeanMapping(mapping, SyncData.class, "SyncData");

		registerBeanMapping(mapping, SyncData.SyncObject.class,
				"SyncData$SyncObject");

		registerBeanMapping(mapping, SyncData.SyncObject.SyncAttribute.class,
				"SyncData$SyncObject$SyncAttribute");

		// registerBeanMapping(mapping, SyncData.SyncObject.class,
		// "SyncData.SyncObject");

		registerBeanMapping(mapping, SyncMapping.class, "SyncMapping");

		registerBeanMapping(mapping, SyncMapping.MapObjectType.class,
				"SyncMapping$MapObjectType");

		registerBeanMapping(mapping,
				SyncMapping.MapObjectType.MapAttributeType.class,
				"SyncMapping$MapObjectType$MapAttributeType");

		// 1 registry.register("http://schemas.xmlsoap.org/soap/encoding/",
		// mapping);
		// i don't want to know why, but it works

		// mapping for SyncData Object

		// TypeMapping syncData = registry.createTypeMapping();

		// mapping for SyncMapping Object
		// registerBeanMapping(mapping, SyncMapping.class, "SyncMapping");

		registry.register("", mapping);

		// registry.register("http://schemas.xmlsoap.org/soap/encoding/",
		// mapping);

	}

	protected void registerBeanMapping(TypeMapping mapping, Class type,
			String name) {

		System.out.println("registerBeanMapping");

		// static address. bad.
		QName qName = new QName(
				"http://localhost:8080/veriniceserver/sync/syncService", name);

		// QName qName = new QName("urn:syncService", name);

		mapping.register(type, qName, new BeanSerializerFactory(type, qName),
				new BeanDeserializerFactory(type, qName));

		System.out.println("#registered: " + name);
	}
}
