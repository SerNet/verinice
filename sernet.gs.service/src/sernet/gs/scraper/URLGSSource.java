package sernet.gs.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

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
 * Source to read the GS catalogues from a URL.
 * 
 * @author akoderman@sernet.de
 * 
 */
public class URLGSSource implements IGSSource {

	private String baseUrl;

	private static final String BAUSTEIN_PATH_2005 = "gshb/deutsch/baust/";

	private static final String BAUSTEIN_PATH_2006 = "baust/";

	private static final String MASSNAHME_PATH_2005 = "gshb/deutsch/m/";

	private static final String MASSNAHME_PATH_2006 = "m/";

	private static final String SUFFIX = ".htm";

	private static final String GEFAEHRDUNG_PATH_2005 = "gshb/deutsch/g";

	private static final String GEFAEHRDUNG_PATH_2006 = "g/";

	static final Pattern relPath = Pattern.compile("^../baust/");

	public URLGSSource(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public Node parseDocument(String baustein) throws GSServiceException {
		try {
			InputStream stream = getBausteinAsStream(baustein);
			InputStreamReader reader = new InputStreamReader(stream,
					"ISO-8859-1");
			BufferedReader buffRead = new BufferedReader(reader);

			SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory
					.newInstance();
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
			stream.close();

			return domRootNode;
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(
					"Fehler beim Parsen aus Verzeichnis", e);
			throw new GSServiceException(
					"Fehler beim Parsen eines Bausteins. (URL)", e);
		}

	}

	public InputStream getBausteinAsStream(String baustein)
			throws GSServiceException {
		Matcher matcher = relPath.matcher(baustein);
		baustein = matcher.replaceAll("");
		try {
			URL url = new URL(baseUrl + BAUSTEIN_PATH_2006 + baustein + SUFFIX);
			return url.openStream();
		} catch (Exception e) {
			try {
				URL url = new URL(baseUrl + BAUSTEIN_PATH_2005 + baustein
						+ SUFFIX);
				return url.openStream();
			} catch (IOException e1) {
				throw new GSServiceException("Error parsing BSI document.", e);
			}
		}
	}

	public InputStream getMassnahmeAsStream(String massnahme)
			throws GSServiceException {
		try {
			URL url = new URL(baseUrl + MASSNAHME_PATH_2006 + massnahme
					+ SUFFIX);
			return url.openStream();
		} catch (Exception e) {
			try {
				URL url = new URL(baseUrl + MASSNAHME_PATH_2005 + massnahme
						+ SUFFIX);
				return url.openStream();
			} catch (Exception e2) {
				throw new GSServiceException("Massnahme nicht gefunden: "
						+ massnahme, e2);
			}
		}
	}

	public InputStream getGefaehrdungAsStream(String gefaehrdung)
			throws GSServiceException {

		try {
			URL url = new URL(baseUrl + GEFAEHRDUNG_PATH_2006 + gefaehrdung
					+ SUFFIX);
			return url.openStream();
		} catch (Exception e) {
			try {
				URL url = new URL(baseUrl + GEFAEHRDUNG_PATH_2005 + gefaehrdung
						+ SUFFIX);
				return url.openStream();
			} catch (Exception e2) {
				throw new GSServiceException("Gefaehrdung nicht gefunden: "
						+ gefaehrdung, e2);
			}
		}

	}

}
