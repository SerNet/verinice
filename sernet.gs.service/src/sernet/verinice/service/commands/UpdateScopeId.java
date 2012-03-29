/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.interfaces.INoAccessControl;

/**
 * This command set the scope-id of CnaTreeElements.
 * 
 * You can use this in two different ways:
 * <ul>
 *  <li>
 *    If you want to set all scope-ids in your database, use the 
 *    constructor without params.
 *  </li>
 *  <li>
 *    If you want to set all scope-ids of an subtree, use the 
 *    constructor with params {@link Integer} and {@link Integer}.
 *  </li>
 * </ul>
 * 
 * Command is used for update from Db-Version 0.98 to 0.99
 * and while moving elements from one scope to another.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class UpdateScopeId extends GenericCommand implements INoAccessControl {

    private transient Logger log = Logger.getLogger(UpdateScopeId.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(UpdateScopeId.class);
        }
        return log;
    }
      
    private Integer elementId;
    
    private Integer scopeId;

    private transient IBaseDao<Organization, Serializable> orgDao;
    
    private transient IBaseDao<ITVerbund, Serializable> itverbundDao;
    
    private transient IBaseDao<CnATreeElement, Serializable> cnaTreeElementDao;
    
      
    /**
     * Use this constructor, if you want to set all scope-ids in the database
     */
    public UpdateScopeId() {
        super();
    }
    
    /**
     * Use this constructor, if you want to set the scope-id of <code>element</code>
     * and all elements in it's subtree.
     * 
     * @param elementId Id of a subtree root. All scope-id in this subtree will be set.
     * @param scopeId A scope-id (Db-Id of an organization or IT-Verbund)
     */
    public UpdateScopeId(Integer elementId, Integer scopeId) {
        super();
        if(elementId==null || scopeId==null) {
            throw new IllegalArgumentException("Param elementId or scopeId is null. Please pass an element id and a scope id.");
        }
        this.elementId = elementId;
        this.scopeId = scopeId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(getElementId()==null) {
            updateAllElements();
        } else {
            updateSubtree();
        }
    }

    
    /**
     * Sets the scope-id of all elements the database.
     */
    private void updateAllElements() {
        for (Organization org : getOrgDao().findAll()) {
            update(org,org.getDbId());            
        }
        for (ITVerbund itverbund : getItverbundDao().findAll()) {
            update(itverbund,itverbund.getDbId());            
        }
    }
    
    /**
     * Set the scope-id of a subtree
     */
    private void updateSubtree() {
        CnATreeElement element = getCnaTreeElementDao().findById(getElementId());
        update(element, getScopeId());
    }

    private void update(CnATreeElement element, Integer scopeId) {
        element.setScopeId(scopeId);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Updating element: " + element.getTypeId() + " (" + element.getUuid() + ") to scope: " + scopeId);
        }
        for (CnATreeElement child : element.getChildren()) {
            update(child, scopeId);
        }
    }

   
    /**
     * @return the elementId
     */
    public Integer getElementId() {
        return elementId;
    }


    /**
     * @return the scopeId
     */
    public Integer getScopeId() {
        return scopeId;
    }


    public IBaseDao<Organization, Serializable> getOrgDao() {
        if(orgDao==null) {
            orgDao = getDaoFactory().getDAO(Organization.TYPE_ID);
        }
        return orgDao;
    }
    
    public IBaseDao<ITVerbund, Serializable> getItverbundDao() {
        if(itverbundDao==null) {
            itverbundDao = getDaoFactory().getDAO(ITVerbund.TYPE_ID);
        }
        return itverbundDao;
    }

    public IBaseDao<CnATreeElement, Serializable> getCnaTreeElementDao() {
        if(cnaTreeElementDao==null) {
            cnaTreeElementDao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return cnaTreeElementDao;
    }


}
