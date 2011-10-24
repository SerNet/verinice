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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;
import org.doomdark.uuid.UUIDGenerator;

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
import com.thoughtworks.xstream.core.TreeMarshaller.CircularReferenceException;

import sernet.hui.common.connect.Entity;
import sernet.hui.server.connect.xml.XmlSerializer;
import sernet.snutils.DBException;

public class EntityHomeXML {
	
	private static EntityHomeXML inst;
	
	private DBEnvironment env;
	private boolean open = false;

	private ExecutorService ces;
	
	private EntityHomeXML() {
		try {
			env = new DBEnvironment();
			ces = Executors.newSingleThreadExecutor();
		} catch (Exception e) {
			Logger.getLogger(EntityHomeXML.class).error(e);
			throw new RuntimeException("DB init failed.", e);
		}
	}
	
	public static synchronized EntityHomeXML getInstance() {
		if (inst==null) {
			inst = new EntityHomeXML();
		}
		return inst;
	}

	public synchronized void open() throws XmlException {
		if (!open) {
			env.open();
			open = true;
		}
	}
	
	public synchronized void create() throws XmlException {
		env.create();
	}
	
	public synchronized void close() throws XmlException {
		inst = null;
		env.close();
	}

	public void create(Entity entity) throws XmlException, DBException, IOException  {
			entity.setEntityId(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
			env.createDocument(entity.getEntityId(), serialize(entity));
	}
	
	public void update(Entity entity) throws XmlException, DBException, IOException  {
			env.updateDocument(entity.getEntityId(), serialize(entity));
	}


	public Entity findById(String id) throws XmlException, DBException {
		return deserialize(env.retrieveDocument(id));
	}

	private String serialize(final Entity entity) throws IOException  {
		XmlSerializer toXml = new XmlSerializer();
		return toXml.serialize(entity);
	}
	
	private Entity deserialize(String xml) {
		XmlSerializer fromXml = new XmlSerializer();
		return fromXml.deserialize(xml);
	}

}
