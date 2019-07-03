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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command creates links for copied elements. Copies of elements are passed
 * in a map. Key of the map is the UUID of the source element, values is the
 * UUID of the copy.
 * 
 * All links from the source elements are copied the following way:
 * 
 * If the link destination element was copied together with the source a new
 * link is created from the source copy to the destination copy.
 * 
 * If the link destination element was not copied a new link is created from the
 * source copy to the original destination.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyLinksCommand extends GenericCommand {

    private static final Logger logger = Logger.getLogger(CopyLinksCommand.class);

    private static final int FLUSH_LEVEL = 20;
    private int number = 0;

    private transient Map<String, String> sourceDestMap;

    private transient Map<String, List<LinkInformation>> existingLinksByCopiedElementUUID;

    private transient IBaseDao<CnATreeElement, Serializable> dao;

    private final CopyLinksMode copyLinksMode;

    public CopyLinksCommand(Map<String, String> sourceDestMap, CopyLinksMode copyLinksMode) {
        super();
        this.sourceDestMap = sourceDestMap;
        this.copyLinksMode = copyLinksMode;
    }

    /*
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if (copyLinksMode == CopyLinksMode.NONE) {
            return;
        }
        loadAndCacheLinks();
        copyLinks();
    }

    public void copyLinks() {
        number = 0;
        for (Entry<String, String> e : sourceDestMap.entrySet()) {
            String sourceUuid = e.getKey();
            String targetUuid = e.getValue();
            createLinks(targetUuid, existingLinksByCopiedElementUUID.get(sourceUuid));
        }
    }

    private void createLinks(String copyTargetUUID, List<LinkInformation> linkInformations) {
        if (linkInformations == null) {
            return;
        }
        for (LinkInformation linkInformation : linkInformations) {
            processLink(linkInformation, copyTargetUUID);
            number++;
            if (number % FLUSH_LEVEL == 0) {
                flushAndClear();
            }
        }
        flushAndClear();
    }

    /**
     * Process a single link that a copied element is part of. Depending on the
     * {@link CopyLinksMode copy mode} and whether the element on the other side
     * of the link was copied too, this will either ignore the existing link or
     * create a new link from the copied element. For example, when copying
     * elements from the compendium, this will not copy links that would point
     * into the compendium from the copied element.
     * 
     * 
     * @param linkInformation
     *            information about the processed link
     * @param copyTargetUUID
     *            the uuid of the element that one of the linked entities was
     *            copied to
     */
    private void processLink(LinkInformation linkInformation, String copyTargetUUID) {
        String otherElementUUID = linkInformation.otherElementUUID;
        String copyDestinationUuid = sourceDestMap.get(otherElementUUID);
        if (copyDestinationUuid == null) {
            // the element on the other side of the link was not copied
            if (copyLinksMode == CopyLinksMode.FROM_COMPENDIUM_TO_MODEL) {
                // we don't want to copy the link if we copy from the compendium
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping link to " + otherElementUUID
                            + " while copying from compendium");
                }
                return;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating link to original target... " + copyTargetUUID + " -> "
                            + otherElementUUID);
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating link to copy of target... " + copyTargetUUID + " -> "
                        + otherElementUUID);
            }
            otherElementUUID = copyDestinationUuid;
        }

        if (linkInformation.direction == Direction.FROM_COPIED_ELEMENT) {
            createLink(copyTargetUUID, otherElementUUID, linkInformation.type,
                    linkInformation.comment);
        } else {
            createLink(otherElementUUID, copyTargetUUID, linkInformation.type,
                    linkInformation.comment);
        }
    }

    private void flushAndClear() {
        IBaseDao<CnALink, Serializable> linkDao = getDaoFactory().getDAO(CnALink.class);
        linkDao.flush();
        linkDao.clear();
        getDao().flush();
        getDao().clear();
    }

    private void createLink(String sourceUuid, String destUuid, String type, String comment) {
        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<>(sourceUuid,
                destUuid, type, comment);
        try {
            getCommandService().executeCommand(createLink);
        } catch (CommandException e) {
            logger.error("Error while creating link for copy", e);
            throw new RuntimeCommandException(e);
        }
    }

    public void loadAndCacheLinks() {
        final Set<String> copiedElementUUIDs = sourceDestMap.keySet();
        List<Object[]> allLinkedUuids = getDao()
                .findByCallback(new FindLinksForElements(copiedElementUUIDs));

        existingLinksByCopiedElementUUID = new HashMap<>(copiedElementUUIDs.size());
        for (Object[] entry : allLinkedUuids) {
            String dependantUUID = (String) entry[0];
            String dependencyUUID = (String) entry[1];
            String typeId = (String) entry[2];
            String comment = (String) entry[3];
            if (copiedElementUUIDs.contains(dependantUUID)) {
                cacheLink(dependantUUID, dependencyUUID, typeId, Direction.FROM_COPIED_ELEMENT,
                        comment);
            } else {
                cacheLink(dependencyUUID, dependantUUID, typeId, Direction.TO_COPIED_ELEMENT,
                        comment);
            }
        }
    }

    public void cacheLink(String copiedElementUUID, String destinationUUID, String type,
            Direction direction, String comment) {
        List<LinkInformation> destinations = existingLinksByCopiedElementUUID
                .get(copiedElementUUID);
        if (destinations == null) {
            destinations = new LinkedList<>();
            existingLinksByCopiedElementUUID.put(copiedElementUUID, destinations);
        }
        destinations.add(new LinkInformation(destinationUUID, type, direction, comment));
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if (dao == null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    private static final class FindLinksForElements implements HibernateCallback {
        private final Set<String> sourceUUIDs;

        private FindLinksForElements(Set<String> sourceUUIDs) {
            this.sourceUUIDs = sourceUUIDs;
        }

        @Override
        public Object doInHibernate(Session session) throws SQLException {
            Query query = session
                    .createQuery("select l.dependant.uuid,l.dependency.uuid,l.id.typeId,l.comment "
                            + "from sernet.verinice.model.common.CnALink l "
                            + "where l.dependant.uuid in (:sourceUUIDs) or l.dependency.uuid in (:sourceUUIDs)");
            query.setParameterList("sourceUUIDs", sourceUUIDs);
            return query.list();
        }
    }

    /**
     * Information about a link as seen from a copied element
     */
    private static final class LinkInformation {

        LinkInformation(String destinationUUID, String type, Direction direction, String comment) {
            this.otherElementUUID = destinationUUID;
            this.type = type;
            this.direction = direction;
            this.comment = comment;
        }

        private final String otherElementUUID;
        private final String type;
        private final Direction direction;
        private final String comment;

    }

    private enum Direction {
        FROM_COPIED_ELEMENT, TO_COPIED_ELEMENT
    }

    public enum CopyLinksMode {
        NONE, ALL, FROM_COMPENDIUM_TO_MODEL
    }
}
