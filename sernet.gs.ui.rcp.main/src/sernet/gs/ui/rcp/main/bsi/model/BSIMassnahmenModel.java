package sernet.gs.ui.rcp.main.bsi.model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.scraper.GSScraper;
import sernet.gs.scraper.IGSSource;
import sernet.gs.scraper.PatternBfDI2008;
import sernet.gs.scraper.PatternGSHB2005_2006;
import sernet.gs.scraper.URLGSSource;
import sernet.gs.scraper.ZIPGSSource;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.grundschutzparser.GetBausteinText;
import sernet.gs.ui.rcp.main.service.grundschutzparser.GetGefaehrdungText;
import sernet.gs.ui.rcp.main.service.grundschutzparser.GetMassnahmeText;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;

public class BSIMassnahmenModel {

	private static final String DS_B01005_BFDI = "b01005_bfdi"; //$NON-NLS-1$

	private static final String DS_B_1_5 = "B 1.5"; //$NON-NLS-1$

	private static final String DS_2008 = "2008"; //$NON-NLS-1$

	private static final String B01005 = "b01005"; //$NON-NLS-1$

	private static List<Baustein> cache;

	private static GSScraper scrape;

	private static String previouslyReadFile = ""; //$NON-NLS-1$

	private static GSScraper dsScrape;

	private static String previouslyReadFileDS = ""; //$NON-NLS-1$
	
	private static IBSIConfig config;

	public static synchronized List<Baustein> loadBausteine(IProgress mon)
			throws GSServiceException, IOException {
		Logger.getLogger(BSIMassnahmenModel.class).debug(
				"Laden und Zwischenspeichern der GS-Kataloge...");
		
		if (config instanceof BSIConfigurationRemoteSource) {
			return loadBausteineRemote();
		}

		String gsPath = config.getGsPath();
		String dsPath = config.getDsPath();
		boolean fromZipFile = config.isFromZipFile();
		IGSSource gsSource = null;
		String cacheDir = config.getCacheDir();

		// did user really change the path to file?
		if (! (previouslyReadFile.equals(gsPath) && previouslyReadFileDS.equals(dsPath))
				) {
			previouslyReadFile = gsPath;
			previouslyReadFileDS = dsPath;

			try {
				if (fromZipFile)
					gsSource = new ZIPGSSource(gsPath);
				else
					gsSource = new URLGSSource(gsPath);
			} catch (IOException e) {
				Logger.getLogger(BSIMassnahmenModel.class).error(
						"GS-Kataloge nicht gefunden.", e); //$NON-NLS-1$
				return null;
			}

			scrape = new GSScraper(gsSource, new PatternGSHB2005_2006());
			scrape.setCacheDir(cacheDir); //$NON-NLS-1$
			
			Logger.getLogger(BSIMassnahmenModel.class).debug("Setting GS-Cache to " + scrape.getCacheDir()); //$NON-NLS-1$
			mon.beginTask("Laden und Zwischenspeichern der GS-Kataloge...", 5);
			List<Baustein> alleBst = new ArrayList<Baustein>();

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[0]);
			alleBst.addAll(scrapeBausteine("b01")); //$NON-NLS-1$
			mon.worked(1);

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[1]);
			alleBst.addAll(scrapeBausteine("b02")); //$NON-NLS-1$
			mon.worked(1);

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[2]);
			alleBst.addAll(scrapeBausteine("b03")); //$NON-NLS-1$
			mon.worked(1);

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[3]);
			alleBst.addAll(scrapeBausteine("b04")); //$NON-NLS-1$
			mon.worked(1);

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[4]);
			alleBst.addAll(scrapeBausteine("b05")); //$NON-NLS-1$
			mon.worked(1);
			
			// if a source for data privacy module is defined, replace the temporary module with the real one:
			if (dsPath != null && dsPath.length() > 0) {
				try {
					ZIPGSSource dsSource = new ZIPGSSource(dsPath);
					dsScrape = new GSScraper(dsSource, new PatternBfDI2008());
					dsScrape.setCacheDir(cacheDir); //$NON-NLS-1$
					
					Baustein dsBaustein = scrapeDatenschutzBaustein();
					
					searchDataPrivacyModule: for (Iterator iterator = alleBst.iterator(); iterator.hasNext();) {
						Baustein baustein = (Baustein) iterator.next();
						if (baustein.getUrl().indexOf(B01005) > -1) {
							alleBst.remove(baustein);
							break searchDataPrivacyModule;
						}
					}
					alleBst.add(dsBaustein);
				} catch (Exception e) {
					Logger.getLogger(BSIMassnahmenModel.class).debug("Datenschutz-Baustein nicht gefunden."); //$NON-NLS-1$
				}
			}
			
			cache = alleBst;
			mon.done();
			Logger.getLogger(BSIMassnahmenModel.class).debug(
					"GS-Kataloge loaded.");

		}
		return cache;
	}

	private static List<Baustein> loadBausteineRemote() throws GSServiceException {
		// use remote source
		try {
			LoadBausteine command = new LoadBausteine();
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			return command.getBausteine();
		} catch (CommandException e) {
			throw new GSServiceException(e.getCause());
		}
	}

	private static Baustein scrapeDatenschutzBaustein() throws GSServiceException {
    	Baustein b = new Baustein();
    	b.setStand(DS_2008);
    	b.setId(DS_B_1_5);
    	b.setTitel("Datenschutz BfDI"); //$NON-NLS-1$
    	b.setUrl(DS_B01005_BFDI);
    	b.setSchicht(1);
    	
    	List<Massnahme> massnahmen = dsScrape.getMassnahmen(b.getUrl());
		b.setMassnahmen(massnahmen);

		List<Gefaehrdung> gefaehrdungen = dsScrape.getGefaehrdungen(b.getUrl());
		b.setGefaehrdungen(gefaehrdungen);
    	
    	return b;
	}

	public static InputStream getBaustein(String url, String stand) 
		throws GSServiceException {
		
		if (config instanceof BSIConfigurationRemoteSource) {
			return getBausteinFromServer(url, stand);
		}	
		
		InputStream bausteinText = null;
		try {
			bausteinText = scrape.getBausteinText(url, stand);
		} catch (Exception e) {
			if (dsScrape != null)
				bausteinText = dsScrape.getBausteinText(url, stand);
		}
		return bausteinText;
	}

	private static InputStream getBausteinFromServer(String url, String stand) throws GSServiceException {
		GetBausteinText command = new GetBausteinText(url, stand);
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			String bausteinText = command.getBausteinText();
			return stringToStream(bausteinText);
		} catch (CommandException e) {
			throw new GSServiceException(e.getCause());
		} catch (UnsupportedEncodingException e) {
			throw new GSServiceException(e.getCause());
		}
	}

	private static InputStream stringToStream(String text) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(text.getBytes("iso-8859-1"));
	}

	public static InputStream getMassnahme(String url, String stand)
			throws GSServiceException {
		
		if (config instanceof BSIConfigurationRemoteSource) {
			return getMassnahmeFromServer(url, stand);
		}	
		
		InputStream massnahme = null;
		try {
			massnahme = scrape.getMassnahme(url, stand);
		} catch (Exception e) {
			if (dsScrape != null)
				massnahme = dsScrape.getMassnahme(url, stand);
		}
		return massnahme;
	}

	private static InputStream getMassnahmeFromServer(String url, String stand) throws GSServiceException {
		try {
			GetMassnahmeText command = new GetMassnahmeText(url, stand);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			String text = command.getText();
			return stringToStream(text);
		} catch (CommandException e) {
			throw new GSServiceException(e.getCause());
		} catch (UnsupportedEncodingException e) {
			throw new GSServiceException(e.getCause());
		}
	}

	private static List<Baustein> scrapeBausteine(String schicht)
			throws GSServiceException {
		List<Baustein> bausteine = scrape.getBausteine(schicht);
		for (Baustein baustein : bausteine) {
			List<Massnahme> massnahmen = scrape
					.getMassnahmen(baustein.getUrl());
			baustein.setMassnahmen(massnahmen);

			List<Gefaehrdung> gefaehrdungen = scrape
					.getGefaehrdungen(baustein.getUrl());
			baustein.setGefaehrdungen(gefaehrdungen);
		}
		return bausteine;
	}

	public static InputStream getGefaehrdung(String url, String stand) 
		throws GSServiceException {
		
		if (config instanceof BSIConfigurationRemoteSource) {
			return getGefaehrdungFromServer(url, stand);
		}	
		
		InputStream gefaehrdung = null;
		try {
			gefaehrdung = scrape.getGefaehrdung(url, stand);
		} catch (Exception e) {
			if (dsScrape != null)
				gefaehrdung = dsScrape.getGefaehrdung(url, stand);
		}
		return gefaehrdung;
	}

	private static InputStream getGefaehrdungFromServer(String url, String stand) throws GSServiceException {
		try {
			GetGefaehrdungText command = new GetGefaehrdungText(url, stand);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			String text = command.getText();
			return stringToStream(text);
		} catch (CommandException e) {
			throw new GSServiceException(e.getCause());
		} catch (UnsupportedEncodingException e) {
			throw new GSServiceException(e.getCause());
		}
	}

	public static void flushCache() {
		if (scrape!= null)
			scrape.flushCache();
		if (dsScrape!= null)
			dsScrape.flushCache();
	}

	public static IBSIConfig getConfig() {
		return config;
	}

	public static void setConfig(IBSIConfig config) {
		BSIMassnahmenModel.config = config;
	}

	

}