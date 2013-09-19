/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.service.commands;

import java.io.Serializable;

import org.jbpm.pvm.internal.cmd.GetResourceAsStreamCmd;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class LoadAncestors extends LoadElementByUuid {

    
    private transient IBaseDao<CnATreeElement, Serializable> elementDao;

    /**
     * @param typeId
     * @param uuid
     * @param ri
     */
    public LoadAncestors(String typeId, String uuid, RetrieveInfo ri) {
        super(typeId, uuid, ri);
        ri.setParent(true);
    }

    /**
     * @param uuid
     * @param ri
     */
    public LoadAncestors(String uuid, RetrieveInfo ri) {
        super(uuid, ri);
        ri.setParent(true);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.commands.LoadElementByUuid#execute()
     */
    @Override
    public void execute() {
        super.execute();
        if(getElement()!=null) {
            CnATreeElement elementWithParent = loadParent(getElement());
            element.setParent(elementWithParent.getParent());
        }
    }

    /**
     * @param parent
     */
    private CnATreeElement loadParent(CnATreeElement child) {
        Integer parentId = child.getParentId();
        if(parentId!=null) {
            RetrieveInfo ri = new RetrieveInfo();
            ri.setParent(true);
            CnATreeElement parent = (CnATreeElement) getElementDao().retrieve(parentId, ri);    
            if(parent!=null) {
                parent = loadParent(parent);
                child.setParent(parent);
            }        
        }
        return child;
    }
    
    /**
     * @return the elementDao
     */
    public IBaseDao<CnATreeElement, Serializable> getElementDao() {
        if(elementDao==null) {
            elementDao = (IBaseDao<CnATreeElement, Serializable>) getDaoFactory().getDAO(CnATreeElement.class);      
        }
        return elementDao;
    }

}
