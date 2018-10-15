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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.gs.service.Retriever;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CutCommand;

/**
 * This command is called by MigrateDbTo1_06D and is used to migrate threat
 * modeling.
 */
public class MigrateThreatTo1_06D extends MigrateModellingCommand {

    private static final long serialVersionUID = -3997074254000293178L;

    private Map<Integer, Map<String, CnATreeElement>> copiedElementsPerTargetById;

    CnATreeElement threat;

    /**
     * Create a migration command that only migrates data from IT networks with
     * the given IDs
     */
    public MigrateThreatTo1_06D(CnATreeElement threat,
            Map<Integer, Map<String, CnATreeElement>> copiedElementsPerTargetById) {
        this.threat = threat;
        this.copiedElementsPerTargetById = copiedElementsPerTargetById;
    }

    @Override
    public void execute() {
        initializeDaos();
        try {
            processThreat(threat);
        } catch (CommandException e) {
            throw new RuntimeCommandException("Failed to migrate requirement: " + threat, e);
        }
        flushAndClearDaos();
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

    private void processLinkFromThreatToTargetObject(CnALink link, CnATreeElement threat,
            OperationMode operationMode) throws CommandException {
        CnATreeElement target = link.getDependency();
        CnATreeElement threatGroup = Retriever.checkRetrieveParent(threat).getParent();
        CnATreeElement copiedGroup = copyGroupIfNecessary(threatGroup, target,
                "bp_threat_group_name", copiedElementsPerTargetById);
        if (operationMode == OperationMode.COPY) {
            Set<CnALink> additionalLinksToDelete = new HashSet<>();
            copyWithProperties(threat, copiedGroup, linkCandidate -> {
                String relationID = linkCandidate.getRelationId();

                if (BpRequirement.REL_BP_REQUIREMENT_BP_THREAT.equals(relationID)) {
                    // only copy links to requirements that are linked to the
                    // target object
                    CnATreeElement linkedRequirement = linkCandidate.getDependant();
                    linkedRequirement = Retriever.checkRetrieveLinks(linkedRequirement, false);
                    linkCandidate.setDependant(linkedRequirement);
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
            linkDao.flush();
            linkDao.clear();
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


}