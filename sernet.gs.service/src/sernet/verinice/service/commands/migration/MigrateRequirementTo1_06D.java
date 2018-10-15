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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import sernet.gs.service.Retriever;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CutCommand;
import sernet.verinice.service.commands.RemoveElement;

/**
 * This command is called by MigrateDbTo1_06D and is used to migrate requirement
 * modeling.
 */
public class MigrateRequirementTo1_06D extends MigrateModellingCommand {

    private Map<Integer, Map<String, CnATreeElement>> copiedElementsPerTargetById;

    CnATreeElement requirement;

    public MigrateRequirementTo1_06D(CnATreeElement requirement,
            Map<Integer, Map<String, CnATreeElement>> copiedElementsPerTargetById) {
        this.requirement = requirement;
        this.copiedElementsPerTargetById = copiedElementsPerTargetById;
    }

    @Override
    public void execute() {
        initializeDaos();
        try {
            processRequirement(requirement);
        } catch (CommandException e) {
            throw new RuntimeCommandException("Failed to migrate requirement: " + requirement, e);
        }
        flushAndClearDaos();
    }

    private void processRequirement(CnATreeElement requirement) throws CommandException {
        Set<CnALink> linksDown = requirement.getLinksDown();
        List<CnALink> linksToTargetObjects = new ArrayList<>(linksDown.size());
        for (CnALink link : linksDown) {
            String relationId = link.getRelationId();
            if (!BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(relationId)
                    && !BpRequirement.REL_BP_REQUIREMENT_BP_THREAT.equals(relationId)) {
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
            CnATreeElement requirementGroupCopy = copyGroupIfNecessary(requirementGroup, target,
                    "bp_requirement_group_id", copiedElementsPerTargetById);
            if (operationModeRequirement == OperationMode.COPY) {
                requirementBelowTargetObject = copyWithProperties(requirement, requirementGroupCopy,
                        linkCandidate -> {
                            String relationID = linkCandidate.getRelationId();
                            if (BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(relationID)
                                    || BpRequirement.REL_BP_REQUIREMENT_BP_THREAT
                                            .equals(relationID)) {
                                return true;
                            }
                            return linkCandidate.getDependency().getDbId().equals(target.getDbId());
                        });
                linkDao.flush();
                linkDao.clear();
                // remove the link from the global requirement to the
                // element
                deleteLink(link);
            } else {
                CutCommand cutCommand = new CutCommand(requirementGroupCopy.getUuid(),
                        Collections.singletonList(requirement.getUuid()));
                getCommandService().executeCommand(cutCommand);
            }
        }

        Set<CnALink> linksDownFromRequirementBelowTargetObject = new HashSet<>(
                requirementBelowTargetObject.getLinksDown());
        for (Iterator<CnALink> iterator = linksDownFromRequirementBelowTargetObject
                .iterator(); iterator.hasNext();) {
            CnALink cnALink = iterator.next();
            if (BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(cnALink.getRelationId())) {
                processLinkFromRequirementToSafeguard(cnALink, target);
            }
        }

    }

    private void processLinkFromRequirementToSafeguard(CnALink linkToSafeguard,
            CnATreeElement target) throws CommandException {
        CnATreeElement requirement = linkToSafeguard.getDependant();
        CnATreeElement safeguard = linkToSafeguard.getDependency();
        OperationMode operationModeSafeguard = safeguard.getLinksUp().stream()
                .filter(item -> item.getRelationId()
                        .equals(BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD))
                .count() > 1 ? OperationMode.COPY : OperationMode.MOVE;
        CnATreeElement safeguardGroup = Retriever.checkRetrieveParent(safeguard).getParent();
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
                        BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD,
                        linkToSafeguard.getComment());
                linkDao.saveOrUpdate(newLink);
                return;
            }
            CnATreeElement copiedSafeuard = copyWithProperties(safeguard, copiedGroup,
                    linkCandidate -> {
                        String relationID = linkCandidate.getRelationId();
                        if (BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(relationID)) {
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
                linkDao.flush();
                linkDao.clear();
                // remove the link from the copied requirement to the
                // original safeguard
                deleteLink(linkToSafeguard);
            }
        } else {
            if (safeguardAlreadyCopied) {

                // replace link from requirement to original safeguard
                CnALink newLink = new CnALink(requirement, existingSafeguard,
                        BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD,
                        linkToSafeguard.getComment());
                linkDao.saveOrUpdate(newLink);
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

    private boolean elementHasRequirementWithIdentifierAsGrandchild(CnATreeElement target,
            String requirementIdentifier) {
        for (CnATreeElement child : Retriever.checkRetrieveChildren(target).getChildren()) {
            String childType = child.getTypeId();
            if (childType.equals("bp_requirement_group")) {
                child = Retriever.checkRetrieveChildren(child);
                for (CnATreeElement requirementChild : child.getChildren()) {
                    if (requirementIdentifier.equals(requirementChild.getEntity()
                            .getRawPropertyValue("bp_requirement_id"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}