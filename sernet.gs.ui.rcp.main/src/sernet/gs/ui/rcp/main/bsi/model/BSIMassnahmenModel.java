package sernet.gs.ui.rcp.main.bsi.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
import sernet.gs.scraper.URLGSSource;
import sernet.gs.scraper.ZIPGSSource;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public class BSIMassnahmenModel {

	private static List<Baustein> cache;

	private static GSScraper scrape;

	private static String previouslyReadFile = "";

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

		if (!previouslyReadFile.equals(gsPath)) {
			previouslyReadFile = gsPath;

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

			scrape = new GSScraper(gsSource);
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
			cache = alleBst;
			mon.done();
			Logger.getLogger(BSIMassnahmenModel.class).debug(
					Messages.BSIMassnahmenModel_9);

		}
		return cache;
	}

	public static InputStream getBaustein(String url, String stand) 
		throws GSServiceException {
		return scrape.getBaustein(url, stand);
	}

	public static InputStream getMassnahme(String url, String stand)
			throws GSServiceException {
		return scrape.getMassnahme(url, stand);
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
		return scrape.getGefaehrdung(url, stand);
	}

}