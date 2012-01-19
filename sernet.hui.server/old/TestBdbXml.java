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
package sernet.hui.server.connect;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import junit.framework.TestCase;

import com.sleepycat.collections.MapEntryParameter;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlContainerConfig;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlIndexSpecification;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlManagerConfig;
import com.sleepycat.dbxml.XmlQueryContext;
import com.sleepycat.dbxml.XmlQueryExpression;
import com.sleepycat.dbxml.XmlResults;
import com.sleepycat.dbxml.XmlTransaction;
import com.sleepycat.dbxml.XmlUpdateContext;
import com.sleepycat.dbxml.XmlValue;

public class TestBdbXml extends TestCase {
	
	 private static final String DOC1 =  
			"<Node0 xmlns:bigtest=\"http://testenv.dbxml/bigtest\" id=\"%1$s\">\n" + 
			"<Node1 class=\"myValue1\">Node1 text </Node1>\n" + 
			"<Node2>\n" + 
			"    <Node3>%1$s.1</Node3>\n" + 
			"    <Node3>%1$s.2</Node3>\n" + 
			"    <Node3>%1$s.3</Node3>\n" + 
			"    <Node4>Node4</Node4>\n" + 
			"    </Node2>\n" + 
			"</Node0>";
	
	 private static final String TEST_CONTAINER = "testbdbContainer.bdbxml";

	Environment myEnv = null;

	XmlManager myManager = null;

	 XmlContainer container;
	

	private void loadTest() {
		
		System.out.println("Creating Test DB...");
		XmlTransaction ta = null;
		try {
			// Need an update context for the put.
			XmlUpdateContext theContext = myManager.createUpdateContext();
			ta = myManager.createTransaction();
			
//			 add index for id attributes:
			XmlIndexSpecification is = container.getIndexSpecification();
			is.addIndex("", "id", "node-attribute-equality-string");
			XmlUpdateContext uc = myManager.createUpdateContext();
		    container.setIndexSpecification(ta, is, uc);

			for (int i=0; i < 1000; ++i) {
				StringBuffer buff  = new StringBuffer();
				buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				buff.append(String.format(DOC1, i));
				container.putDocument(ta, "doc"+i, buff.toString(), theContext, null); 
			}
			
			
		    ta.commit();
		} catch (XmlException e) {
			try {
				ta.abort();
			} catch (XmlException e1) {
			}
			fail(e.getMessage());
		}
	}
	
	
	public void testBDB() throws Exception {

		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true); 
		envConf.setInitializeCache(true); 
		envConf.setInitializeLocking(true); 
		envConf.setInitializeLogging(true); 
		envConf.setTransactional(true); 
		envConf.setRunRecovery(true);

		try {
			File envHome = new File("testEnv");
			myEnv = new Environment(envHome, envConf);

			XmlManagerConfig managerConfig = new XmlManagerConfig();
			// close env when manager is closed:
			managerConfig.setAdoptEnvironment(true);

			myManager = new XmlManager(myEnv, managerConfig);
			myManager.setDefaultContainerType(XmlContainer.NodeContainer);
			
			XmlContainerConfig containerConf = new XmlContainerConfig();
		    containerConf.setTransactional(true);
		    
			try {
				container = myManager.createContainer(TEST_CONTAINER, containerConf);
				loadTest();
			}
			catch (XmlException e) {
				System.out.println("Opening container...");
				container = myManager.openContainer(TEST_CONTAINER, containerConf);
			}
			
			queryTest();
		//	queryTest2();
			flworTest();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (Exception re) {
			re.printStackTrace();
		}
		finally {
			container.close();
			myManager.removeContainer(TEST_CONTAINER);
			myManager.close();
		}
	}


	private void flworTest() throws XmlException {
		XmlQueryContext context = myManager.createQueryContext();
		context.setNamespace("bigtest", "http://testenv.dbxml/bigtest");
		context.setEvaluationType(XmlQueryContext.Eager);
		context.setReturnType(XmlQueryContext.DeadValues);
		
		String collection = "collection('"+ TEST_CONTAINER +"')";
		
		String flwor =
			    "for $i in " + collection + "/Node0[@id = $idVar]/*/Node3 " +
				"let $idx := xs:double( data($i) ) " +
				"order by $idx descending " +
				"return $i";
		
		context.setVariableValue("idVar", new XmlValue("333"));
		long start = System.nanoTime();
		XmlQueryExpression qe = myManager.prepare(flwor, context);
		XmlResults results = qe.execute(context);
		long taskTime = System.nanoTime() - start;
		System.out.printf("Time for flwor query: %d ms \n", taskTime/1000000);
		
		System.out.println("FLWOR Results: " + results.size() +"\n");	
		assertEquals("DBD failed", 3, results.size());
		
		XmlValue value = results.next();
		
//	    while (value != null) {
//	        System.out.println(value.asString());
//	        value = results.next();
//	     } 
	}


	private void queryTest2() throws XmlException {
		XmlQueryContext context = myManager.createQueryContext();
		context.setNamespace("bigtest", "http://testenv.dbxml/bigtest");
		//context.setEvaluationType(XmlQueryContext.Eager);
		//context.setReturnType(XmlQueryContext.DeadValues);
		
		String collection = "collection('"+ TEST_CONTAINER+"')";
		String query = "/Node0/*/Node3";
		
		long start = System.nanoTime();
		XmlQueryExpression qe = myManager.prepare( collection+query, context);
		XmlResults results = qe.execute(context);
		long taskTime = System.nanoTime() - start;
		System.out.printf("Time for query2: %d ms \n", taskTime/1000000);
		
		
		System.out.println("Query2 Results: " + results.size() +"\n");	
		assertEquals("DBD failed", 30000, results.size());
	}


	private void queryTest() throws XmlException {
		XmlQueryContext context = myManager.createQueryContext();
		context.setNamespace("bigtest", "http://testenv.dbxml/bigtest");
		context.setEvaluationType(XmlQueryContext.Eager);
		context.setReturnType(XmlQueryContext.DeadValues);
		
		
		String collection = "collection('"+ TEST_CONTAINER+"')";
		String query = "/Node0[@id=$idVar]";
		
		context.setVariableValue("idVar", new XmlValue("999")); 
		
		long start = System.nanoTime();
		XmlQueryExpression qe = myManager.prepare(collection+query, context);
		XmlResults results = qe.execute(context);
		long taskTime = System.nanoTime() - start;
		System.out.printf("Time for query1: %d ms \n", taskTime/1000000);
		
		System.out.println("Query Results: " + results.size() +"\n");		
		assertTrue("DBD Storage failed.", results.size() == 1);
	}


}
