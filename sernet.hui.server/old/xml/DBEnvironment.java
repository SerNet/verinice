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
package sernet.hui.server.connect.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import sernet.snutils.DBException;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockDetectMode;
import com.sleepycat.db.PreparedTransaction;
import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlContainerConfig;
import com.sleepycat.dbxml.XmlDocument;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlIndexSpecification;
import com.sleepycat.dbxml.XmlInputStream;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlManagerConfig;
import com.sleepycat.dbxml.XmlQueryContext;
import com.sleepycat.dbxml.XmlQueryExpression;
import com.sleepycat.dbxml.XmlResults;
import com.sleepycat.dbxml.XmlTransaction;
import com.sleepycat.dbxml.XmlUpdateContext;
import com.sleepycat.dbxml.XmlValue;

public class DBEnvironment {

	private Environment myEnv = null;
	
	private DBConfiguration conf;

	private XmlManager myManager;

	private XmlContainer container;
	
	public DBEnvironment() throws IOException, DatabaseException  {
		conf = new DBConfiguration();
		
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setRunRecovery(true);
		envConf.setAllowCreate(true); 
		envConf.setInitializeCache(true); 
		envConf.setInitializeLocking(true); 
		envConf.setInitializeLogging(true); 
		envConf.setTransactional(true); 

		File envHome = new File(conf.getEnvDir());
		Logger.getLogger(DBEnvironment.class).debug("DB XML Environment: " + envHome.getAbsolutePath());
		myEnv = new Environment(envHome, envConf);
//		PreparedTransaction[] transactions = myEnv.recover(20000, false);
//		for (int i = 0; i < transactions.length; i++) {
//			Logger.getLogger(DBEnvironment.class).debug("Abort transaction " + i);
//			transactions[i].getTransaction().abort();
//		}
		
		XmlManagerConfig managerConfig = new XmlManagerConfig();
		// close env when manager is closed:
		managerConfig.setAdoptEnvironment(true);

		myManager = new XmlManager(myEnv, managerConfig);
		myManager.setDefaultContainerType(XmlContainer.NodeContainer);
	}
	
	public void open() throws XmlException {
			XmlContainerConfig containerConf = new XmlContainerConfig();
		    containerConf.setTransactional(true);
		    
		    Logger.getLogger(DBEnvironment.class).debug("Opening container...");
			container = myManager.openContainer(conf.getContainer(), containerConf);
	}
	
	public void create() throws XmlException {
			XmlContainerConfig containerConf = new XmlContainerConfig();
		    containerConf.setTransactional(true);
		    
		    Logger.getLogger("HUI-Connect").debug("Creating container");
			container = myManager.createContainer(conf.getContainer(), containerConf);
			
			// add index for id attributes:
			XmlIndexSpecification is = container.getIndexSpecification();
			is.addIndex("", "id", "node-attribute-equality-string");
			XmlUpdateContext uc = myManager.createUpdateContext();
		    container.setIndexSpecification(is, uc);
	}
	
	public void close() throws XmlException {
		container.close();
		myManager.close();
	}

	public void updateDocument(String docId, String  document) throws XmlException, DBException {
		if (container == null) 
			throw new DBException("DB closed");
		
		XmlTransaction ta = null;
		try {
			XmlUpdateContext theContext = myManager.createUpdateContext();
			ta = myManager.createTransaction();
		    XmlDocument theDoc = container.getDocument(ta, docId);
		    // TODO compare timestamps (optimistic locking)
		    
		    theDoc.setContent(document);
		    container.updateDocument(ta, theDoc, theContext); 
		    ta.commit();
		} catch (XmlException e) {
			try {
				ta.abort();
			} catch (XmlException e1) {
				Logger.getLogger(DBEnvironment.class).error(e1);
			}
			throw e;
		}
	}

	public void createDocument(String docId, String document) throws XmlException, DBException {
		if (container == null) 
			throw new DBException("DB closed");
		
		XmlTransaction ta = null;
		try {
			XmlUpdateContext theContext = myManager.createUpdateContext();
			ta = myManager.createTransaction();
			
			
			container.putDocument(ta, docId, document,
					theContext, null); 
		    ta.commit();
		} catch (XmlException e) {
			try {
				ta.abort();
			} catch (XmlException e1) {
				Logger.getLogger(DBEnvironment.class).error(e1);
			}
			throw e;
		}
	}

	public String retrieveDocument(String id) throws XmlException, DBException {
		if (container == null) 
			throw new DBException("DB closed");
		
		try {
		    XmlDocument theDoc = container.getDocument(id);
		    return theDoc.getContentAsString();
		} catch (XmlException e) {
			throw e;
		}
	}
	
	
}
