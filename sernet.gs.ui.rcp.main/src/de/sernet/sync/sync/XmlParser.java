package de.sernet.sync.sync;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;



public class XmlParser {
	
	
	@SuppressWarnings("unchecked")
	public static List<Element> parseData(/*ZipInputStream*/ File file){
		Document doc;
		try {
			SAXBuilder saxBuilder = new SAXBuilder(false);
			saxBuilder.setIgnoringBoundaryWhitespace(true);
			saxBuilder.setIgnoringElementContentWhitespace(true);
			doc = saxBuilder.build( file );
			Element syncData = doc.getRootElement();
			Namespace ns = syncData.getNamespace();
			List<Element> syncObject = syncData.getChildren("syncObject", ns);
			
			return syncObject;
			
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Element> parseMapping(/*ZipInputStream*/ File file){
		Document doc;
		try {
			SAXBuilder saxBuilder = new SAXBuilder(false);
			saxBuilder.setIgnoringBoundaryWhitespace(true);
			saxBuilder.setIgnoringElementContentWhitespace(true);
			
			//System.out.println("parseMapping: " + file.getAbsolutePath());
			
			doc = saxBuilder.build( file );
			Element syncData = doc.getRootElement();
			Namespace ns = syncData.getNamespace();
			List<Element> syncObject = syncData.getChildren("mapObjectType", ns);
			
			return syncObject;
			
		} catch (JDOMException e) {
			System.out.println("Fehler 1");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Fehler 2");
			e.printStackTrace();
		} 
		return null;
	}

}
