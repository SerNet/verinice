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
package sernet.verinice.iso27k.service.commands;

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
@SuppressWarnings("restriction")
public class RetrieveCnATreeElement extends GenericCommand {
    
	private CnATreeElement element;
	
	private RetrieveInfo retrieveInfo;

	private Integer dbId;

    private String typeId;
    
    private Map<String, Object> parameter;
	
    public RetrieveCnATreeElement(String typeId, Integer dbId) {
	    this.typeId = typeId;
		this.dbId = dbId;
	}
	
	public RetrieveCnATreeElement(String typeId, Integer dbId, RetrieveInfo retrieveInfo) {
	    this.typeId = typeId;
		this.dbId = dbId;
		this.retrieveInfo = retrieveInfo;
	}
	
	public RetrieveCnATreeElement(String typeId, Integer dbId, RetrieveInfo retrieveInfo, Map<String, Object> parameter) {
        this.typeId = typeId;
        this.dbId = dbId;
        this.retrieveInfo = retrieveInfo;
        this.parameter = parameter;
    }
	
	
	/**
	 * @param dbId2
	 * @return
	 */
	public static RetrieveCnATreeElement getISO27KModelISMViewInstance(Integer dbId) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(ISO27KModel.TYPE_ID, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setPermissions(true).setChildren(true).setChildrenProperties(true).setChildrenPermissions(true).setGrandchildren(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	public static RetrieveCnATreeElement getOrganizationISMViewInstance(Integer dbId) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(Organization.TYPE_ID, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setPermissions(true).setProperties(true).setChildren(true).setChildrenPermissions(true).setChildrenProperties(true).setGrandchildren(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	public static RetrieveCnATreeElement getGroupISMViewInstance(Integer dbId, String typeId) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(typeId, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setProperties(true).setPermissions(true).setChildren(true).setChildrenPermissions(true).setChildrenProperties(true).setGrandchildren(true).setParent(true).setSiblings(true);
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
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
		element = dao.retrieve(dbId,getRetrieveInfo());
		ElementFilter.applyParameter(element, parameter);
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

    /**
     * @return the parameter
     */
    public Map<String, Object> getParameter() {
        return parameter;
    }
    
    /**
     * @param parameter the parameter to set
     */
    public void setParameter(Map<String, Object> parameter) {
        this.parameter = parameter;
    }
 
}
