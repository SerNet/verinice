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
package sernet.hui.server.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.log4j.Logger;

import com.sleepycat.dbxml.XmlException;

import sernet.hui.common.connect.Entity;
import sernet.hui.server.connect.xml.EntityHomeXML;
import sernet.snutils.DBException;

@WebService(name="EntityHome", serviceName="EntityHomeService")
public class EntityHomeServiceImpl {

	@WebMethod(operationName="findByID")
	public Entity findByID(String id) throws DBException {
		try {
			EntityHomeXML eHome = EntityHomeXML.getInstance();
			eHome.open();
			return eHome.findById(id);
		} catch (XmlException e) {
			Logger.getLogger(EntityHomeServiceImpl.class).error(e);
		}
		return null;
	}
}
