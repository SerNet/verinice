/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin <dm{a}sernet{dot}de>.
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

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This task models threat groups from the ITBP compendium with certain target
 * object types of an IT network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelThreatGroupTask extends ModelCopyTask {

    private static final Logger LOG = Logger.getLogger(ModelThreatGroupTask.class);

    private final Set<CnATreeElement> requirementGroups;

    public ModelThreatGroupTask(ICommandService commandService, IDAOFactory daoFactory,
            ModelingData modelingData) {
        super(commandService, daoFactory, modelingData, BpThreatGroup.TYPE_ID,
                element -> element.getLinksUp().stream().map(CnALink::getDependant)
                        .map(modelingData::getScopeElementByCompendiumElement)
                        .anyMatch(Objects::nonNull),
                BpThreat.PROP_RELEASE, BpThreatGroup.PROP_RELEASE);
        this.requirementGroups = modelingData.getRequirementGroups();
    }

    @Override
    public Set<CnATreeElement> getGroupsFromCompendium() {
        Set<CnATreeElement> threatGroupsFromCompendium = retrieveThreatGroupsFromModules();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Threat groups linked to modules: ");
            logElements(threatGroupsFromCompendium);
        }
        return threatGroupsFromCompendium;
    }

    @Override
    protected String getIdentifier(CnATreeElement element) {
        if (element instanceof BpThreat) {
            return ((BpThreat) element).getIdentifier();
        }
        if (element instanceof BpThreatGroup) {
            return ((BpThreatGroup) element).getTitle();
        }
        throw new IllegalArgumentException("Cannot handle " + element);

    }

    @Override
    protected void afterCopyElement(CnATreeElement targetObject, CnATreeElement newElement,
            CnATreeElement compendiumElement) {
        afterHandleElement(targetObject, newElement, compendiumElement);
    }

    @Override
    protected void afterSkipExistingElement(CnATreeElement targetObject,
            CnATreeElement existingElement, CnATreeElement compendiumElement) {
        afterHandleElement(targetObject, existingElement, compendiumElement);
    }

    @Override
    protected void updateExistingElement(CnATreeElement targetObject,
            CnATreeElement existingElement, CnATreeElement compendiumElement,
            boolean elementRemoved) {
        copyProperties(compendiumElement, existingElement, BpThreat.PROP_RELEASE,
                BpThreat.PROP_CHANGE_DETAILS, BpThreat.PROP_CONFIDENTIALITY,
                BpThreat.PROP_INTEGRITY, BpThreat.PROP_AVAILABILITY);
        if (elementRemoved) {
            existingElement.setPropertyValue(BpThreat.PROP_CHANGE_TYPE,
                    BpThreat.PROP_CHANGE_TYPE_REMOVED);
        } else {
            copyProperties(compendiumElement, existingElement, BpThreat.PROP_NAME,
                    BpThreat.PROP_CHANGE_TYPE, BpThreat.PROP_OBJECTBROWSER_DESC);
        }
        afterHandleElement(targetObject, existingElement, compendiumElement);

    }

    @Override
    protected void updateExistingGroup(CnATreeElement targetObject, CnATreeElement existingGroup,
            CnATreeElement compendiumGroup, boolean groupRemoved) {
        copyProperties(compendiumGroup, existingGroup, BpThreatGroup.PROP_RELEASE,
                BpThreatGroup.PROP_CHANGE_DETAILS);
        if (groupRemoved) {
            existingGroup.setPropertyValue(BpThreatGroup.PROP_CHANGE_TYPE,
                    BpThreatGroup.PROP_CHANGE_TYPE_REMOVED);
        } else {
            copyProperties(compendiumGroup, existingGroup, BpThreatGroup.PROP_CHANGE_TYPE,
                    BpThreatGroup.PROP_OBJECTBROWSER_CONTENT);
        }
    }

    private void afterHandleElement(CnATreeElement targetObject, CnATreeElement threatFromScope,
            CnATreeElement threatFromCompendium) {
        String linkType = BpThreat.getLinkTypeToTargetObject(targetObject.getTypeId());
        CnALink linkToTargetObject = new CnALink(threatFromScope, targetObject, linkType, "");
        daoFactory.getDAO(CnALink.class).merge(linkToTargetObject);
        for (CnALink link : threatFromCompendium.getLinksUp()) {
            if (BpRequirement.REL_BP_REQUIREMENT_BP_THREAT.equals(link.getRelationId())) {
                CnATreeElement newDependant = modelingData
                        .getScopeElementByCompendiumElement(link.getDependant());
                if (newDependant != null) {
                    CnALink newLink = new CnALink(newDependant, threatFromScope,
                            BpRequirement.REL_BP_REQUIREMENT_BP_THREAT, "");
                    daoFactory.getDAO(CnALink.class).merge(newLink);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<CnATreeElement> retrieveThreatGroupsFromModules() {
        Set<Integer> dbIds = requirementGroups.stream()
                .flatMap(module -> module.getChildren().stream())
                .flatMap(requirement -> requirement.getLinksDown().stream())
                .filter(link -> BpRequirement.REL_BP_REQUIREMENT_BP_THREAT
                        .equals(link.getId().getTypeId()))
                .map(CnALink::getDependency).map(CnATreeElement::getParent)
                .map(CnATreeElement::getDbId).collect(Collectors.toSet());
        if (dbIds.isEmpty()) {
            return Collections.emptySet();
        }
        return (Set<CnATreeElement>) daoFactory
                .getDAO(BpThreatGroup.class).findByCriteria(DetachedCriteria
                        .forClass(BpThreatGroup.class).add(Restrictions.in("dbId", dbIds)))
                .stream().collect(Collectors.toSet());
    }
}