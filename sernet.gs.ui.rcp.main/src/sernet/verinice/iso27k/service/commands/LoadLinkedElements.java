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
package sernet.verinice.iso27k.service.commands;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class LoadLinkedElements extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadLinkedElements.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadLinkedElements.class);
        }
        return log;
    }
    
    private List<String> typeIdList;
	private List<CnATreeElement> elementList;
	private int selectedId;

	public LoadLinkedElements(List<String> typeIdList,int selectedId) {
	    this.typeIdList = typeIdList;
	    this.selectedId = selectedId;
	}
	
	public void execute() {
		StringBuffer sb = new StringBuffer();
		sb.append("select distinct element from ").append(CnATreeElement.class.getName()).append(" as element ");
		sb.append("left outer join element.linksDown as linksDown ");
		sb.append("left outer join element.linksUp as linksUp ");
		sb.append("where linksDown.id.dependencyId = ? ");
        sb.append("or linksUp.id.dependantId = ? ");
		final String hql = sb.toString();
		if(getLog().isDebugEnabled()) {
		    getLog().debug("hql: " + hql);
		}
		elementList = new Vector<CnATreeElement>();
		List<CnATreeElement> resultList = getDaoFactory().getDAO(CnATreeElement.class).findByQuery(hql,new Object[]{selectedId,selectedId});
		for (Iterator<CnATreeElement> iterator = resultList.iterator(); iterator.hasNext();) {
		    CnATreeElement element =  iterator.next();
		    if(typeIdList.contains(element.getTypeId())) {
		        elementList.add(element);
		    }
            
        }
	}


	public List<CnATreeElement> getElementList() {
		return elementList;
	}
	
}
