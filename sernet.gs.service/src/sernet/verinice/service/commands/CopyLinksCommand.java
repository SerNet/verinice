/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command creates links for copied elements.
 * Copies of elements are passed in a map. Key of the map
 * is the UUID of the source element, values is the UUID of the copy.
 * 
 * All links from the source elements are copied the following way:
 * 
 * If the link destination element was copied together with the source a new link is created
 * from the source copy to the destination copy.
 * 
 * If the link destination element was not copied a new link is created
 * from the source copy to the original destination.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyLinksCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(CopyLinksCommand.class);
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CopyLinksCommand.class);
        }
        return log;
    }
    
    private static final int FLUSH_LEVEL = 20;
    private int number = 0;
    
    private static final String UP = "up";
    private static final String DOWN = "down";
    
    private transient Map<String, String> sourceDestMap;
    
    private transient Map<String, List<String[]>> existingUpLinkMap;
    private transient Map<String, List<String[]>> existingDownLinkMap;
    
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    
    /**
     * @param sourceDestMap
     */
    public CopyLinksCommand(Map<String, String> sourceDestMap) {
        super();
        this.sourceDestMap = sourceDestMap;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        loadAndCacheLinks();
        copyLinks();
    }

    public void copyLinks() {
        number = 0;
        Set<String> sourceUuids = sourceDestMap.keySet();
        for (String sourceUuid : sourceUuids) {
            createLinks(sourceDestMap.get(sourceUuid), existingUpLinkMap.get(sourceUuid), UP);
            createLinks(sourceDestMap.get(sourceUuid), existingDownLinkMap.get(sourceUuid), DOWN);
        }
    }

    private void createLinks(String sourceUuid, List<String[]> destinations, String direction) {
        if(destinations==null) {
            return;
        }
        for (String[] destAndType : destinations) {
            String uuid = destAndType[0];
            String copyDestUuid = sourceDestMap.get(uuid);
            if(copyDestUuid!=null) {
                uuid = copyDestUuid;
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Creating link to copy of target... " + sourceUuid + " -> " + uuid);
                }       
            } else if (getLog().isDebugEnabled()) {
                getLog().debug("Creating link to same target... " + sourceUuid + " -> " + uuid);
            } 
            if(UP.equals(direction)) {
                createLink(sourceUuid, uuid, destAndType[1]);
            } else {
                createLink(uuid, sourceUuid, destAndType[1]);
            }
            number++;
            if(number % FLUSH_LEVEL == 0 ) {
                flushAndClear();
            }
        }
    }

    private void flushAndClear() {
        IBaseDao<CnALink, Serializable> linkDao = getDaoFactory().getDAO(CnALink.class);
        linkDao.flush();
        linkDao.clear();
        getDao().flush();
        getDao().clear();      
    }

    private void createLink(String sourceUuid, String destUuid, String type) {
        CreateLink<CnATreeElement, CnATreeElement>  createLink = new CreateLink<>(sourceUuid, destUuid, type);
        try {
            getCommandService().executeCommand(createLink);
        } catch (CommandException e) {
            getLog().error("Error while creating link for copy", e);
            throw new RuntimeCommandException(e);
        }
    }

    public void loadAndCacheLinks() {
        String hql = "select l.dependant.uuid,l.dependency.uuid,l.id.typeId from sernet.verinice.model.common.CnALink l";
        List<Object[]> allLinkedUuids = getDao().findByQuery(hql, null);
        existingUpLinkMap = new HashMap<String, List<String[]>>();
        existingDownLinkMap = new HashMap<String, List<String[]>>();
        for (Object[] sourceAndDest : allLinkedUuids) {
            cacheLink(sourceAndDest);
        }
    }
    
    private void cacheLink(Object[] sourceAndDest) {
        cacheLink((String)sourceAndDest[0], (String)sourceAndDest[1], (String)sourceAndDest[2], existingUpLinkMap);
        cacheLink((String)sourceAndDest[1], (String)sourceAndDest[0], (String)sourceAndDest[2], existingDownLinkMap);
    }

    public void cacheLink(String source, String dest, String type, Map<String, List<String[]>> map) {
        List<String[]> destinations = map.get(source);
        if(destinations==null) {
            destinations = new LinkedList<String[]>();          
            map.put(source, destinations);
        }
        destinations.add(new String[]{dest,type});
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

}
