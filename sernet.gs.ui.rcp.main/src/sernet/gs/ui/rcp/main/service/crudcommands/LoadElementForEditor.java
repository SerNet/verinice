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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

public class LoadElementForEditor<T extends CnATreeElement> extends GenericCommand {

	private transient Logger log = Logger.getLogger(LoadElementForEditor.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadElementForEditor.class);
		}
		return log;
	}
	
	private T element;
	private boolean retrieveChildren;
	private Integer dbId;
    private String typeId;

	public LoadElementForEditor(T element, boolean retrieveChildren) {
		// slim down for transfer:
		dbId = element.getDbId();
		typeId = element.getTypeId();
		this.retrieveChildren = retrieveChildren;
	}
	
	public LoadElementForEditor(T element) {
		this(element, false);
	}
	
	public void execute() {
		if (getLog().isDebugEnabled()) {
			getLog().debug("execute, dbId: " + dbId);
		}
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
		RetrieveInfo ri = new RetrieveInfo();
		ri.setLinksDown(true).setLinksUp(true);
		element = (T) dao.retrieve(dbId, ri);
		HydratorUtil.hydrateElement(dao, element, retrieveChildren);
		Set<CnALink> linksDown = element.getLinksDown();
		for (CnALink cnALink : linksDown) {
			HydratorUtil.hydrateElement(dao, cnALink.getDependency(), false);
			
		}
		Set<CnALink> linksUp = element.getLinksUp();
		for (CnALink cnALink : linksUp) {
			HydratorUtil.hydrateElement(dao, cnALink.getDependant(), false);
			
		}
	}


	public T getElement() {
		return element;
	}

}
