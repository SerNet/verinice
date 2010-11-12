/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.audit.rcp;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.iso27k.rcp.IContentCommandFactory;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ElementViewTreeCommandFactory implements IContentCommandFactory {

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.IContentCommandFactory#createCommand(sernet.verinice.model.common.CnATreeElement, boolean)
     */
    @Override
    public RetrieveCnATreeElement createCommand(CnATreeElement el, boolean loadParent) {
        RetrieveCnATreeElement command = null;
        if(el instanceof ISO27KModel) {
            command = ElementViewTreeCommandFactory.getISO27KModelISMViewInstance(el);
        } else if(el instanceof Organization) {
            command = ElementViewTreeCommandFactory.getOrganizationISMViewInstance(el,loadParent);
        } else if( el instanceof IISO27kGroup ) {
            command = ElementViewTreeCommandFactory.getGroupISMViewInstance(el,loadParent);
        } else if( el instanceof CnATreeElement) {
            command = ElementViewTreeCommandFactory.getElementISMViewInstance(el,loadParent);
        }
        return command;
    }
    
    /**
     * @param dbId2
     * @return
     */
    public static RetrieveCnATreeElement getISO27KModelISMViewInstance(CnATreeElement el) {
        RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(ISO27KModel.TYPE_ID, el.getDbId());
        RetrieveInfo retrieveInfo = new RetrieveInfo();
        retrieveInfo.setPermissions(true).setParent(true).setChildren(true).setChildrenProperties(true).setChildrenPermissions(true).setGrandchildren(true);
        retrieveElement.setRetrieveInfo(retrieveInfo);
        return retrieveElement;
    }
    
    public static RetrieveCnATreeElement getOrganizationISMViewInstance(CnATreeElement el, boolean loadParent) {
        RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(Organization.TYPE_ID, el.getDbId());
        RetrieveInfo retrieveInfo = new RetrieveInfo();
        retrieveInfo.setPermissions(true).setProperties(true).setParent(true).setChildren(true).setChildrenProperties(true).setGrandchildren(true);
        if(loadParent) {
            retrieveInfo.setParent(true).setParentPermissions(true).setSiblings(true);
        }
        retrieveElement.setRetrieveInfo(retrieveInfo);
        return retrieveElement;
    }
    
    public static RetrieveCnATreeElement getGroupISMViewInstance(CnATreeElement el, boolean loadParent) {
        RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(el.getTypeId(), el.getDbId());
        RetrieveInfo retrieveInfo = new RetrieveInfo();
        retrieveInfo.setProperties(true).setPermissions(true).setParent(true).setChildren(true).setChildrenPermissions(true).setChildrenProperties(true).setGrandchildren(true);
        if(loadParent) {
            retrieveInfo.setParent(true).setParentPermissions(true).setSiblings(true);
        }
        retrieveElement.setRetrieveInfo(retrieveInfo);
        return retrieveElement;
    }
    
    public static RetrieveCnATreeElement getElementISMViewInstance(CnATreeElement el, boolean loadParent) {
        RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(el.getTypeId(), el.getDbId());
        RetrieveInfo retrieveInfo = new RetrieveInfo();
        retrieveInfo.setPermissions(true).setProperties(true).setParent(true).setChildren(true);
        if(loadParent) {
            retrieveInfo.setParent(true).setParentPermissions(true).setSiblings(true);
        }
        retrieveElement.setRetrieveInfo(retrieveInfo);
        return retrieveElement;
    }

}
