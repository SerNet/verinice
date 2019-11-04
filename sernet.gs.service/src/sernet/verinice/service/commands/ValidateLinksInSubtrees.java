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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.LinkValidator;
import sernet.verinice.interfaces.GenericCommand;
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
        List<CnATreeElement> elements = getDaoFactory().getDAO(CnATreeElement.class)
                .findByCriteria(DetachedCriteria.forClass(CnATreeElement.class)
                        .add(Restrictions.in(CnATreeElement.UUID, rootElementUUIDs)));

        Set<CnALink> invalidLinks = new HashSet<>();
        for (CnATreeElement element : elements) {
            addInvalidLinksToSet(element, invalidLinks);
        }

        this.invalidLinks = Collections.unmodifiableSet(invalidLinks);
    }

    private void addInvalidLinksToSet(CnATreeElement element, Set<CnALink> invalidLinks) {
        invalidLinks.addAll(
                Stream.concat(element.getLinksUp().stream(), element.getLinksDown().stream())
                        .filter(link -> !LinkValidator.isRelationValid(link.getDependant(),
                                link.getDependency(), link.getRelationId()))
                        .map(link -> {
                            // initialize dependant and dependency title so we
                            // can use them in a potential error message
                            link.getDependant().getTitle();
                            link.getDependency().getTitle();
                            return link;
                        }).collect(Collectors.toSet()));
        element.getChildren().forEach(child -> addInvalidLinksToSet(child, invalidLinks));
    }

    public Set<CnALink> getInvalidLinks() {
        return invalidLinks;
    }

}
