/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.CollectionUtil;
import sernet.gs.service.LinkValidator;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Check whether all the links from and to elements in a specific subtree are
 * valid according to the SNCA
 */
public class ValidateLinksInSubtrees extends GenericCommand {

    private static final long serialVersionUID = -168146657250647713L;
    private Set<String> rootElementUUIDs;
    private Set<CnALink> invalidLinks;

    public ValidateLinksInSubtrees(Collection<String> rootElementUUIDs) {
        this.rootElementUUIDs = new HashSet<>(rootElementUUIDs);
    }

    @Override
    public void execute() {
        @NonNull
        IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);

        DetachedCriteria criteria = DetachedCriteria.forClass(CnATreeElement.class)
                .add(Restrictions.in(CnATreeElement.UUID, rootElementUUIDs));
        List<CnATreeElement> elements = dao.findByCriteria(criteria);

        try {
            LoadSubtreeIds loadSubtreeIds = new LoadSubtreeIds(elements);
            Set<Integer> subTreeIds = getCommandService().executeCommand(loadSubtreeIds)
                    .getDbIdsOfSubtree();

            // Process data in partitions due to Oracle limitations
            this.invalidLinks = CollectionUtil
                    .partition(new ArrayList<>(subTreeIds), IDao.QUERY_MAX_ITEMS_IN_LIST).stream()
                    .flatMap(partition -> {
                        DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class)
                                .add(Restrictions.in("dbId", partition));
                        List<CnATreeElement> subtreeElements = dao.findByCriteria(crit);
                        return subtreeElements.stream();
                    }).flatMap(this::getInvalidLinks).collect(Collectors.toUnmodifiableSet());

        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
    }

    private Stream<CnALink> getInvalidLinks(CnATreeElement element) {
        return Stream
                .concat(element.getLinksUp().stream()
                        .filter(link -> !LinkValidator.isRelationValid(link.getDependant(), element,
                                link.getRelationId())),
                        element.getLinksDown().stream()
                                .filter(link -> !LinkValidator.isRelationValid(element,
                                        link.getDependency(), link.getRelationId())))
                .map(link -> {
                    // initialize dependant and dependency title so we
                    // can use them in a potential error message
                    link.getDependant().getTitle();
                    link.getDependency().getTitle();
                    return link;
                });
    }

    public Set<CnALink> getInvalidLinks() {
        return invalidLinks;
    }

}
