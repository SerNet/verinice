package sernet.gs.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import sernet.gs.service.GSServiceException;

import com.sun.org.apache.xalan.internal.xsltc.trax.SAX2DOM;

/**
 * Source that reads BSI GS catalogues from within
 * a ZIP file.
 * 
 * @author akoderman@sernet.de
 *
 */
public class ZIPGSSource implements IGSSource {
	
	private ZipFile zf;
	private static final String BAUSTEIN_PATH_2005 = "gshb/deutsch/baust/";
	private static final String BAUSTEIN_PATH_2006 = "baust/";
	
	private static final String MASSNAHME_PATH_2005 = "gshb/deutsch/m/";
	private static final String MASSNAHME_PATH_2006 = "m/";
	
	private static final String SUFFIX = ".htm";
	static final Pattern relPath = Pattern.compile("^../baust/");
	
	private static final String GEFAEHRDUNG_PATH_2005 = "gshb/deutsch/g/";
	private static final String GEFAEHRDUNG_PATH_2006 = "g/";
	
	public ZIPGSSource(String fileName) throws IOException {
		zf = new ZipFile(fileName);
	}
	
	public InputStream getBausteinAsStream(String baustein) throws GSServiceException {
		Matcher matcher = relPath.matcher(baustein);
		baustein = matcher.replaceAll("");	
		try {
			ZipEntry entry = zf.getEntry(BAUSTEIN_PATH_2006 + baustein + SUFFIX); 
			return zf.getInputStream(entry);
		} catch (Exception e) {
			ZipEntry entry = zf.getEntry(BAUSTEIN_PATH_2005 + baustein + SUFFIX); 
			try {
				return zf.getInputStream(entry);
			} catch (IOException e1) {
				throw new GSServiceException(e);
			}
		}
	}
	
	public Node parseDocument(String baustein) throws GSServiceException {
		try {
			
			Matcher matcher = relPath.matcher(baustein);
			baustein = matcher.replaceAll("");	
			
			ZipEntry entry = zf.getEntry(BAUSTEIN_PATH_2006 + baustein + SUFFIX); 
			if (entry == null)
				entry = zf.getEntry(BAUSTEIN_PATH_2005 + baustein + SUFFIX); 
				
			InputStream zipIS = zf.getInputStream(entry);
			InputStreamReader reader = new InputStreamReader(zipIS, "ISO-8859-1");
			BufferedReader buffRead = new BufferedReader(reader);
			
		    SAXTransformerFactory stf = 
		      (SAXTransformerFactory) TransformerFactory.newInstance();
		    TransformerHandler th = stf.newTransformerHandler();
		    DOMResult dr = new DOMResult();
		    th.setResult(dr);
		    Parser parser = new Parser();
		    parser.setContentHandler(th);
		    parser.parse(new InputSource(buffRead));
		    Node domRootNode = dr.getNode();
		    domRootNode.normalize();
			
			buffRead.close();
			reader.close();
			zipIS.close();
			
			
//			FileWriter fw = new FileWriter("test.xml");
//			BufferedWriter write = new BufferedWriter(fw);
//			write.write(xml);
//			write.close();
//			zipIS.close();
			
			
			return domRootNode;
		} catch (Exception e) {
			Logger.getLogger(ZIPGSSource.class).error("Fehler beim Parsen eines Bausteins.", e);
			throw new GSServiceException("Fehler beim Parsen eines Bausteins (ZIP).", e);
		}
	
	}

	public InputStream getMassnahmeAsStream(String massnahme) throws GSServiceException {
		try {
			ZipEntry entry = zf.getEntry(MASSNAHME_PATH_2006 + massnahme + SUFFIX); 
			return zf.getInputStream(entry);
		} catch (Exception e) {
			try {
				ZipEntry entry = zf.getEntry(MASSNAHME_PATH_2005 + massnahme + SUFFIX); 
				return zf.getInputStream(entry);
			} catch (Exception e2) {
				throw new GSServiceException("Massnahme nicht gefunden: " + massnahme, e2);
			}
		}
	}
	
	public InputStream getGefaehrdungAsStream(String gefaehrdung) throws GSServiceException {

		try {
			ZipEntry entry = zf.getEntry(GEFAEHRDUNG_PATH_2006 + gefaehrdung + SUFFIX); 
			return zf.getInputStream(entry);
		} catch (Exception e) {
			try {
				ZipEntry entry = zf.getEntry(GEFAEHRDUNG_PATH_2005 + gefaehrdung + SUFFIX); 
				return zf.getInputStream(entry);
			} catch (Exception e2) {
				throw new GSServiceException("Gefaehrdung nicht gefunden: " + gefaehrdung, e2);
			}
		}
	}
	

}
