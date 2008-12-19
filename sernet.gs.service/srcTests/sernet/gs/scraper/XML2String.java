package sernet.gs.scraper;

import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class XML2String {
	public String writeXML(Document doc) {
		StringWriter w = new StringWriter();
		TransformerFactory xformFactory = TransformerFactory.newInstance();
		Transformer idTransform;
		try {
			idTransform = xformFactory.newTransformer();
		Source input = new DOMSource(doc);
		Result output = new StreamResult(w);
		idTransform.transform(input, output);
		
		//System.out.println("Generated XML from user input: " + w.toString());
		
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}
		return w.toString();
	}
}
