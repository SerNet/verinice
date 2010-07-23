package sernet.gs.ui.rcp.main;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import sernet.gs.ui.rcp.main.bsi.dialogs.Messages;

public class CreateXMLElement {
	private static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String SOAP = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String SYNC = "http://www.sernet.de/sync/sync";
	private static final String DATA = "http://www.sernet.de/sync/data";
	private static final String MAPPING = "http://www.sernet.de/sync/mapping";
	private Document xmlDoc;
	private Element syncData;
	private Element syncMapping;
	
	public CreateXMLElement() {
		xmlDoc = new Document();
	}
	public void syncRequest(boolean insert, boolean update, boolean delete, String sourceId){
		Namespace xsi = Namespace.getNamespace(SYNC);
		Namespace xsi2 = Namespace.getNamespace("xsi", XSI);
		Element syncRequest = new Element("syncRequest", xsi);
		syncRequest.setAttribute("schemaLocation", SYNC +" sync.xsd", xsi2);
		
		if(sourceId.equals(""))
			syncRequest.setAttribute("sourceId", "Default-Id");
		else
			syncRequest.setAttribute("sourceId", sourceId);
		
		syncRequest.setAttribute("insert", String.valueOf(insert));
		syncRequest.setAttribute("update", String.valueOf(update));
		syncRequest.setAttribute("delete", String.valueOf(delete));
		
		syncData = new Element("syncData");
		syncRequest.addContent(syncData);
		
		syncMapping = new Element("syncMapping");
		syncRequest.addContent(syncMapping);
		
		this.xmlDoc.setRootElement(syncRequest);
	}
	public Element syncRequestXMLFiles(boolean insert, boolean update, boolean delete, String sourceId){
		Namespace xsi = Namespace.getNamespace(SYNC);
		Namespace xsi2 = Namespace.getNamespace("xsi", XSI);
		Element syncRequest = new Element("syncRequest", xsi);
		syncRequest.setAttribute("schemaLocation", SYNC +" sync.xsd", xsi2);
		
		if(sourceId.equals(""))
			syncRequest.setAttribute("sourceId", "Default-Id");
		else
			syncRequest.setAttribute("sourceId", sourceId);
		
		syncRequest.setAttribute("insert", String.valueOf(insert));
		syncRequest.setAttribute("update", String.valueOf(update));
		syncRequest.setAttribute("delete", String.valueOf(delete));
		return syncRequest;
	}
	public void mapping(String entityName, String EntityNameId, Vector<Vector<String>> propertyContainer){
		Namespace namespace = Namespace.getNamespace(MAPPING);
		this.syncMapping.setNamespace(namespace);
		Element mapObjectType = new Element("mapObjectType", namespace);
		mapObjectType.setAttribute("extId", entityName);
		mapObjectType.setAttribute("intId", EntityNameId);
		
		for(Vector<String> properties: propertyContainer){
			Element attribute = new Element("mapAttributeType", namespace);
			attribute.setAttribute("extId", properties.get(1));
			attribute.setAttribute("intId", properties.get(0));
			mapObjectType.addContent(attribute);
		}
		this.syncMapping.addContent(mapObjectType);
	}
	public void data(String entityName, Vector<Vector<String>> inhaltDerTabelle, String[] spalten){
		Namespace namespace = Namespace.getNamespace(DATA);
		this.syncData.setNamespace(namespace);
		for(int i = 0; i < inhaltDerTabelle.size(); i++){
			Element syncObject = new Element("syncObject", namespace);
			syncObject.setAttribute("extId", entityName+ String.valueOf(i));
			syncObject.setAttribute("extObjectType", entityName);
			for(int j = 0; j < inhaltDerTabelle.get(i).size(); j++){
				Element attribute = new Element("syncAttribute", namespace);
				attribute.setAttribute("name", spalten[j]);
				attribute.setAttribute("value", inhaltDerTabelle.get(i).get(j));
				syncObject.addContent(attribute);
			}
			this.syncData.addContent(syncObject);
		}
	}
	public void show(){
		Format format = Format.getPrettyFormat();
		format.setEncoding("UTF-8");
		XMLOutputter out = new XMLOutputter(format); 
		try {
			out.output( this.xmlDoc, System.out );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Document getXMLDocument() {
		return xmlDoc;
	}
	
	//For XML Import
	public void getSyncRequestXMLFiles(File file, boolean insert, 
			boolean update, boolean delete, String sourceId){
		try {
			xmlDoc = new SAXBuilder().build(file);
			Element data = xmlDoc.getRootElement();
			data.setAttribute("sourceId", sourceId);
			data.setAttribute("insert", String.valueOf(insert));
			data.setAttribute("update", String.valueOf(update));
			data.setAttribute("delete", String.valueOf(delete));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//For XML Import
	public File getSyncRequestXML(){
		File syncRequestXML = new File("syncRequestXML.xml");
		Format format = Format.getPrettyFormat();
		format.setEncoding("UTF-8");
		XMLOutputter xmlOut = new XMLOutputter(format);
		try {
			xmlOut.output(xmlDoc, new FileOutputStream(syncRequestXML));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return syncRequestXML;
	}
}