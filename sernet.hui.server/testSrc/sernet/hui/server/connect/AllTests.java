package sernet.hui.server.connect;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for sernet.hui.server.connect");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestHibernate.class);
		suite.addTestSuite(TestEntityHomeSQL.class);
		//$JUnit-END$
		return suite;
	}

}
