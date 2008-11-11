package sernet.gs.scraper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;

public class TestScraperMassnahmen extends TestCase {

	public void testMassnahmenGefaehrdungen() throws Exception {
		String zipfile = "/home/aprack/down/gshb/it-grundschutz2007_html_de.zip";
		GSScraper scrape = new GSScraper(new ZIPGSSource(zipfile),
				new PatternGSHB2005_2006());
		
		List<Baustein>  bausteine = scrape.getBausteine("b01");
		for (Baustein baustein : bausteine) {
			List<Massnahme>  massnahmen = scrape.getMassnahmen(baustein.getUrl());
			for (Massnahme massnahme : massnahmen) {
				scrape.getMassnahme(massnahme.getUrl(), "2006");
			}
			
		}
		
		bausteine = scrape.getBausteine("b02");
		for (Baustein baustein : bausteine) {
			List<Massnahme>  massnahmen = scrape.getMassnahmen(baustein.getUrl());
			for (Massnahme massnahme : massnahmen) {
				scrape.getMassnahme(massnahme.getUrl(), "2006");
			}
			
		}
		
		bausteine = scrape.getBausteine("b03");
		for (Baustein baustein : bausteine) {
			List<Massnahme>  massnahmen = scrape.getMassnahmen(baustein.getUrl());
			for (Massnahme massnahme : massnahmen) {
				scrape.getMassnahme(massnahme.getUrl(), "2006");
			}
			
		}
		
		bausteine = scrape.getBausteine("b04");
		for (Baustein baustein : bausteine) {
			List<Massnahme>  massnahmen = scrape.getMassnahmen(baustein.getUrl());
			for (Massnahme massnahme : massnahmen) {
				scrape.getMassnahme(massnahme.getUrl(), "2006");
			}
			
		}
		
		bausteine = scrape.getBausteine("b05");
		for (Baustein baustein : bausteine) {
			List<Massnahme>  massnahmen = scrape.getMassnahmen(baustein.getUrl());
			for (Massnahme massnahme : massnahmen) {
				scrape.getMassnahme(massnahme.getUrl(), "2006");
			}
			
		}
		
//		List<Massnahme>  massnahmen = scrape.getMassnahmen("b05013");
//		for (Massnahme massnahme : massnahmen) {
////			System.out.println(massnahme.toString());
//			scrape.getMassnahme(massnahme.getUrl(), "2006");
//		}

		
		
		List<Gefaehrdung> gefaehrdungen = scrape.getGefaehrdungen("b01001");
		for (Gefaehrdung gefaehrdung : gefaehrdungen) {
//			System.out.println(gefaehrdung);
			InputStream stream = scrape.getGefaehrdung(gefaehrdung.getUrl(), "2006");
		}
		
		
		//assertNotNull(massnahmen);
		//assertEquals("Falsche Anzahl Massnahmen fuer baustein.", 1, massnahmen.size());
		
//		List<Baustein> bausteine = scrape.getBausteine("b05013");
//		for (Baustein baustein : bausteine) {
//			System.out.println("---------------------------------------------------------------");
//			System.out.println(baustein.getId() + " " +baustein.getTitel());
//			List<Massnahme> massnahmen2 = scrape.getMassnahmen(baustein.getUrl());
//			for (Massnahme massnahme : massnahmen2) {
//				System.out.println(massnahme.toString());
//			}
//		}
		
		
	}
}
