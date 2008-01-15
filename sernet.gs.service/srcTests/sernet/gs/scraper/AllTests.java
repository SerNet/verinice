package sernet.gs.scraper;

import junit.framework.Test;
import junit.framework.TestSuite;



public class AllTests {

	
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for sernet.gs.scraper");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestScraperMassnahmen.class);
		suite.addTestSuite(TestScraperBausteine.class);
		//$JUnit-END$
		return suite;
	}

}
