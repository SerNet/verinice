/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class Retriever {

	private static final Logger LOG = Logger.getLogger(Retriever.class);
	
	private static ICommandService commandService;
	
	public static CnATreeElement checkRetrieveElement(CnATreeElement element) {
		if(!isElementInitialized(element)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading properties of element: " + element.getDbId());
			}
			element = retrieveElement(element,RetrieveInfo.getPropertyInstance());
		}
		return element;
	}
	
	public static CnATreeElement checkRetrieveChildren(CnATreeElement element) {
		if(!areChildrenInitialized(element)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading children of element: " + element.getDbId());
			}
			element = retrieveElement(element,RetrieveInfo.getChildrenInstance());
		}
		return element;
	}
	
	public static CnATreeElement checkRetrievePermissions(CnATreeElement element) {
        if(!arePermissionsInitialized(element)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Loading permissions of element: " + element.getDbId());
            }
            RetrieveInfo ri = new RetrieveInfo();
            ri.setPermissions(true);
            element = retrieveElement(element,ri);
        }
        return element;
    }
	
	public static CnATreeElement checkRetrieveElementAndChildren(final CnATreeElement element) {
		RetrieveInfo ri = null;
		CnATreeElement returnValue;
		if(!isElementInitialized(element)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading children of element: " + element.getDbId());
			}
			if(ri==null) {
				ri = new RetrieveInfo();
			}
			ri.setProperties(true);
		}
		if(!areChildrenInitialized(element)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading properties of element: " + element.getDbId());
			}
			if(ri==null) {
				ri = new RetrieveInfo();
			}
			ri.setChildren(true);
		}
		if(ri!=null) {
		    returnValue = retrieveElement(element,ri);
		} else {
		    returnValue = element;
		}
		return returnValue;
	}
	
	/**
     * @param cte
     */
    public static CnATreeElement checkRetrieveParent(CnATreeElement element) {
        if(!isParentInitialized(element) && element!=null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Loading parent of element: " + element.getDbId());
            }
            RetrieveInfo ri = new RetrieveInfo();   
            ri.setParent(true);
            element = retrieveElement(element,ri);
        }
        return element;
    }

    public static CnATreeElement checkRetrieveLinks(CnATreeElement element, boolean upLinks){
        if(!areLinksInitizialized(element, upLinks) && element != null){
            if(LOG.isInfoEnabled()){
                LOG.info("Loading links of element: " + element.getDbId());
            }
            RetrieveInfo ri = new RetrieveInfo();
            ri.setLinksDown(true).setLinksUp(true);
            element = retrieveElement(element, ri);
        }
        return element;
    }
    
    public static boolean isParentInitialized(CnATreeElement element) {
        return Hibernate.isInitialized(element) 
            && (element==null || Hibernate.isInitialized(element.getParent())); 

    }
	
	private static boolean isElementInitialized(CnATreeElement element) {
        if(element==null) {
            return true;
        }
        if(!Hibernate.isInitialized(element)) {
            return false;
        }
        return isEntityInitialized(element.getEntity());
	}
	
    private static boolean isEntityInitialized(Entity entity) {
        if(entity==null) {
            return true;
        }
        if(!Hibernate.isInitialized(entity)) {
            return false;
        }
        return isPropertyListInitialize(entity.getTypedPropertyLists());
    }

    private static boolean isPropertyListInitialize(Map<String, PropertyList> typedPropertyLists) {
        if(typedPropertyLists==null) {
            return true;
        }
        if(!Hibernate.isInitialized(typedPropertyLists)) {
            return false;
        }
        for (PropertyList properties : typedPropertyLists.values()) {
            if(!isPropertiesInitialized(properties)) {
                return false;
            }
        }
        return true;
    }


    private static boolean isPropertiesInitialized(PropertyList properties) {
        if(properties==null) {
            return true;
        }
        if(!Hibernate.isInitialized(properties)) {
            return false;
        }
        if(properties.getProperties()==null) {
            return true;
        }
        if(!Hibernate.isInitialized(properties.getProperties())) {
            return false;
        }
        return true;
    }

    public static boolean areChildrenInitialized(CnATreeElement element) {
        return Hibernate.isInitialized(element) 
            && (element==null || Hibernate.isInitialized(element.getChildren()));
    }
	
	public static boolean arePermissionsInitialized(CnATreeElement element) {
        return Hibernate.isInitialized(element) 
            && (element==null || Hibernate.isInitialized(element.getPermissions()));
    }
	
	public static CnATreeElement retrieveElement(final CnATreeElement element, RetrieveInfo ri)  {
		RetrieveCnATreeElement retrieveCommand = new RetrieveCnATreeElement(
				element.getTypeId(), 
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
	
	public static boolean areLinksInitizialized(CnATreeElement element, boolean upLinks){
	    boolean elementIni = Hibernate.isInitialized(element);
	    boolean links = false;
	    if(elementIni){
	        links = Hibernate.isInitialized(element.getLinksDown());
	        if(upLinks){
	            links = links && Hibernate.isInitialized(element.getLinksUp());
	        }
	    }
	    return elementIni && (element == null || links);
	}
	
	
}
