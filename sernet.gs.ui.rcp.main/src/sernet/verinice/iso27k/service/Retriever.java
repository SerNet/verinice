/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class Retriever {

	private static final Logger LOG = Logger.getLogger(Retriever.class);
	
	private static ICommandService commandService;
	
	public static CnATreeElement checkRetrieveElement(CnATreeElement element) {
		try {
			checkElement(element);
		} catch(LazyInitializationException e) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading properties of element: " + element.getDbId());
			}
			element = retrieveElement(element,RetrieveInfo.getPropertyInstance());
		}
		return element;
	}
	
	public static CnATreeElement checkRetrieveChildren(CnATreeElement element) {
		try {
			checkChildren(element);
		} catch(LazyInitializationException e) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading children of element: " + element.getDbId());
			}
			element = retrieveElement(element,RetrieveInfo.getChildrenInstance());
		}
		return element;
	}
	
	public static CnATreeElement checkRetrieveElementAndChildren(CnATreeElement element) {
		RetrieveInfo ri = null;
		try {
			checkElement(element);
		} catch(LazyInitializationException e) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading children of element: " + element.getDbId());
			}
			if(ri==null) {
				ri = new RetrieveInfo();
			}
			ri.setProperties(true);
		}
		try {
			checkChildren(element);
		} catch(LazyInitializationException e) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading properties of element: " + element.getDbId());
			}
			if(ri==null) {
				ri = new RetrieveInfo();
			}
			ri.setChildren(true);
		}
		if(ri!=null) {
			element = retrieveElement(element,ri);
		}
		return element;
	}
	
	private static void checkElement(CnATreeElement element) {
		if(element.getEntity()!=null
		   && element.getEntity().getTypedPropertyLists()!=null
		   && !element.getEntity().getTypedPropertyLists().isEmpty()) {
			Map<String, PropertyList> map = element.getEntity().getTypedPropertyLists();
			String key = map.keySet().iterator().next();
			PropertyList propertyList = map.get(key);
			propertyList.getProperties();
		}
	}
	
	private static void checkChildren(CnATreeElement element) {
		if(element.getChildren()!=null) {
			element.getChildren().iterator();
		}
	}
	
	public static CnATreeElement retrieveElement(CnATreeElement element, RetrieveInfo ri)  {
		RetrieveCnATreeElement retrieveCommand = new RetrieveCnATreeElement(
				element.getClass(), 
				element.getDbId(),
				ri);
		try {
			retrieveCommand = getCommandService().executeCommand(retrieveCommand);
		} catch (CommandException e1) {
			LOG.error("Error while retrieving element", e1);
			throw new RuntimeException("Error while retrieving element", e1);
		}
		return retrieveCommand.getElement();
	}
	
	private static ICommandService getCommandService() {
		if (commandService == null) {
			commandService = createCommandServive();
		}
		return commandService;
	}

	private static ICommandService createCommandServive() {
		return ServiceFactory.lookupCommandService();
	}
	
}
