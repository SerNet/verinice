/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
package sernet.verinice.service.commands.bp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.IIdentifiableElement;
import sernet.hui.common.connect.ITargetObject;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateElement;

/**
 * Creates a dummy safeguard for a requirement if no safeguard is linked to
 * requirement.
 */
public class ModelDummySafeguards implements Runnable {

    private static final Logger LOG = Logger.getLogger(ModelDummySafeguards.class);

    private final ICommandService commandService;
    private final IDAOFactory daoFactory;

    private final Collection<CnATreeElement> modulesFromScope;
    private final Set<CnATreeElement> safeguardGroupsFromScope;

    public ModelDummySafeguards(ICommandService commandService, IDAOFactory daoFactory,
            ModelingData modelingData) {
        this.commandService = commandService;
        this.daoFactory = daoFactory;
        this.modulesFromScope = modelingData.getModulesFromScope();
        this.safeguardGroupsFromScope = modelingData.getSafeguardGroupsFromScope();
    }

    @Override
    public void run() {
        for (CnATreeElement module : modulesFromScope) {
            try {
                handleModule(module);
            } catch (Exception e) {
                LOG.error("Error while handling module with UUID: " + module.getUuid(), e); //$NON-NLS-1$
            }
        }
    }

    private void handleModule(CnATreeElement module) throws CommandException {
        for (CnATreeElement requirement : module.getChildren()) {
            handleRequirement(requirement);
        }

    }

    private void handleRequirement(CnATreeElement requirement) throws CommandException {
        Set<CnALink> linksToSafeguard = getLinksToSafeguards(requirement);
        if (linksToSafeguard == null || linksToSafeguard.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No safeguards found for requirement with UUID: " //$NON-NLS-1$
                        + requirement.getUuid() + ", will create a dummy safewguard now..."); //$NON-NLS-1$
            }
            Safeguard safeguard = createDummySafeguardForRequirement((BpRequirement) requirement);
            requirement.getEntity().setFlag(BpRequirement.PROP_IMPLEMENTATION_DEDUCE, true);
            requirement = daoFactory.getDAO(CnATreeElement.class).merge(requirement);
            CnALink link = new CnALink(requirement, safeguard,
                    BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, "");
            daoFactory.getDAO(CnALink.class).merge(link);
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Safeguards found for requirement with UUID: " + requirement.getUuid()); //$NON-NLS-1$
            for (CnALink link : linksToSafeguard) {
                LOG.debug("Link type: " + link.getRelationId()); //$NON-NLS-1$
            }
        }
    }

    private Set<CnALink> getLinksToSafeguards(CnATreeElement requirement) {
        Set<CnALink> linksToSafeguard = requirement.getLinksDown();
        if (linksToSafeguard != null && !linksToSafeguard.isEmpty()) {
            linksToSafeguard = filterLinks(linksToSafeguard);
        }
        return linksToSafeguard;
    }

    private Set<CnALink> filterLinks(Set<CnALink> links) {
        Set<CnALink> linksToSafeguard = new HashSet<>(links.size());
        for (CnALink link : links) {
            if (BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(link.getRelationId())) {
                linksToSafeguard.add(link);
            }
        }
        return linksToSafeguard;
    }

    private Safeguard createDummySafeguardForRequirement(BpRequirement requirement)
            throws CommandException {
        CnATreeElement element = loadTargetElement(requirement);
        BpRequirementGroup requirementGroup = (BpRequirementGroup) requirement.getParent();
        String moduleTitle = requirementGroup.getFullTitle();
        CnATreeElement safeguardGroup = getSafeguardGroup(element, moduleTitle);
        if (safeguardGroup == null) {
            safeguardGroup = createSafeguardGroup(element, requirementGroup);
        }
        return createSafeguard(requirement, safeguardGroup);
    }

    private Safeguard createSafeguard(BpRequirement requirement, CnATreeElement safeguardGroup)
            throws CommandException {
        CreateElement<Safeguard> createElement = new CreateElement<>(safeguardGroup,
                Safeguard.class,
                requirement.getTitle() + " [" + Messages.getString("ModelDummySafeguards.6") + "]", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                true, true);
        createElement = commandService.executeCommand(createElement);
        Safeguard safeguard = createElement.getNewElement();
        safeguard.setIdentifier(getSafeguardForRequirementIdentifier(requirement.getIdentifier()));
        safeguard.setSecurityLevel(requirement.getSecurityLevel());
        safeguard = daoFactory.getDAO(Safeguard.class).merge(safeguard);
        return safeguard;
    }

    private CnATreeElement createSafeguardGroup(CnATreeElement parent,
            BpRequirementGroup requirementGroup) throws CommandException {
        CreateElement<SafeguardGroup> createElement = new CreateElement<>(parent,
                SafeguardGroup.class, requirementGroup.getTitle(), true, true);
        createElement = commandService.executeCommand(createElement);
        SafeguardGroup safeguardGroup = createElement.getNewElement();
        safeguardGroup.setIdentifier(requirementGroup.getIdentifier());
        return daoFactory.getDAO(SafeguardGroup.class).merge(safeguardGroup);
    }

    private CnATreeElement getSafeguardGroup(CnATreeElement element, String fullTitle) {
        CnATreeElement safeguardGroup = null;
        Set<CnATreeElement> children = element.getChildren();
        safeguardGroup = getSafeguardGroup(fullTitle, children);
        if (safeguardGroup == null && !safeguardGroupsFromScope.isEmpty()) {
            safeguardGroup = getSafeguardGroup(fullTitle, safeguardGroupsFromScope);
        }
        return safeguardGroup;
    }

    private CnATreeElement getSafeguardGroup(String fullTitle,
            Collection<CnATreeElement> allSafeguardGroups) {
        CnATreeElement safeguardGroup = null;
        for (CnATreeElement group : allSafeguardGroups) {
            if (SafeguardGroup.TYPE_ID.equals(group.getTypeId())
                    && fullTitle.equals(((IIdentifiableElement) group).getFullTitle())) {
                safeguardGroup = group;
            }
        }
        return safeguardGroup;
    }

    private CnATreeElement loadTargetElement(CnATreeElement requirement) {
        // Find target element
        CnATreeElement element = null;
        Set<CnALink> links = requirement.getLinksDown();
        for (CnALink link : links) {
            if (link.getDependency() instanceof ITargetObject) {
                element = link.getDependency();
            }
        }
        return element;
    }

    private String getSafeguardForRequirementIdentifier(String identifier) {
        return identifier.replace(".A", ".M"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
