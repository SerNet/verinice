/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.verinice.service.commands.migration;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CopyCommand;
import sernet.verinice.service.commands.CreateLink;

/**
 * Abstract base class for commands which used to migrate ITBP modeling data.
 * See class MigrateDbTo1_06D for more documentation
 */
public abstract class MigrateModellingCommand extends GenericCommand {

    private static final long serialVersionUID = -8260461595706398827L;

    private static final Logger logger = Logger.getLogger(MigrateModellingCommand.class);

    private static final String SQL_INSERT_LINK = "INSERT INTO cnalink "
            + "(dependant_id,dependency_id,type_id,linktype,comment) "
            + "VALUES (:dependantId,:dependencyId,:linkType,1,:comment)";
    private static final String SQL_DELETE_LINK = "DELETE FROM cnalink "
            + "WHERE dependant_id=:dependantId AND dependency_id=:dependencyId AND type_id=:linkType";

    protected transient IBaseDao<CnALink, Serializable> linkDao;
    protected transient IBaseDao<CnATreeElement, Serializable> elementDao;

    protected CnATreeElement copyWithProperties(final CnATreeElement source, CnATreeElement group,
            final Predicate<CnALink> linkCopyPredicate) throws CommandException {
        if (!group.canContain(source)) {
            throw new IllegalStateException("Cannot copy " + source + " to "
                    + " target, unsupported type " + source.getClass());
        }
        String copyUUID = copyElement(source, group);
        if (copyUUID.equals(source.getUuid())) {
            throw new IllegalStateException(source + " not properly copied");
        }
        final CnATreeElement copiedElement = elementDao.findByUuid(copyUUID, new RetrieveInfo());
        if (linkCopyPredicate != null) {
            copyLinks(source, copiedElement, linkCopyPredicate);
        }
        return copiedElement;
    }

    /**
     * Copies the given element to the given group. Returns the UUID of the
     * copied element.
     */
    private String copyElement(final CnATreeElement element, CnATreeElement group)
            throws CommandException {
        CopyCommand copyCommand = new CopyCommand(group.getUuid(), createUuidList(element), null);
        copyCommand.setCopyChildren(false);
        copyCommand = getCommandService().executeCommand(copyCommand);

        return copyCommand.getNewElements().iterator().next();
    }

    private void copyLinks(final CnATreeElement source, final CnATreeElement copiedElement,
            final Predicate<CnALink> linkCopyPredicate) throws CommandException {
        copyLinksByCommand(source, copiedElement, linkCopyPredicate);
    }

    private void copyLinksBySql(final CnATreeElement source, final CnATreeElement copiedElement,
            final Predicate<CnALink> linkCopyPredicate) throws CommandException {
        for (CnALink linkDown : source.getLinksDown()) {
            if (linkCopyPredicate.test(linkDown)) {
                createLink(copiedElement.getDbId(), linkDown.getDependency().getDbId(),
                        linkDown.getRelationId(), linkDown.getComment());
            }
        }
        for (CnALink linkUp : source.getLinksUp()) {
            if (linkCopyPredicate.test(linkUp)) {
                createLink(linkUp.getDependant().getDbId(), copiedElement.getDbId(),
                        linkUp.getRelationId(), linkUp.getComment());
            }
        }
    }

    private void copyLinksByCommand(final CnATreeElement source, final CnATreeElement copiedElement,
            final Predicate<CnALink> linkCopyPredicate) throws CommandException {
        for (CnALink linkDown : source.getLinksDown()) {
            if (linkCopyPredicate.test(linkDown)) {
                CreateLink<CnATreeElement, CnATreeElement> createLinkCommand = new CreateLink<>(
                        copiedElement.getUuid(), linkDown.getDependency().getUuid(),
                        linkDown.getRelationId(), linkDown.getComment());
                createLinkCommand = getCommandService().executeCommand(createLinkCommand);
                if (logger.isDebugEnabled()) {
                    logger.debug("Created: " + createLinkCommand.getLink());
                }
            }
        }
        for (CnALink linkUp : source.getLinksUp()) {
            if (linkCopyPredicate.test(linkUp)) {
                CreateLink<CnATreeElement, CnATreeElement> createLinkCommand = new CreateLink<>(
                        linkUp.getDependant().getUuid(), copiedElement.getUuid(),
                        linkUp.getRelationId(), linkUp.getComment());
                createLinkCommand = getCommandService().executeCommand(createLinkCommand);
                if (logger.isDebugEnabled()) {
                    logger.debug("Created: " + createLinkCommand.getLink());
                }
            }
        }
    }

    protected CnATreeElement copyGroupIfNecessary(CnATreeElement group, CnATreeElement target,
            String identityCheckProperty,
            Map<Integer, Map<String, CnATreeElement>> createdGroupsPerElementById)
            throws CommandException {
        group = Retriever.retrieveElement(Objects.requireNonNull(group, "group must not be null"),
                RetrieveInfo.getPropertyInstance());
        target = Retriever.checkRetrieveElementAndChildren(
                Objects.requireNonNull(target, "target must not be null"));

        String groupIdentifier = group.getEntity().getRawPropertyValue(identityCheckProperty);
        if (groupIdentifier != null) {
            for (CnATreeElement child : target.getChildren()) {
                if (group.getTypeId().equals(child.getTypeId())
                        && groupIdentifier.equals(Retriever.checkRetrieveElement(child).getEntity()
                                .getRawPropertyValue(identityCheckProperty))) {
                    return child;
                }
            }
        }
        Map<String, CnATreeElement> createdElementsForTarget = createdGroupsPerElementById
                .get(target.getDbId());
        if (createdElementsForTarget != null) {
            CnATreeElement alreadyCreatedGroup = createdElementsForTarget.get(group.getUuid());
            if (alreadyCreatedGroup != null) {
                return alreadyCreatedGroup;
            }
        } else {
            createdElementsForTarget = new HashMap<>();
            createdGroupsPerElementById.put(target.getDbId(), createdElementsForTarget);
        }

        CnATreeElement createdGroup = copyWithProperties(group, target, null);
        createdElementsForTarget.put(group.getUuid(), createdGroup);
        return createdGroup;
    }

    private List<String> createUuidList(final CnATreeElement source) {
        return Collections.singletonList(Objects.requireNonNull(
                Objects.requireNonNull(source, "element must not be null").getUuid()));
    }

    protected void createLink(int dependantId, int dependencyId, String relationId,
            String comment) {
        if (logger.isDebugEnabled()) {
            logger.debug("Insert link SQL: " + SQL_INSERT_LINK);
        }
        linkDao.executeCallback(session -> {
            Query query = session.createSQLQuery(SQL_INSERT_LINK);
            query.setInteger("dependantId", dependantId);
            query.setInteger("dependencyId", dependencyId);
            query.setString("linkType", relationId);
            query.setString("comment", comment);
            return query.executeUpdate();
        });
    }

    protected void deleteLink(final CnALink link) {
        deleteLinkByDao(link);
    }

    protected void deleteLinkBySql(final CnALink link) {
        if (logger.isDebugEnabled()) {
            logger.debug("Delete link SQL: " + SQL_DELETE_LINK);
        }
        linkDao.executeCallback(session -> {
            Query query = session.createSQLQuery(SQL_DELETE_LINK);
            query.setInteger("dependantId", link.getId().getDependantId());
            query.setInteger("dependencyId", link.getId().getDependencyId());
            query.setString("linkType", link.getId().getTypeId());
            return query.executeUpdate();
        });
    }

    protected void deleteLinkByDao(final CnALink link) {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting link: " + link + "...");
        }
        linkDao.delete(link);
        link.setDependant(Retriever.checkRetrieveLinks(link.getDependant(), true));
        link.setDependency(Retriever.checkRetrieveLinks(link.getDependency(), true));
        link.remove();
    }

    protected void initializeDaos() {
        linkDao = getDaoFactory().getDAO(CnALink.class);
        elementDao = getDaoFactory().getDAO(CnATreeElement.class);
    }

    protected void flushAndClearDaos() {
        linkDao.flush();
        linkDao.clear();
        elementDao.flush();
        elementDao.clear();
    }

    enum OperationMode {
        COPY, MOVE
    }
}
