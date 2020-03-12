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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.Map;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementFilter;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class RetrieveCnATreeElement extends GenericCommand {

    private static final long serialVersionUID = -2402504404366837672L;

    private CnATreeElement element;

    private RetrieveInfo retrieveInfo;

    private Integer dbId;

    private String typeId;

    private transient Map<String, Object> parameter;

    public RetrieveCnATreeElement(String typeId, Integer dbId) {
        this(typeId, dbId, null, null);
    }

    public RetrieveCnATreeElement(String typeId, Integer dbId, RetrieveInfo retrieveInfo) {
        this(typeId, dbId, retrieveInfo, null);
    }

    public RetrieveCnATreeElement(String typeId, Integer dbId, RetrieveInfo retrieveInfo,
            Map<String, Object> parameter) {
        this.typeId = typeId;
        this.dbId = dbId;
        this.retrieveInfo = retrieveInfo;
        this.parameter = parameter;
    }

    public static RetrieveCnATreeElement getISO27KModelISMViewInstance(Integer dbId) {
        RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(ISO27KModel.TYPE_ID,
                dbId);
        RetrieveInfo retrieveInfo = new RetrieveInfo();
        retrieveInfo.setPermissions(true).setChildren(true).setChildrenProperties(true)
                .setChildrenPermissions(true).setGrandchildren(true);
        retrieveElement.setRetrieveInfo(retrieveInfo);
        return retrieveElement;
    }

    public static RetrieveCnATreeElement getOrganizationISMViewInstance(Integer dbId) {
        RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(Organization.TYPE_ID,
                dbId);
        RetrieveInfo retrieveInfo = new RetrieveInfo();
        retrieveInfo.setPermissions(true).setProperties(true).setChildren(true)
                .setChildrenPermissions(true).setChildrenProperties(true).setGrandchildren(true);
        retrieveElement.setRetrieveInfo(retrieveInfo);
        return retrieveElement;
    }

    public static RetrieveCnATreeElement getGroupISMViewInstance(Integer dbId, String typeId) {
        RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(typeId, dbId);
        RetrieveInfo retrieveInfo = new RetrieveInfo();
        retrieveInfo.setProperties(true).setPermissions(true).setChildren(true)
                .setChildrenPermissions(true).setChildrenProperties(true).setGrandchildren(true)
                .setParent(true).setSiblings(true);
        retrieveElement.setRetrieveInfo(retrieveInfo);
        return retrieveElement;
    }

    public static RetrieveCnATreeElement getElementISMViewInstance(Integer dbId, String typeId) {
        RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(typeId, dbId);
        RetrieveInfo retrieveInfo = new RetrieveInfo();
        retrieveInfo.setPermissions(true).setProperties(true).setChildren(true);
        retrieveElement.setRetrieveInfo(retrieveInfo);
        return retrieveElement;
    }

    /*
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    public void execute() {
        @SuppressWarnings("unchecked")
        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
        element = dao.retrieve(dbId, getRetrieveInfo());
        ElementFilter.filterChildrenOfElement(element, parameter);
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public CnATreeElement getElement() {
        return element;
    }

    public void setRetrieveInfo(RetrieveInfo retrieveInfo) {
        this.retrieveInfo = retrieveInfo;
    }

    public RetrieveInfo getRetrieveInfo() {
        return retrieveInfo;
    }

    public Map<String, Object> getParameter() {
        return parameter;
    }

    public void setParameter(Map<String, Object> parameter) {
        this.parameter = parameter;
    }

}
