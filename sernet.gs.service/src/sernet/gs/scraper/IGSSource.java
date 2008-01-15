package sernet.gs.scraper;

import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;

import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;

/**
 * Interface for all types of data sources
 * containing BSI GS-Catalogue information. 
 * 
 * @author akoderman@sernet.de
 *
 */
public interface IGSSource {
	
	public Node parseDocument(String path) throws GSServiceException;
	public InputStream getBausteinAsStream(String baustein) throws GSServiceException;
	public InputStream getMassnahmeAsStream(String massnahme) throws GSServiceException;
	public InputStream getGefaehrdungAsStream(String gefaehrdung) throws GSServiceException;
}
