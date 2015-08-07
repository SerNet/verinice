/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Link;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CreateMultipleLinks extends GenericCommand {

    private transient Logger log = Logger.getLogger(CreateMultipleLinks.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CreateMultipleLinks.class);
        }
        return log;
    }
    
    private List<Link> linkList;
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    private transient IBaseDao<CnALink, Serializable> linkDao;
    private boolean retrieve;
    
    public CreateMultipleLinks(List<Link> linkList) {
        super();
        this.linkList = linkList;
        this.retrieve = false;
    }
    
    public CreateMultipleLinks(List<Link> linkList, boolean retrieve) {
        super();
        this.linkList = linkList;
        this.retrieve = retrieve;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        for (Link link : linkList) {
            createLink(link);
        }
        linkList = null;
    }
    
    private void createLink(Link link) {
        try {
            CnATreeElement dependency = link.getTo();            
            if(retrieve) {
                RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                ri.setLinksUp(true);      
                dependency = getDao().findByUuid(dependency.getUuid(), ri);  
            }
            
            CnATreeElement dependant = link.getFrom();
            if(retrieve) {
                RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                ri.setLinksDown(true);  
                dependant = getDao().findByUuid(dependant.getUuid(), ri);
            }

            if (getLog().isDebugEnabled()) {
                getLog().debug("Creating link from " + dependency.getTypeId() + " to " + dependant.getTypeId());
            }
            
            CnALink cnaLink = new CnALink(dependant, dependency, link.getRelationId(), link.getComment());

            getLinkDao().merge(cnaLink, true);
        } catch (RuntimeException e) {
            getLog().error("RuntimeException while creating link.", e);
            throw e;
        } catch (Exception e) {
            getLog().error("Error while creating link", e);
            throw new RuntimeException("Error while creating link", e);
        }
    }
    
    public boolean isRetrieve() {
        return retrieve;
    }

    public void setRetrieve(boolean retrieve) {
        this.retrieve = retrieve;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if (dao == null) {
            dao = (IBaseDao<CnATreeElement, Serializable>) getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }
    
    private IBaseDao<CnALink, Serializable> getLinkDao() {
        if (linkDao == null) {
            linkDao = (IBaseDao<CnALink, Serializable>) getDaoFactory().getDAO(CnALink.TYPE_ID);
        }
        return linkDao;
    }

}
