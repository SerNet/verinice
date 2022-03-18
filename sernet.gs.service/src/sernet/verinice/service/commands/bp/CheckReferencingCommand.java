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
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

public class CheckReferencingCommand extends GenericCommand {

    private static final long serialVersionUID = 7375543903414429067L;

    private Collection<Integer> moduleIDs;
    private Integer targetID;

    private Set<String> existingModules;

    public CheckReferencingCommand(Collection<Integer> moduleIDs, Integer targetID) {
        super();
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

        CnATreeElement targetElement = dao.findById(targetID);
        existingModules = new HashSet<>(requirementGroups.size());
        Set<String> existingLinkedRequirementIdentifiers = targetElement.getLinksUp().stream()
                .map(CnALink::getDependant).filter(d -> d.getTypeId().equals(BpRequirement.TYPE_ID))
                .map(r -> r.getEntity().getRawPropertyValue(BpRequirement.PROP_ID))
                .filter(Objects::nonNull).collect(Collectors.toSet());

        for (CnATreeElement requirementGroup : requirementGroups) {
            if (linksToRequirementsExist(existingLinkedRequirementIdentifiers, requirementGroup)) {
                existingModules.add(requirementGroup.getTitle());
            }
        }
    }

    private boolean linksToRequirementsExist(Set<String> existingLinkedRequirementIdentifiers,
            CnATreeElement requirementGroup) {
        for (CnATreeElement requirement : requirementGroup.getChildren()) {
            String identifier = requirement.getEntity().getRawPropertyValue(BpRequirement.PROP_ID);
            if (identifier != null && existingLinkedRequirementIdentifiers.contains(identifier)) {
                return true;
            }
        }
        return false;
    }

    private void validateParameter(Collection<Integer> moduleIDs, Integer targetID) {
        if (moduleIDs == null) {
            throw new IllegalArgumentException("Module ids must not be null.");
        }
        if (targetID == null) {
            throw new IllegalArgumentException("Target element ids must not be null.");
        }
        if (moduleIDs.isEmpty()) {
            throw new IllegalArgumentException("Module ids list is empty.");
        }
    }

    public Set<String> getExistingModules() {
        return existingModules;
    }

}
