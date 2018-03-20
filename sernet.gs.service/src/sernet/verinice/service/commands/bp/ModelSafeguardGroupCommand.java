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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command models modules (requirements groups) from the ITBP compendium
 * with certain target object types of an IT network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelSafeguardGroupCommand extends ModelCopyCommand {

    private static final long serialVersionUID = 4781171353692785642L;

    private static final Logger LOG = Logger.getLogger(ModelSafeguardGroupCommand.class);

    private Set<String> moduleUuids;
    private transient Set<CnATreeElement> safeguardGroupsFromCompendium;
    private ItNetwork itNetwork;

    public ModelSafeguardGroupCommand(Set<String> moduleUuids, Set<CnATreeElement> targetElements,
            ItNetwork itNetwork) {
        super();
        this.moduleUuids = moduleUuids;
        this.targetElements = targetElements;
        this.itNetwork = itNetwork;
    }

    @Override
    public Set<CnATreeElement> getElementsFromCompendium() {
        if (safeguardGroupsFromCompendium == null) {
            loadCompendiumSafeguardGroups();
        }
        validateSafeguardGroups(safeguardGroupsFromCompendium);
        return safeguardGroupsFromCompendium;
    }

    private void validateSafeguardGroups(Set<CnATreeElement> safeguardGroups) {
        for (CnATreeElement group : safeguardGroups) {
            Set<CnATreeElement> safeguards = group.getChildren();
            Set<CnATreeElement> validSafeguards = new HashSet<>(safeguards.size());
            for (CnATreeElement safeguard : safeguards) {
                if (ModelingValidator.isSafeguardValidInItNetwork(safeguard, itNetwork)) {
                    validSafeguards.add(safeguard);
                }
            }
            group.setChildren(validSafeguards);
        }
    }

    @Override
    protected String getIdentifier(CnATreeElement element) {
        if (element instanceof Safeguard) {
            return ((Safeguard) element).getIdentifier();
        }
        if (element instanceof SafeguardGroup) {
            return ((SafeguardGroup) element).getTitle();
        }
        return null;
    }

    @Override
    protected boolean isSuitableType(CnATreeElement e1, CnATreeElement e2) {
        return SafeguardGroup.TYPE_ID.equals(e2.getTypeId())
                && SafeguardGroup.TYPE_ID.equals(e1.getTypeId());
    }

    private void loadCompendiumSafeguardGroups() {
        safeguardGroupsFromCompendium = new HashSet<>(loadSafeguardGroupsByModuleUuids());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Safeguards linked to modules: ");
            logElements(safeguardGroupsFromCompendium);
        }
    }

    private List<CnATreeElement> loadSafeguardGroupsByModuleUuids() {
        return getMetaDao().loadChildrenLinksParents(moduleUuids, SafeguardGroup.TYPE_ID);
    }



}
