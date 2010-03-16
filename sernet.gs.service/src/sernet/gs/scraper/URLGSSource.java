/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sernet.gs.service.GSServiceException;

/**
 * Source to read the GS catalogues from a URL.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class URLGSSource implements IGSSource {

	private static final Logger LOG = Logger.getLogger(URLGSSource.class);
	
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

	public Node parseBausteinDocument(String baustein) throws GSServiceException {
		try {
			InputStream stream = getBausteinAsStream(baustein);
			return (parseDocument(stream));
		} catch (Exception e) {
			final String message = "Fehler beim Parsen des Bausteins: " + baustein;
			LOG.error(message);
			if (LOG.isDebugEnabled()) {
				LOG.debug("stacktrace: ", e);
			}
			throw new GSServiceException(message, e);
		}

	}

	private Node parseDocument(InputStream stream) throws TransformerConfigurationException, IOException, SAXException {
		InputStreamReader reader = new InputStreamReader(stream, "ISO-8859-1");
		BufferedReader buffRead = new BufferedReader(reader);
		SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
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
	}

	public InputStream getBausteinAsStream(String baustein) throws GSServiceException {
		Matcher matcher = relPath.matcher(baustein);
		baustein = matcher.replaceAll("");
		try {
			URL url = new URL(baseUrl + BAUSTEIN_PATH_2006 + baustein + SUFFIX);
			return url.openStream();
		} catch (Exception e) {
			try {
				URL url = new URL(baseUrl + BAUSTEIN_PATH_2005 + baustein + SUFFIX);
				return url.openStream();
			} catch (IOException e1) {
				throw new GSServiceException("Error parsing BSI document.", e);
			}
		}
	}

	public InputStream getMassnahmeAsStream(String massnahme) throws GSServiceException {
		try {
			URL url = new URL(baseUrl + MASSNAHME_PATH_2006 + massnahme + SUFFIX);
			return url.openStream();
		} catch (Exception e) {
			try {
				URL url = new URL(baseUrl + MASSNAHME_PATH_2005 + massnahme + SUFFIX);
				return url.openStream();
			} catch (Exception e2) {
				throw new GSServiceException("Massnahme nicht gefunden: " + massnahme, e2);
			}
		}
	}

	public InputStream getGefaehrdungAsStream(String gefaehrdung) throws GSServiceException {

		try {
			URL url = new URL(baseUrl + GEFAEHRDUNG_PATH_2006 + gefaehrdung + SUFFIX);
			return url.openStream();
		} catch (Exception e) {
			try {
				URL url = new URL(baseUrl + GEFAEHRDUNG_PATH_2005 + gefaehrdung + SUFFIX);
				return url.openStream();
			} catch (Exception e2) {
				throw new GSServiceException("Gefaehrdung nicht gefunden: " + gefaehrdung, e2);
			}
		}

	}

	public Node parseMassnahmenDocument(String path) throws GSServiceException {
		try {
			return parseDocument(getMassnahmeAsStream(path));
		} catch (TransformerConfigurationException e) {
			throw new GSServiceException("Fehler beim Parsen der Massnahme.", e);
		} catch (IOException e) {
			throw new GSServiceException("Fehler beim Parsen der Massnahme.", e);
		} catch (SAXException e) {
			throw new GSServiceException("Fehler beim Parsen der Massnahme.", e);
		}
	}

	/* (non-Javadoc)
	 * @see sernet.gs.scraper.IGSSource#getVintage()
	 */
	public String getVintage() {
		return IGSSource.VINTAGE_2006;
	}
	
}
