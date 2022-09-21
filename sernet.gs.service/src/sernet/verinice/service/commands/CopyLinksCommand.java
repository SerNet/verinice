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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.CollectionUtil;
import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
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

    private transient Map<Integer, Integer> sourceDestMap;

    private transient Map<Integer, List<LinkInformation>> existingLinksByCopiedElementId;

    private transient IBaseDao<CnATreeElement, Serializable> dao;

    private final CopyLinksMode copyLinksMode;

    private transient Map<Integer, CnATreeElement> copiedElementsById;

    public CopyLinksCommand(Map<Integer, Integer> sourceDestMap, CopyLinksMode copyLinksMode) {
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
        loadAndCacheElements();
        copyLinks();
    }

    public void copyLinks() {
        number = 0;
        for (Entry<Integer, Integer> e : sourceDestMap.entrySet()) {
            Integer sourceId = e.getKey();
            Integer targetId = e.getValue();
            createLinks(targetId, existingLinksByCopiedElementId.remove(sourceId));
        }
    }

    private void createLinks(Integer copyTargetId, List<LinkInformation> linkInformations) {
        if (linkInformations == null) {
            return;
        }
        for (LinkInformation linkInformation : linkInformations) {
            processLink(linkInformation, copyTargetId);
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
    private void processLink(LinkInformation linkInformation, Integer copyTargetId) {
        Integer otherElementId = linkInformation.otherElementId;
        Integer copyDestinationId = sourceDestMap.get(otherElementId);
        if (copyDestinationId == null) {
            // the element on the other side of the link was not copied
            if (copyLinksMode == CopyLinksMode.FROM_COMPENDIUM_TO_MODEL) {
                // we don't want to copy the link if we copy from the compendium
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping link to " + otherElementId
                            + " while copying from compendium");
                }
                return;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating link to original target... " + copyTargetId + " -> "
                            + otherElementId);
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating link to copy of target... " + copyTargetId + " -> "
                        + otherElementId);
            }
            otherElementId = copyDestinationId;
        }

        if (linkInformation.direction == Direction.FROM_COPIED_ELEMENT) {
            createLink(copyTargetId, otherElementId, linkInformation.type, linkInformation.comment);
        } else {
            createLink(otherElementId, copyTargetId, linkInformation.type, linkInformation.comment);
        }
    }

    private void flushAndClear() {
        IBaseDao<CnALink, Serializable> linkDao = getDaoFactory().getDAO(CnALink.class);
        linkDao.flush();
        linkDao.clear();
        getDao().flush();
        getDao().clear();
    }

    private void createLink(Integer sourceId, Integer destId, String type, String comment) {
        CnATreeElement source = copiedElementsById.get(sourceId);
        CnATreeElement target = copiedElementsById.get(destId);
        CnALink link = new CnALink(source, target, type, comment);
        getDaoFactory().getDAO(CnALink.class).saveOrUpdate(link);
    }

    public void loadAndCacheLinks() {
        final Set<Integer> copiedElementUUIDs = sourceDestMap.keySet();
        List<ElementLink> allLinkedUuids = getDao()
                .findByCallback(new FindLinksForElements(copiedElementUUIDs));

        existingLinksByCopiedElementId = new HashMap<>(copiedElementUUIDs.size());
        for (ElementLink entry : allLinkedUuids) {
            Integer dependantId = entry.dependantId;
            Integer dependencyId = entry.dependencyId;
            String typeId = entry.typeId;
            String comment = entry.comment;
            if (copiedElementUUIDs.contains(dependantId)) {
                cacheLink(dependantId, dependencyId, typeId, Direction.FROM_COPIED_ELEMENT,
                        comment);
            } else {
                cacheLink(dependencyId, dependantId, typeId, Direction.TO_COPIED_ELEMENT, comment);
            }
        }
    }

    private void loadAndCacheElements() {
        Set<Integer> elementIDs = new HashSet<>();
        for (Entry<Integer, List<LinkInformation>> entry : existingLinksByCopiedElementId
                .entrySet()) {
            elementIDs.add(sourceDestMap.get(entry.getKey()));
            for (LinkInformation info : entry.getValue()) {
                Integer otherElementId = info.otherElementId;
                Integer copyDestinationId = sourceDestMap.get(otherElementId);
                if (copyDestinationId == null) {
                    // the element on the other side of the link was not copied
                    if (copyLinksMode != CopyLinksMode.FROM_COMPENDIUM_TO_MODEL) {
                        elementIDs.add(otherElementId);
                    }
                } else {
                    elementIDs.add(copyDestinationId);
                }
            }

        }
        copiedElementsById = new HashMap<>(elementIDs.size());

        CollectionUtil.partition(List.copyOf(elementIDs), 500).forEach(chunk -> {
            DetachedCriteria criteria = DetachedCriteria.forClass(CnATreeElement.class)
                    .add(Restrictions.in("id", chunk));
            RetrieveInfo.getPropertyInstance().setLinksUp(true).setLinksDown(true)
                    .configureCriteria(criteria);
            List<CnATreeElement> elements = getDao().findByCriteria(criteria);

            for (CnATreeElement element : elements) {
                copiedElementsById.put(element.getDbId(), element);
            }
        });
    }

    public void cacheLink(Integer copiedElementId, Integer destinationId, String type,
            Direction direction, String comment) {
        List<LinkInformation> destinations = existingLinksByCopiedElementId.get(copiedElementId);
        if (destinations == null) {
            destinations = new LinkedList<>();
            existingLinksByCopiedElementId.put(copiedElementId, destinations);
        }
        destinations.add(new LinkInformation(destinationId, type, direction, comment));
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if (dao == null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    private static final class FindLinksForElements implements HibernateCallback {
        private final Set<Integer> sourceIds;

        private FindLinksForElements(Set<Integer> sourceIds) {
            this.sourceIds = sourceIds;
        }

        @Override
        public Object doInHibernate(Session session) throws SQLException {
            Set<ElementLink> links = new HashSet<>();
            Query query1 = session
                    .createQuery("select l.id.dependantId,l.id.dependencyId,l.id.typeId,l.comment "
                            + "from sernet.verinice.model.common.CnALink l "
                            + "where l.id.dependantId in (:ids)");
            Query query2 = session
                    .createQuery("select l.id.dependantId,l.id.dependencyId,l.id.typeId,l.comment "
                            + "from sernet.verinice.model.common.CnALink l "
                            + "where l.id.dependencyId in (:ids)");
            CollectionUtil.partition(List.copyOf(sourceIds), IDao.QUERY_MAX_ITEMS_IN_LIST)
                    .forEach(partition -> {
                        Set.of(query1, query2).forEach(query -> {
                            query.setParameterList("ids", partition);
                            List<Object[]> result = query.list();
                            links.addAll(result.stream().map(entry -> {
                                Integer dependantId = (Integer) entry[0];
                                Integer dependencyId = (Integer) entry[1];
                                String typeId = (String) entry[2];
                                String comment = (String) entry[3];
                                return new ElementLink(dependantId, dependencyId, typeId, comment);
                            }).collect(Collectors.toSet()));

                        });

                    });
            return List.copyOf(links);
        }
    }

    /**
     * Information about a link as seen from a copied element
     */
    private static final class LinkInformation {

        LinkInformation(Integer destinationId, String type, Direction direction, String comment) {
            this.otherElementId = destinationId;
            this.type = type;
            this.direction = direction;
            this.comment = comment;
        }

        private final Integer otherElementId;
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

    private static final class ElementLink {
        public ElementLink(Integer dependantId, Integer dependencyId, String typeId,
                String comment) {
            this.dependantId = dependantId;
            this.dependencyId = dependencyId;
            this.typeId = typeId;
            this.comment = comment;
        }

        private final Integer dependantId;
        private final Integer dependencyId;
        private final String typeId;
        private final String comment;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((dependantId == null) ? 0 : dependantId.hashCode());
            result = prime * result + ((dependencyId == null) ? 0 : dependencyId.hashCode());
            result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ElementLink other = (ElementLink) obj;
            if (dependantId == null) {
                if (other.dependantId != null)
                    return false;
            } else if (!dependantId.equals(other.dependantId))
                return false;
            if (dependencyId == null) {
                if (other.dependencyId != null)
                    return false;
            } else if (!dependencyId.equals(other.dependencyId))
                return false;
            if (typeId == null) {
                if (other.typeId != null)
                    return false;
            } else if (!typeId.equals(other.typeId))
                return false;
            return true;
        }
    }
}
