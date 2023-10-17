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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final long serialVersionUID = 6532903972493877065L;

    private static final Logger log = Logger.getLogger(CreateMultipleLinks.class);

    private List<Link> linkList;
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    private transient IBaseDao<CnALink, Serializable> linkDao;
    private transient Map<Integer, CnATreeElement> dependantsById;
    private transient Map<Integer, CnATreeElement> dependenciesById;
    private boolean retrieve;
    private List<CnALink> createdLinks;

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

    /*
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        dependantsById = new HashMap<>(linkList.size());
        dependenciesById = new HashMap<>(linkList.size());
        for (Link link : linkList) {
            CnATreeElement dependant = link.getFrom();
            CnATreeElement dependency = link.getTo();
            Integer dependantId = dependant.getDbId();
            Integer dependencyId = dependency.getDbId();
            if (!dependantsById.containsKey(dependantId)) {
                if (retrieve) {
                    RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                    ri.setLinksDown(true);
                    dependant = getDao().retrieve(dependantId, ri);
                }
                dependantsById.put(dependantId, dependant);

            }
            if (!dependenciesById.containsKey(dependencyId)) {
                if (retrieve) {
                    RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                    ri.setLinksUp(true);
                    dependency = getDao().retrieve(dependencyId, ri);
                }
                dependenciesById.put(dependencyId, dependency);
            }
        }

        createdLinks = new ArrayList<>(linkList.size());
        for (Link link : linkList) {
            createdLinks.add(createLink(link));
        }
        linkList = null;
    }

    private CnALink createLink(Link link) {
        try {
            CnATreeElement dependency = dependenciesById.get(link.getTo().getDbId());
            CnATreeElement dependant = dependantsById.get(link.getFrom().getDbId());

            if (log.isDebugEnabled()) {
                log.debug("Creating link from " + dependency.getTypeId() + " to "
                        + dependant.getTypeId());
            }

            CnALink cnaLink = new CnALink(dependant, dependency, link.getRelationId(),
                    link.getComment());

            return getLinkDao().merge(cnaLink, true);
        } catch (RuntimeException e) {
            log.error("RuntimeException while creating link.", e);
            throw e;
        } catch (Exception e) {
            log.error("Error while creating link", e);
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
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    private IBaseDao<CnALink, Serializable> getLinkDao() {
        if (linkDao == null) {
            linkDao = (IBaseDao<CnALink, Serializable>) getDaoFactory().getDAO(CnALink.TYPE_ID);
        }
        return linkDao;
    }

    public List<CnALink> getCreatedLinks() {
        return createdLinks;
    }
}
