/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This task models modules (requirements groups) from the ITBP compendium with
 * certain target object types of an IT network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelModulesTask extends ModelCopyTask {

    private final Set<CnATreeElement> modulesCompendium;

    public ModelModulesTask(ICommandService commandService, IDAOFactory daoFactory,
            ModelingData modelingData) {
        super(commandService, daoFactory, modelingData, BpRequirementGroup.TYPE_ID, null,
                BpRequirement.PROP_RELEASE, BpRequirementGroup.PROP_RELEASE);
        this.modulesCompendium = modelingData.getRequirementGroups();
    }

    @Override
    protected String getIdentifier(CnATreeElement element) {
        if (element instanceof BpRequirementGroup) {
            return ((BpRequirementGroup) element).getIdentifier();
        }
        if (element instanceof BpRequirement) {
            return ((BpRequirement) element).getIdentifier();
        }
        throw new IllegalArgumentException("Cannot handle " + element);

    }

    @Override
    protected void afterCopyElement(CnATreeElement targetObject, CnATreeElement newElement,
            CnATreeElement compendiumElement) {
        if (newElement.getEntity().isFlagged(BpRequirement.PROP_IMPLEMENTATION_DEDUCE)) {
            newElement.getEntity().setFlag(BpRequirement.PROP_IMPLEMENTATION_DEDUCE, false);
            daoFactory.getDAO(CnATreeElement.class).merge(newElement);
        }
        afterHandleElement(targetObject, newElement);
    }

    @Override
    protected void afterSkipExistingElement(CnATreeElement targetObject,
            CnATreeElement existingElement, CnATreeElement compendiumElement) {
        afterHandleElement(targetObject, existingElement);
    }

    @Override
    protected void updateExistingElement(CnATreeElement targetObject,
            CnATreeElement existingElement, CnATreeElement compendiumElement,
            boolean elementRemoved) {
        copyProperties(compendiumElement, existingElement, BpRequirement.PROP_QUALIFIER,
                BpRequirement.PROP_LAST_CHANGE, BpRequirement.PROP_RELEASE,
                BpRequirement.PROP_CHANGE_DETAILS);
        if (elementRemoved) {
            existingElement.setPropertyValue(BpRequirement.PROP_CHANGE_TYPE,
                    BpRequirement.PROP_CHANGE_TYPE_REMOVED);
        } else {
            copyProperties(compendiumElement, existingElement, BpRequirement.PROP_NAME,
                    BpRequirement.PROP_CHANGE_TYPE, BpRequirement.PROP_OBJECTBROWSER);
        }

        Set<String> linkedThreatIDsCompendium = compendiumElement.getLinksDown().stream()
                .filter(link -> BpRequirement.REL_BP_REQUIREMENT_BP_THREAT
                        .equals(link.getRelationId()))
                .map(CnALink::getDependency)
                .map(r -> r.getEntity().getPropertyValue(BpThreat.PROP_ID))
                .collect(Collectors.toSet());
        Iterator<CnALink> it = existingElement.getLinksDown().iterator();
        while (it.hasNext()) {
            CnALink link = it.next();
            if (BpRequirement.REL_BP_REQUIREMENT_BP_THREAT.equals(link.getRelationId())) {
                CnATreeElement dependency = link.getDependency();
                if (!(linkedThreatIDsCompendium
                        .contains(dependency.getEntity().getPropertyValue(BpThreat.PROP_ID)))) {
                    daoFactory.getDAO(CnALink.class).delete(link);
                    it.remove();
                }
            }
        }

        afterHandleElement(targetObject, existingElement);
    }

    @Override
    protected void updateExistingGroup(CnATreeElement targetObject, CnATreeElement existingGroup,
            CnATreeElement compendiumGroup, boolean groupRemoved) {
        copyProperties(compendiumGroup, existingGroup, BpRequirementGroup.PROP_IMPLEMENTATION_ORDER,
                BpRequirementGroup.PROP_LAST_CHANGE, BpRequirementGroup.PROP_RELEASE,
                BpRequirementGroup.PROP_CHANGE_DETAILS);
        if (groupRemoved) {
            existingGroup.setPropertyValue(BpRequirementGroup.PROP_CHANGE_TYPE,
                    BpRequirementGroup.PROP_CHANGE_TYPE_REMOVED);
        } else {
            copyProperties(compendiumGroup, existingGroup, BpRequirementGroup.PROP_NAME,
                    BpRequirementGroup.PROP_CHANGE_TYPE,
                    BpRequirementGroup.PROP_OBJECTBROWSER_DESC);
        }
    }

    private void afterHandleElement(CnATreeElement targetObject,
            CnATreeElement requirementFromScope) {
        String linkType = BpRequirement.getLinkTypeToTargetObject(targetObject.getTypeId());
        CnALink linkToTargetObject = new CnALink(requirementFromScope, targetObject, linkType, "");
        daoFactory.getDAO(CnALink.class).merge(linkToTargetObject);
    }

    @Override
    public Set<CnATreeElement> getGroupsFromCompendium() {
        return modulesCompendium;
    }
}