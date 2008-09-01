package sernet.gs.ui.rcp.main.bsi.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.derby.impl.drda.DssTrace;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.PlatformUI;

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
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public class BSIMassnahmenModel {

	private static final String DS_B01005_BFDI = "b01005_bfdi";

	private static final String DS_B_1_5 = "B 1.5";

	private static final String DS_2008 = "2008";

	private static final String B01005 = "b01005";

	private static List<Baustein> cache;

	private static GSScraper scrape;

	private static String previouslyReadFile = "";

	private static GSScraper dsScrape;

	private static String previouslyReadFileDS = "";

	public static synchronized List<Baustein> loadBausteine(IProgressMonitor mon)
			throws GSServiceException, IOException {
		Logger.getLogger(BSIMassnahmenModel.class).debug(
				Messages.BSIMassnahmenModel_0);

		IGSSource gsSource = null;
		String gsPath = null;
		Preferences preferences = Activator.getDefault().getPluginPreferences();
		boolean fromZipFile = preferences.getString(
				PreferenceConstants.GSACCESS).equals(
				PreferenceConstants.GSACCESS_ZIP);

		if (fromZipFile) {
			gsPath = preferences.getString(PreferenceConstants.BSIZIPFILE);
		} else {
			gsPath = preferences.getString(PreferenceConstants.BSIDIR);
			gsPath = (new File(gsPath)).toURI().toURL().toString();
		}
		
		String dsPath = preferences.getString(PreferenceConstants.DSZIPFILE);

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
						"GS-Kataloge nicht gefunden.", e);
				return null;
			}

			scrape = new GSScraper(gsSource, new PatternGSHB2005_2006());
			mon.beginTask(Messages.BSIMassnahmenModel_3, 5);
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
					Logger.getLogger(BSIMassnahmenModel.class).debug("Datenschutz-Baustein nicht gefunden.");
				}
			}
			
			cache = alleBst;
			mon.done();
			Logger.getLogger(BSIMassnahmenModel.class).debug(
					Messages.BSIMassnahmenModel_9);

		}
		return cache;
	}

	private static Baustein scrapeDatenschutzBaustein() throws GSServiceException {
    	Baustein b = new Baustein();
    	b.setStand(DS_2008);
    	b.setId(DS_B_1_5);
    	b.setTitel("Datenschutz BfDI");
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
		InputStream bausteinText;
		try {
			bausteinText = scrape.getBausteinText(url, stand);
		} catch (Exception e) {
			bausteinText = dsScrape.getBausteinText(url, stand);
		}
		return bausteinText;
	}

	public static InputStream getMassnahme(String url, String stand)
			throws GSServiceException {
		InputStream massnahme;
		try {
			massnahme = scrape.getMassnahme(url, stand);
		} catch (Exception e) {
			massnahme = dsScrape.getMassnahme(url, stand);
		}
		return massnahme;
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
		InputStream gefaehrdung;
		try {
			gefaehrdung = scrape.getGefaehrdung(url, stand);
		} catch (Exception e) {
			gefaehrdung = dsScrape.getGefaehrdung(url, stand);
		}
		return gefaehrdung;
	}

}