/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade <jk{a}sernet{dot}de>.
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
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

public class ReferencingCommand extends GenericCommand implements IChangeLoggingCommand {

    private static final long serialVersionUID = 7375543903414429067L;

    private Collection<Integer> moduleIDs;
    private Integer targetID;

    private String stationId;

    public ReferencingCommand(Collection<Integer> moduleIDs, Integer targetID) {
        super();
        this.stationId = ChangeLogEntry.STATION_ID;
        validateParameter(moduleIDs, targetID);
        this.moduleIDs = moduleIDs;
        this.targetID = targetID;
    }

    @Override
    public void execute() {
        @NonNull
        IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
        @SuppressWarnings("unchecked")
        Set<CnATreeElement> requirementGroups = new HashSet<>(dao.findByCriteria(DetachedCriteria
                .forClass(CnATreeElement.class).add(Restrictions.in("dbId", moduleIDs))
                .setFetchMode("children", FetchMode.JOIN)
                .setFetchMode("children.linksDown", FetchMode.JOIN)));
        List<CnALink> newLinks = new ArrayList<>();
        CnATreeElement targetElement = dao.findById(targetID);
        for (CnATreeElement requirementGroup : requirementGroups) {
            newLinks.addAll(addModuleReferences(requirementGroup, targetElement));
        }
        getDaoFactory().getDAO(CnALink.class).saveOrUpdateAll(newLinks);
    }

    private Collection<CnALink> addModuleReferences(CnATreeElement droppedElement,
            CnATreeElement dropTarget) {
        Set<CnALink> result = new HashSet<>();
        Set<CnATreeElement> threatsToLink = new HashSet<>();
        Set<CnATreeElement> existingLinkedRequirements = dropTarget.getLinksUp().stream()
                .map(CnALink::getDependant).filter(d -> d.getTypeId().equals(BpRequirement.TYPE_ID))
                .collect(Collectors.toSet());
        Set<CnATreeElement> existingLinkedThreats = dropTarget.getLinksUp().stream()
                .map(CnALink::getDependant).filter(d -> d.getTypeId().equals(BpThreat.TYPE_ID))
                .collect(Collectors.toSet());

        for (CnATreeElement requirement : droppedElement.getChildren()) {
            if (!existingLinkedRequirements.contains(requirement)) {
                CnALink link = new CnALink(requirement, dropTarget,
                        BpRequirement.getLinkTypeToTargetObject(dropTarget.getTypeId()),
                        StringUtils.EMPTY);
                result.add(link);
            }
            for (CnALink linkDown : requirement.getLinksDown()) {
                if (linkDown.getRelationId().equals(BpRequirement.REL_BP_REQUIREMENT_BP_THREAT)) {
                    CnATreeElement threat = linkDown.getDependency();
                    if (!existingLinkedThreats.contains(threat)) {
                        threatsToLink.add(threat);
                    }
                }
            }
        }
        for (CnATreeElement threat : threatsToLink) {
            CnALink link = new CnALink(threat, dropTarget,
                    BpThreat.getLinkTypeToTargetObject(dropTarget.getTypeId()), StringUtils.EMPTY);
            result.add(link);
        }
        return result;
    }

    private void validateParameter(Collection<Integer> moduleIDs, Integer targetID) {
        if (moduleIDs == null) {
            throw new IllegalArgumentException("Module ids must not be null.");
        }
        if (targetID == null) {
            throw new IllegalArgumentException("Target element id must not be null.");
        }
        if (moduleIDs.isEmpty()) {
            throw new IllegalArgumentException("Module ids list is empty.");
        }

    }

    @Override
    public String getStationId() {
        return stationId;
    }

    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

}
