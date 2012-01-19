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
 *     Robert Schuster <r.schuster@tarent.de> - load file from URL 
 ******************************************************************************/
package sernet.gs.scraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sernet.gs.service.GSServiceException;

/**
 * Source that reads BSI GS catalogues from within
 * a ZIP file.
 * 
 * @author akoderman[at]sernet[dot]de
 *
 */
public class ZIPGSSource implements IGSSource {
	
	Logger log = Logger.getLogger(ZIPGSSource.class);
	
	private ZipFile zf;
	private static final String BAUSTEIN_PATH_2005 = "gshb/deutsch/baust/";
	private static final String BAUSTEIN_PATH_2006 = "baust/";
	private static final String BAUSTEIN_PATH_DATENSCHUTZ = "B1.5-Datenschutz/www.bsi.de/gshb/baustein-datenschutz/html/";
	private static final String PREFIX_2009 = "it-grundschutz_el11_html/";
	private static final String BAUSTEIN_PATH_2009 = PREFIX_2009 + "baust/";
	
	private static final String MASSNAHME_PATH_2005 = "gshb/deutsch/m/";
	private static final String MASSNAHME_PATH_2006 = "m/";
	private static final String MASSNAHME_PATH_DATENSCHUTZ1 = "B1.5-Datenschutz/www.bsi.de/gshb/baustein-datenschutz/html/";
	private static final String MASSNAHME_PATH_DATENSCHUTZ2 = "B1.5-Datenschutz/www.bsi.de/gshb/";
	
	private static final String SUFFIX = ".htm";
	private static final String SUFFIX_2009 = ".html";
	
	static final Pattern relPath = Pattern.compile("^../baust/");
	
	private static final String GEFAEHRDUNG_PATH_2005 = "gshb/deutsch/g/";
	private static final String GEFAEHRDUNG_PATH_2006 = "g/";
	private static final String GEFAEHRDUNG_PATH_DATENSCHUTZ = "B1.5-Datenschutz/www.bsi.de/gshb/baustein-datenschutz/html/";

	
	public ZIPGSSource(String fileName) throws IOException {
		// fileName may be an URL actually. In that case we transparently
		// retrieve it from the URL and place the contents in a 
		// temp file from which it is accessed normally.
		// However if what we have is a 'file:' url then we extract
		// the filesystem path out of it.
		if (fileName.startsWith("file://"))
			fileName = fileName.substring(7);
		
		File file = new File(fileName);
		if (!file.exists())
		{
			log.debug("Catalogue file is not in local filesystem. Retrieving it from URL and placing into temp file.");
			file = File.createTempFile("verinice", "zip");
			FileUtils.copyURLToFile(new URL(fileName), file);
		}
		
		zf = new ZipFile(file);
	}
	
	public InputStream getBausteinAsStream(String baustein) throws GSServiceException {
		Matcher matcher = relPath.matcher(baustein);
		baustein = matcher.replaceAll("");	
		try {
			ZipEntry entry = zf.getEntry(BAUSTEIN_PATH_2006 + baustein + SUFFIX); 
			return zf.getInputStream(entry);
		} catch (Exception e) {
			try {
				ZipEntry entry = zf.getEntry(BAUSTEIN_PATH_2005 + baustein + SUFFIX); 
				return zf.getInputStream(entry);
			} catch (Exception e1) {
				try {
					ZipEntry entry = zf.getEntry(BAUSTEIN_PATH_DATENSCHUTZ + baustein + SUFFIX); 
					return zf.getInputStream(entry);
				} catch (Exception e2) {
					try {
						ZipEntry entry = getBausteinPath2009(zf, baustein); 
						return zf.getInputStream(entry);
					} catch (Exception e3) {
						throw new GSServiceException(e3);
					}
				}
			}
		}
	}
	
	private Node parseDocument(InputStream inputstream, String encoding) 
		throws TransformerConfigurationException, IOException, SAXException {
		
		InputStreamReader reader = new InputStreamReader(inputstream, encoding);
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
		inputstream.close();
		
		return domRootNode;
		
	}
	
	public Node parseBausteinDocument(String bausteinFileName) throws GSServiceException {
		try {
			
			Matcher matcher = relPath.matcher(bausteinFileName);
			bausteinFileName = matcher.replaceAll("");	
			
			ZipEntry entry = zf.getEntry(BAUSTEIN_PATH_2006 + bausteinFileName + SUFFIX); 
			if (entry == null)
				entry = zf.getEntry(BAUSTEIN_PATH_2005 + bausteinFileName + SUFFIX);
			if (entry == null)
				entry = zf.getEntry(BAUSTEIN_PATH_DATENSCHUTZ + bausteinFileName + SUFFIX);
			if (entry == null)
				entry = getBausteinPath2009(zf, bausteinFileName);
				
				
			if (entry == null)
				throw new GSServiceException("Feler beim Laden des Bausteins: " + bausteinFileName);
			
			return parseDocument(zf.getInputStream(entry), getVintage().equals(IGSSource.VINTAGE_2009) ? "utf-8" : "iso-8859-1");
			
		
		} catch (Exception e) {
			Logger.getLogger(ZIPGSSource.class).error("Fehler beim Parsen eines Bausteins.", e);
			throw new GSServiceException("Fehler beim Parsen eines Bausteins (ZIP).", e);
		}
	
	}

	/**
	 * @param zf2
	 * @param bausteinFileName
	 * @param suffix2
	 * @return
	 */
	private ZipEntry getBausteinPath2009(ZipFile zf, String bausteinFileName) {
		ZipEntry entry = null;
		Pattern pat = Pattern.compile("(b\\d\\d).*");
		Matcher matcher = pat.matcher(bausteinFileName);
		if (matcher.find()) {
			String chapter = matcher.group(1);
			String path = BAUSTEIN_PATH_2009 + chapter + "/" + bausteinFileName + SUFFIX_2009;
			entry = zf.getEntry(path);
			
		}
		return entry;
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
				try {
					ZipEntry entry = zf.getEntry(MASSNAHME_PATH_DATENSCHUTZ1 + massnahme + SUFFIX); 
					return zf.getInputStream(entry);
				} catch (Exception e3) {
					try {
						ZipEntry entry = zf.getEntry(MASSNAHME_PATH_DATENSCHUTZ2 + massnahme + SUFFIX); 
						return zf.getInputStream(entry);
					} catch (Exception e4) {
						try {
							ZipEntry entry = zf.getEntry(getPath2009("m", massnahme)); 
							return zf.getInputStream(entry);
						} catch (Exception e5) {
							throw new GSServiceException("Massnahme nicht gefunden: " + massnahme, e4);
						}
					}
				}
			}
		}
	}
	
	/**
	 * @param massnahme
	 * @return
	 */
	private String getPath2009(String dir, String fileName) {
		String path= "";
		Pattern pattern = Pattern.compile("(" + dir + "\\d\\d).*");
		Matcher matcher = pattern.matcher(fileName);
		if (matcher.find()) {
			String chapter = matcher.group(1);
			path = PREFIX_2009 + dir + "/" + chapter + "/" + fileName + SUFFIX_2009;
		}
		return path;
		
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
				try {
					ZipEntry entry = zf.getEntry(GEFAEHRDUNG_PATH_DATENSCHUTZ + gefaehrdung + SUFFIX); 
					return zf.getInputStream(entry);
				} catch (Exception e3) {
					try {
						ZipEntry entry = zf.getEntry(getPath2009("g", gefaehrdung)); 
						return zf.getInputStream(entry);
					} catch (Exception e4) {
						throw new GSServiceException("Massnahme nicht gefunden: " + gefaehrdung, e4);
					}
				}
			}
		}
	}

	public Node parseMassnahmenDocument(String path) throws GSServiceException {
		try {
			InputStream stream = getMassnahmeAsStream(path);
			
//			InputStreamReader reader = new InputStreamReader(stream, "ISO-8859-1");
//			BufferedReader buffRead2 = new BufferedReader(reader);
//			String line;
//			while ((line = buffRead2.readLine()) != null ) {
//				System.out.println(line);
//			}
			
			return parseDocument(stream, getVintage().equals(IGSSource.VINTAGE_2009) ? "utf-8" : "iso-8859-1");
			
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
		if (getBausteinPath2009(zf, "b01001") != null)
			return IGSSource.VINTAGE_2009;
		else
			return IGSSource.VINTAGE_2006;
	}

}
