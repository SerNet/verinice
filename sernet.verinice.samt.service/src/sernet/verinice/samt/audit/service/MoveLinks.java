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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
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
public class MoveLinks extends GenericCommand implements IChangeLoggingCommand, IAuthAwareCommand {

    private transient Logger log = Logger.getLogger(MoveLinks.class);
    public Logger getLog() {
        if (log == null) { log = Logger.getLogger(MoveLinks.class); }
        return log;
    }
    
    Map<String, String> sourceDestMap;
    
    CnATreeElement linkTo;
    
    private transient IBaseDao<CnATreeElement, Serializable> cnaTreeElementDao;
    
    private transient IAuthService authService;

    private String stationId;

    
    
    public MoveLinks(Map<String, String> sourceDestMap, CnATreeElement linkTo) {
        super();
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
            Collection<String> uuidDestCollection = sourceDestMap.values();
            // find the old link up and remove it
            for (String uuidSource : uuidDestCollection) {
                RetrieveInfo ri = new RetrieveInfo().setLinksUp(true).setParent(true);
                CnATreeElement element = getCnaTreeElementDao().findByUuid(uuidSource, ri);
                Set<CnALink> linkUpSet = element.getLinksUp();
                Set<CnALink> linkDeleteSet = new HashSet<CnALink>();
                for (CnALink link : linkUpSet) {
                    if(linkTo.getTypeId().equals(link.getDependant().getTypeId())) {
                        linkDeleteSet.add(link);
                    }
                }
                for (CnALink link : linkDeleteSet) {
                    element.getLinksUp().remove(link);                 
                }
                element = getCnaTreeElementDao().merge(element);
                linkTo = getCnaTreeElementDao().merge(linkTo);
                if(!parentIsLinked(element,linkTo)) {
                    createLink(linkTo, element);
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
            CreateLink<CnALink, CnATreeElement, CnATreeElement> createLink = new CreateLink<CnALink, CnATreeElement, CnATreeElement>(dependant, dependency, relationId, "created by command CopyLinks");      
            createLink = getCommandService().executeCommand(createLink);
            return createLink.getLink();          
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
        return ChangeLogEntry.TYPE_UPDATE;
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
