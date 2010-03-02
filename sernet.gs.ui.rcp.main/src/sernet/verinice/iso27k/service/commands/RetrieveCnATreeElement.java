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
package sernet.verinice.iso27k.service.commands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.connect.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.iso27k.model.Organization;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class RetrieveCnATreeElement extends GenericCommand {

	private CnATreeElement element;
	
	private RetrieveInfo retrieveInfo;
	
	private Class<? extends CnATreeElement> clazz;

	private Integer dbId;
	
	private RetrieveCnATreeElement(Class<? extends CnATreeElement> clazz, Integer dbId) {
		this.clazz = clazz;
		this.dbId = dbId;
	}
	

	public RetrieveCnATreeElement(Class<? extends CnATreeElement> clazz, Integer dbId, RetrieveInfo retrieveInfo) {
		this.clazz = clazz;
		this.dbId = dbId;
		this.retrieveInfo = retrieveInfo;
	}
	
	
	/**
	 * @param dbId2
	 * @return
	 */
	public static RetrieveCnATreeElement getISO27KModelISMViewInstance(Integer dbId) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(ISO27KModel.class, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setChildren(true).setChildrenProperties(true).setChildrenPermissions(true).setGrandchildren(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	public static RetrieveCnATreeElement getOrganizationISMViewInstance(Integer dbId) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(Organization.class, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setProperties(true).setChildren(true).setChildrenProperties(true).setGrandchildren(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	public static RetrieveCnATreeElement getGroupISMViewInstance(Integer dbId, Class<Group> clazz) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(clazz, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setProperties(true).setPermissions(true).setChildren(true).setChildrenPermissions(true).setChildrenProperties(true).setGrandchildren(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	public static RetrieveCnATreeElement getElementISMViewInstance(Integer dbId, Class<? extends CnATreeElement> clazz) {
		RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(clazz, dbId);
		RetrieveInfo retrieveInfo = new RetrieveInfo();
		retrieveInfo.setProperties(true).setChildren(true);
		retrieveElement.setRetrieveInfo(retrieveInfo);
		return retrieveElement;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(clazz);
		element = dao.retrieve(dbId,getRetrieveInfo());
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

	

}
