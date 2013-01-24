/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.samt.audit.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CopyLinks extends ChangeLoggingCommand implements IChangeLoggingCommand, IAuthAwareCommand {

    private transient Logger log = Logger.getLogger(CopyLinks.class);
    public Logger getLog() {
        if (log == null) { log = Logger.getLogger(CopyLinks.class); }
        return log;
    }
    
    private List<String> copyUuidList;
    
    private Map<String, String> sourceDestMap;
    
    private CnATreeElement linkTo;
    
    private transient Set<CnALink> cretedLinkSet;
    
    private transient IBaseDao<CnATreeElement, Serializable> cnaTreeElementDao;
    
    private transient IAuthService authService;

    private String stationId;
    
    public CopyLinks(List<String> copyUuidList, Map<String, String> sourceDestMap, CnATreeElement linkTo) {
        super();
        this.copyUuidList = copyUuidList;
        this.sourceDestMap = sourceDestMap;
        this.linkTo = linkTo;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(sourceDestMap!=null) {
            Set<CnALink> allLinks = new HashSet<CnALink>();
            Set<String> uuidSourceSet = sourceDestMap.keySet();
            cretedLinkSet = new HashSet<CnALink>();
            for (String uuidSource : uuidSourceSet) {
                RetrieveInfo ri = new RetrieveInfo().setLinksDown(true).setLinksUp(true);
                CnATreeElement element = getCnaTreeElementDao().findByUuid(uuidSource, ri);
                Set<CnALink> linkDownSet = element.getLinksDown();
                allLinks.addAll(linkDownSet);
                CnATreeElement linkFrom = element;
                for (CnALink cnALink : linkDownSet) {
                    // element is dependant                 
                    CnATreeElement dependency = cnALink.getDependency();
                    createLink(linkFrom, dependency, cnALink.getRelationId());
                }
                Set<CnALink> linkUpSet = element.getLinksUp();
                allLinks.addAll(linkUpSet);
                if(copyUuidList.contains(uuidSource)) {
                    // linkFrom was selected to copy             
                    linkTo = getCnaTreeElementDao().merge(linkTo);
                    // check if parent is already linked
                    if(!parentIsLinked(linkFrom,linkTo)) {                   
                        createLink(linkTo, linkFrom);
                    }
                } else {
                    // linkFrom was copied by recursion
                    for (CnALink cnALink : linkUpSet) {
                        // element is dependency
                        CnATreeElement dependant = cnALink.getDependant(); 
                        createLink(dependant, linkFrom, cnALink.getRelationId()); 
                    }
                }
            }
        }
    }

    /**
     * @param linkFrom
     * @param linkTo2
     * @return
     */
    private boolean parentIsLinked(CnATreeElement linkFrom, CnATreeElement linkTo) {
        boolean result = false;
        if(sourceDestMap.get(linkFrom.getUuid())!=null) {
            String linkFromUuid = sourceDestMap.get(linkFrom.getUuid());
            linkFrom = getCnaTreeElementDao().findByUuid(linkFromUuid,null);
        }
        CnATreeElement parent = linkFrom.getParent();
        if(parent!=null && linkTo!=null) {
            Set<CnALink> linksUpSet = parent.getLinksUp();
            for (CnALink link : linksUpSet) {
                if(linkTo.equals(link.getDependant())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @param uuidSource
     * @param linkFrom
     */
    private void createLink(CnATreeElement dependant, CnATreeElement dependency) {
        Set<HuiRelation> possibleRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(dependency.getEntityType().getId(), dependant.getEntityType().getId());
        if (possibleRelations.isEmpty()) {
            possibleRelations = HitroUtil.getInstance().getTypeFactory().getPossibleRelations(dependant.getEntityType().getId(),dependency.getEntityType().getId());
        }
        String relationId = null;
        if (!possibleRelations.isEmpty()) {
            relationId = possibleRelations.iterator().next().getId();
        }
        createLink(dependant, dependency, relationId); 
    }

    private CnALink createLink(CnATreeElement dependant, CnATreeElement dependency, String relationId) {
        try {
            if(sourceDestMap.get(dependency.getUuid())!=null) {
                String destUuid = sourceDestMap.get(dependency.getUuid());
                dependency = getCnaTreeElementDao().findByUuid(destUuid,null);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Creating link, dependency " + dependency.getTitle() + " (copy)...");
                }
            } else if (getLog().isDebugEnabled()) {
                getLog().debug("Creating link, dependency " + dependency.getTitle() + "...");
            }
            if(sourceDestMap.get(dependant.getUuid())!=null) {
                String destUuid = sourceDestMap.get(dependant.getUuid());
                dependant = getCnaTreeElementDao().findByUuid(destUuid,null);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Creating link, dependant " + dependant.getTitle() + " (copy)...");
                }
            } else if (getLog().isDebugEnabled()) {
                getLog().debug("Creating link, dependant " + dependant.getTitle() + "...");
            } 
            if(!cretedLinkSet.contains(new CnALink(dependant, dependency, relationId, null))) {
                CreateLink<CnALink, CnATreeElement, CnATreeElement> createLink = new CreateLink<CnALink, CnATreeElement, CnATreeElement>(dependant, dependency, relationId, "created by command CopyLinks");      
                createLink = getCommandService().executeCommand(createLink);
                cretedLinkSet.add(createLink.getLink());
                return createLink.getLink();
            } else {
                return null;
            }
            
        } catch (CommandException e) {
            getLog().error("Error while creating cnalink in db", e);
            return null;
        }
        
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        // TODO dm implement IChangeLoggingCommand.getChangedElements()
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }


    
    protected IBaseDao<CnATreeElement, Serializable> getCnaTreeElementDao() {
        if(cnaTreeElementDao==null) {
            cnaTreeElementDao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return cnaTreeElementDao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#getAuthService()
     */
    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#setAuthService
     * (sernet.gs.ui.rcp.main.service.IAuthService)
     */
    @Override
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }
    
}
