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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class UpdateScopeId extends GenericCommand {

    private transient Logger log = Logger.getLogger(UpdateScopeId.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(UpdateScopeId.class);
        }
        return log;
    }
    
    IBaseDao<Organization, Serializable> orgDao;
    
    IBaseDao<ITVerbund, Serializable> itverbundDao;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        for (Organization org : getOrgDao().findAll()) {
            update(org,org.getDbId());            
        }
        for (ITVerbund itverbund : getItverbundDao().findAll()) {
            update(itverbund,itverbund.getDbId());            
        }
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

}
