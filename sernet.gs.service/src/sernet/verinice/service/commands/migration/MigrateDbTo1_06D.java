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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CopyCommand;
import sernet.verinice.service.commands.CutCommand;
import sernet.verinice.service.commands.RemoveElement;

/**
 * In Verinice 1.15, the modernized base protection was introduced. In that
 * release, when modeling networks, requirements and safeguards (along with
 * their groups) were copied to the respective network only once and linked to
 * every target object. This class performs the migration to the new modeling
 * structure (separate copies of requirements and safeguards below the target
 * objects).
 */
public class MigrateDbTo1_06D extends DbMigration {

    private static final String REL_BP_REQUIREMENT_BP_THREAT = "rel_bp_requirement_bp_threat";

    private static final String REL_BP_REQUIREMENT_BP_SAFEGUARD = "rel_bp_requirement_bp_safeguard";

    private static final String SCOPE_ID = "scopeId";

    private static final String OBJECT_TYPE = "objectType";

    private static final long serialVersionUID = -5426191745458041569L;

    private static final Logger logger = Logger.getLogger(MigrateDbTo1_06D.class);

    public static final double VERSION = 1.06D;

    private Map<Integer, Map<String, CnATreeElement>> copiedElementsPerTargetById = new HashMap<>();

    private transient IBaseDao<CnALink, Serializable> cnALinkDao;

    private List<Integer> iTNetworkIDs;

    /**
     * Create a migration command that migrates data from all available IT
     * networks
     */
    public MigrateDbTo1_06D() {
        this(null);
    }

    /**
     * Create a migration command that only migrates data from IT networks with
     * the given IDs
     */
    public MigrateDbTo1_06D(List<Integer> iTNetworkIDs) {
        this.iTNetworkIDs = iTNetworkIDs;
    }

    @Override
    public void execute() {
        if ("false".equals(System.getProperty("veriniceserver.itbp.migration.1-16to1-17"))) {
            logger.warn("Skipping database update to version " + VERSION);
        } else {
            logger.info("Updating database to version " + VERSION);
            cnALinkDao = getDaoFactory().getDAO(CnALink.class);

            IBaseDao<CnATreeElement, Serializable> cnATreeElementDao = getDaoFactory()
                    .getDAO(CnATreeElement.class);

            if (iTNetworkIDs == null) {
                DetachedCriteria itNetworkIDsCriteria = DetachedCriteria
                        .forClass(CnATreeElement.class).createAlias("parent", "p")
                        .add(Restrictions.eq(OBJECT_TYPE, "bp_itnetwork"))
                        .add(Restrictions.in("p.objectType",
                                new String[] { "bp_model", "bp_import_group" }))
                        .setProjection(Projections.property("dbId"));
                iTNetworkIDs = cnATreeElementDao.findByCriteria(itNetworkIDsCriteria);
            }
            if (!iTNetworkIDs.isEmpty()) {
                logger.info("Migrating networks: " + iTNetworkIDs);
                // load all safeguards
                DetachedCriteria safeguardsCriteria = DetachedCriteria
                        .forClass(CnATreeElement.class)
                        .add(Restrictions.eq(OBJECT_TYPE, "bp_safeguard"))
                        .add(Restrictions.in(SCOPE_ID, iTNetworkIDs));
                new RetrieveInfo().setProperties(true).setLinksDown(true).setLinksUp(true)
                        .setParent(true).configureCriteria(safeguardsCriteria);

                List safeguards = cnATreeElementDao.findByCriteria(safeguardsCriteria);
                logger.info(safeguards.size() + " safeguards loaded");

                // load all threats
                DetachedCriteria threatsCriteria = DetachedCriteria.forClass(CnATreeElement.class)
                        .add(Restrictions.eq(OBJECT_TYPE, "bp_threat"))
                        .add(Restrictions.in(SCOPE_ID, iTNetworkIDs));
                new RetrieveInfo().setLinksDown(true).setLinksUp(true).setParent(true)
                        .configureCriteria(threatsCriteria);

                List<CnATreeElement> threats = cnATreeElementDao.findByCriteria(threatsCriteria);

                DetachedCriteria targetObjectsCriteria = DetachedCriteria
                        .forClass(CnATreeElement.class)
                        .add(Restrictions.in(OBJECT_TYPE,
                                new String[] { "bp_application", "bp_businessprocess", "bp_device",
                                        "bp_icssystem", "bp_itnetwork", "bp_itsystem", "bp_network",
                                        "bp_room" }))
                        .add(Restrictions.in(SCOPE_ID, iTNetworkIDs));
                new RetrieveInfo().setProperties(true).setLinksDown(true).setLinksUp(true)
                        .configureCriteria(targetObjectsCriteria);
                List targetObjects = cnATreeElementDao.findByCriteria(targetObjectsCriteria);
                logger.info(targetObjects.size() + " target objects loaded");

                try {
                    DetachedCriteria requirementsCriteria = DetachedCriteria
                            .forClass(CnATreeElement.class)
                            .add(Restrictions.eq(OBJECT_TYPE, "bp_requirement"))
                            .add(Restrictions.in(SCOPE_ID, iTNetworkIDs));
                    RetrieveInfo.getPropertyInstance().setLinksDown(true).setLinksUp(true)
                            .setParent(true).configureCriteria(requirementsCriteria);
                    requirementsCriteria.setFetchMode("parent.entity", FetchMode.JOIN);

                    List<CnATreeElement> requirements = cnATreeElementDao
                            .findByCriteria(requirementsCriteria);

                    logger.info("Migrating " + requirements.size() + " requirements");

                    for (CnATreeElement element : requirements) {
                        processRequirement(element);
                    }
                    logger.info("Migrating " + threats.size() + " threats");
                    for (CnATreeElement element : threats) {
                        processThreat(element);
                    }
                    removeEmptyGroups(iTNetworkIDs);

                } catch (CommandException e) {
                    throw new RuntimeCommandException("Failed to migrate database", e);
                }
            }
        }
        updateVersion();
    }

    private void processRequirement(CnATreeElement requirement) throws CommandException {
        Set<CnALink> linksDown = requirement.getLinksDown();
        List<CnALink> linksToTargetObjects = new ArrayList<>(linksDown.size());
        for (CnALink link : linksDown) {
            String relationId = link.getRelationId();
            if (!REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(relationId)
                    && !REL_BP_REQUIREMENT_BP_THREAT.equals(relationId)) {
                linksToTargetObjects.add(link);
            }
        }

        Iterator<CnALink> it = linksToTargetObjects.iterator();
        while (it.hasNext()) {
            CnALink link = it.next();
            boolean isLastLink = !it.hasNext();
            OperationMode operationMode = isLastLink ? OperationMode.MOVE : OperationMode.COPY;
            processLinkFromRequirementToTargetObject(link, requirement, operationMode);
        }
    }

    private void processThreat(CnATreeElement threat) throws CommandException {
        List<CnALink> linksToTargetObjects = new ArrayList<>(threat.getLinksDown());

        Iterator<CnALink> it = linksToTargetObjects.iterator();
        while (it.hasNext()) {
            CnALink link = it.next();
            boolean isLastLink = !it.hasNext();
            OperationMode operationMode = isLastLink ? OperationMode.MOVE : OperationMode.COPY;
            processLinkFromThreatToTargetObject(link, threat, operationMode);
        }

    }

    private void processLinkFromRequirementToTargetObject(CnALink link, CnATreeElement requirement,
            OperationMode operationModeRequirement) throws CommandException {
        CnATreeElement target = link.getDependency();
        CnATreeElement requirementBelowTargetObject = requirement;
        String requirementIdentifier = requirement.getEntity()
                .getRawPropertyValue("bp_requirement_id");
        if (requirementIdentifier == null
                || !elementHasRequirementWithIdentifierAsGrandchild(target,
                        requirementIdentifier)) {
            CnATreeElement requirementGroup = Objects.requireNonNull(requirement.getParent());
            CnATreeElement copiedGroup = copyGroupIfNecessary(requirementGroup, target,
                    "bp_requirement_group_id", copiedElementsPerTargetById);
            if (operationModeRequirement == OperationMode.COPY) {
                requirementBelowTargetObject = copyWithProperties(requirement, copiedGroup,
                        linkCandidate -> {
                            String relationID = linkCandidate.getRelationId();
                            if (REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(relationID)
                                    || REL_BP_REQUIREMENT_BP_THREAT.equals(relationID)) {
                                return true;
                            }
                            return linkCandidate.getDependency().getDbId().equals(target.getDbId());
                        });
                // remove the link from the global requirement to the
                // element
                deleteLink(link);
            } else {
                CutCommand cutCommand = new CutCommand(copiedGroup.getUuid(),
                        Collections.singletonList(requirement.getUuid()));
                getCommandService().executeCommand(cutCommand);
            }
        }

        Set<CnALink> linksDownFromRequirementBelowTargetObject = new HashSet<>(
                requirementBelowTargetObject.getLinksDown());
        for (Iterator<CnALink> iterator = linksDownFromRequirementBelowTargetObject
                .iterator(); iterator.hasNext();) {
            CnALink cnALink = iterator.next();
            if (REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(cnALink.getRelationId())) {
                processLinkFromRequirementToSafeguard(cnALink, target);
            }
        }

    }

    private void processLinkFromRequirementToSafeguard(CnALink linkToSafeguard,
            CnATreeElement target) throws CommandException {
        CnATreeElement requirement = linkToSafeguard.getDependant();
        CnATreeElement safeguard = linkToSafeguard.getDependency();
        OperationMode operationModeSafeguard = safeguard.getLinksUp().stream()
                .filter(item -> item.getRelationId().equals(REL_BP_REQUIREMENT_BP_SAFEGUARD))
                .count() > 1 ? OperationMode.COPY : OperationMode.MOVE;
        CnATreeElement safeguardGroup = safeguard.getParent();
        CnATreeElement copiedGroup = copyGroupIfNecessary(safeguardGroup, target,
                "bp_safeguard_group_id", copiedElementsPerTargetById);
        Map<String, CnATreeElement> copiedElementsForTarget = copiedElementsPerTargetById
                .get(target.getDbId());

        CnATreeElement existingSafeguard = null;
        if (copiedGroup.getChildren().contains(safeguard)) {
            existingSafeguard = safeguard;
        } else if (copiedElementsForTarget != null && copiedElementsForTarget
                .containsKey(Objects.requireNonNull(safeguard.getUuid()))) {
            existingSafeguard = copiedElementsForTarget.get(safeguard.getUuid());
        }

        boolean safeguardAlreadyCopied = existingSafeguard != null;
        if (operationModeSafeguard == OperationMode.COPY) {
            if (safeguardAlreadyCopied) {
                deleteLink(linkToSafeguard);
                // replace link from requirement to original safeguard
                CnALink newLink = new CnALink(requirement, existingSafeguard,
                        REL_BP_REQUIREMENT_BP_SAFEGUARD, linkToSafeguard.getComment());
                cnALinkDao.saveOrUpdate(newLink);
                return;
            }
            CnATreeElement copiedSafeuard = copyWithProperties(safeguard, copiedGroup,
                    linkCandidate -> {
                        String relationID = linkCandidate.getRelationId();
                        if (REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(relationID)) {
                            return linkCandidate.getDependant().getDbId()
                                    .equals(requirement.getDbId());
                        }
                        return true;
                    });

            if (copiedElementsForTarget == null) {
                copiedElementsForTarget = new HashMap<>();
                copiedElementsPerTargetById.put(target.getDbId(), copiedElementsForTarget);
            }
            copiedElementsForTarget.put(safeguard.getUuid(), copiedSafeuard);
            if (operationModeSafeguard == OperationMode.COPY) {
                // remove the link from the copied requirement to the
                // original safeguard
                deleteLink(linkToSafeguard);
            }
        } else {
            if (safeguardAlreadyCopied) {

                // replace link from requirement to original safeguard
                CnALink newLink = new CnALink(requirement, existingSafeguard,
                        REL_BP_REQUIREMENT_BP_SAFEGUARD, linkToSafeguard.getComment());
                cnALinkDao.saveOrUpdate(newLink);
                deleteLink(linkToSafeguard);

                // remove the unused safeguard since we already have a copy
                RemoveElement<CnATreeElement> removeSafeguard = new RemoveElement<>(safeguard);
                getCommandService().executeCommand(removeSafeguard);
            } else {
                CutCommand cutCommand = new CutCommand(copiedGroup.getUuid(),
                        Collections.singletonList(safeguard.getUuid()));
                getCommandService().executeCommand(cutCommand);
            }
        }

    }

    private void processLinkFromThreatToTargetObject(CnALink link, CnATreeElement threat,
            OperationMode operationMode) throws CommandException {
        CnATreeElement target = link.getDependency();
        CnATreeElement threatGroup = threat.getParent();
        CnATreeElement copiedGroup = copyGroupIfNecessary(threatGroup, target,
                "bp_threat_group_name", copiedElementsPerTargetById);
        if (operationMode == OperationMode.COPY) {
            Set<CnALink> additionalLinksToDelete = new HashSet<>();
            copyWithProperties(threat, copiedGroup, linkCandidate -> {
                String relationID = linkCandidate.getRelationId();

                if (REL_BP_REQUIREMENT_BP_THREAT.equals(relationID)) {
                    // only copy links to requirements that are linked to the
                    // target object
                    CnATreeElement linkedRequirement = linkCandidate.getDependant();

                    boolean result = linkedRequirement.getLinksDown().stream()
                            .anyMatch(linkFromRequirement -> linkFromRequirement.getDependency()
                                    .getDbId().equals(target.getDbId()));

                    if (result) {
                        additionalLinksToDelete.add(linkCandidate);
                    }
                    return result;
                }
                return linkCandidate.getDependency().getDbId().equals(target.getDbId());

            });
            // remove the link from the global threat to the
            // element

            deleteLink(link);
            additionalLinksToDelete.stream().forEach(this::deleteLink);
        } else {

            CutCommand cutCommand = new CutCommand(copiedGroup.getUuid(),
                    Collections.singletonList(threat.getUuid()));
            getCommandService().executeCommand(cutCommand);
        }
    }

    private CnATreeElement copyWithProperties(final CnATreeElement source, CnATreeElement target,
            final Predicate<CnALink> linkCopyPredicate) throws CommandException {
        if (!target.canContain(source)) {
            throw new IllegalStateException("Cannot copy " + source + " to "
                    + " target, unsupported type " + source.getClass());
        }
        CopyCommand copyCommand = new CopyCommand(target.getUuid(),
                Collections.singletonList(Objects.requireNonNull(
                        Objects.requireNonNull(source, "element must not be null").getUuid())),
                null);
        copyCommand.setCopyChildren(false);

        copyCommand = getCommandService().executeCommand(copyCommand);
        String copyUUID = copyCommand.getNewElements().iterator().next();
        if (copyUUID.equals(source.getUuid())) {
            throw new IllegalStateException(source + " not properly copied");
        }
        final CnATreeElement createdElement = getDaoFactory().getDAO(source.getClass())
                .findByUuid(copyUUID, new RetrieveInfo());
        if (linkCopyPredicate != null) {
            cnALinkDao.executeCallback(session -> {

                for (CnALink linkDown : source.getLinksDown()) {
                    if (linkCopyPredicate.test(linkDown)) {
                        CnATreeElement dependency = Retriever
                                .checkRetrieveLinks(linkDown.getDependency(), true);
                        CnALink newLink1 = new CnALink(createdElement, dependency,
                                linkDown.getRelationId(), linkDown.getComment());
                        session.merge(newLink1);
                    }
                }

                for (CnALink linkUp : source.getLinksUp()) {
                    if (linkCopyPredicate.test(linkUp)) {
                        CnATreeElement dependant = linkUp.getDependant();
                        CnALink newLink2 = new CnALink(dependant, createdElement,
                                linkUp.getRelationId(), linkUp.getComment());
                        session.merge(newLink2);
                    }
                }
                return null;
            });
        }

        return createdElement;
    }

    private CnATreeElement copyGroupIfNecessary(CnATreeElement group, CnATreeElement target,
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

    private void deleteLink(final CnALink link) {
        link.remove();
        cnALinkDao.delete(link);
    }

    private boolean elementHasRequirementWithIdentifierAsGrandchild(CnATreeElement target,
            String requirementIdentifier) {
        for (CnATreeElement child : Retriever.checkRetrieveChildren(target).getChildren()) {
            String childType = child.getTypeId();
            if (childType.equals("bp_requirement_group")) {
                child = Retriever.checkRetrieveChildren(child);
                for (CnATreeElement requirement : child.getChildren()) {
                    if (requirementIdentifier.equals(
                            requirement.getEntity().getRawPropertyValue("bp_requirement_id"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void removeEmptyGroups(List<Integer> allITNetworkIDs) throws CommandException {
        IBaseDao<CnATreeElement, Serializable> itNetworkDao = getDaoFactory()
                .getDAO(CnATreeElement.class);
        Set<CnATreeElement> elementsToRemove = new HashSet<>();
        @SuppressWarnings("unchecked")
        List<CnATreeElement> result = itNetworkDao.findByCriteria(DetachedCriteria
                .forClass(CnATreeElement.class).setFetchMode("children", FetchMode.JOIN)
                .add(Restrictions.in("dbId", allITNetworkIDs)));

        for (CnATreeElement itNetwork : result) {
            for (CnATreeElement child : itNetwork.getChildren()) {
                if (!(isBpRequirementGroup(child) || isSafeguardGroup(child)
                        || isBpThreatGroup(child))) {
                    continue;
                }
                addEmptyChildGroups(elementsToRemove, child);
            }
        }

        RemoveElement<CnATreeElement> removeElements = new RemoveElement<>(elementsToRemove);
        getCommandService().executeCommand(removeElements);
    }

    private boolean addEmptyChildGroups(Set<CnATreeElement> set, CnATreeElement element) {
        boolean foundElementPreventingDeletion = false;
        for (CnATreeElement child : element.getChildren()) {
            if (isBpRequirementGroup(child) || isSafeguardGroup(child) || isBpThreatGroup(child)) {
                boolean childIsEmpty = addEmptyChildGroups(set, child);
                if (!childIsEmpty) {
                    foundElementPreventingDeletion = true;
                }
            } else {
                foundElementPreventingDeletion = true;
            }

        }
        if (!foundElementPreventingDeletion) {
            set.add(element);
            return true;
        } else {
            return false;
        }
    }

    private static boolean isSafeguardGroup(CnATreeElement element) {
        return "bp_safeguard_group".equals(element.getTypeId());
    }

    private static boolean isBpRequirementGroup(CnATreeElement element) {
        return "bp_requirement_group".equals(element.getTypeId());
    }

    private static boolean isBpThreatGroup(CnATreeElement element) {
        return "bp_threat_group".equals(element.getTypeId());
    }

    enum OperationMode {
        COPY, MOVE
    }

    @Override
    public double getVersion() {
        return VERSION;
    }
}