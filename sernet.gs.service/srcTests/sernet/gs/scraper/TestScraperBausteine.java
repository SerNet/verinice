package sernet.gs.scraper;

import java.util.List;

import junit.framework.TestCase;
import sernet.gs.model.Baustein;

public class TestScraperBausteine extends TestCase {
	GSScraper scrape;

	@Override
	protected void setUp() throws Exception {
		String zipfile = "/home/aprack/down/gshb/it-grundschutz2006_html_de.zip";
//		String zipfile = "/home/aprack/down/gshb/gshbdeutschhtml2005.zip";
		scrape = new GSScraper(new ZIPGSSource(zipfile),
				new PatternGSHB2005_2006());

	}
	
	public void testGetBausteine() throws Exception {
		
		List<Baustein>  bausteine = scrape.getBausteine("b01");
		assertNotNull(bausteine);
		assertEquals("Falsche Anzahl Bausteine fuer b01.", 14, bausteine.size());
		
//		for (Baustein baustein : bausteine) {
//			System.out.println("Baustein: " + baustein.getId() + ", " 
//					+ baustein.getTitel());
//			 
//		}
		
		bausteine = scrape.getBausteine("b02");
		assertNotNull(bausteine);
		assertEquals("Falsche Anzahl Bausteine fuer b02.", 11, bausteine.size());
		
		for (Baustein baustein : bausteine) {
			assertTrue("Stand wurde nicht gesetzt", 
					baustein.getStand() != null);
			System.out.println(baustein.getTitel() + ": " + baustein.getStand());
		}
		
		
		bausteine = scrape.getBausteine("b03");
		assertNotNull(bausteine);
		assertEquals("Falsche Anzahl Bausteine fuer b03.", 25, bausteine.size());
		
		bausteine = scrape.getBausteine("b04");
		assertNotNull(bausteine);
		assertEquals("Falsche Anzahl Bausteine fuer b04.", 7, bausteine.size());
		
		bausteine = scrape.getBausteine("b05");
		assertNotNull(bausteine);
		assertEquals("Falsche Anzahl Bausteine fuer b05.", 13, bausteine.size());
	}
}
