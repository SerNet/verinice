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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command models threat groups from the ITBP compendium with certain
 * target object types of an IT network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelThreatGroupCommand extends ModelCopyCommand {

    private static final long serialVersionUID = 1220698466409148043L;

    private static final Logger LOG = Logger.getLogger(ModelSafeguardGroupCommand.class);

    private Set<String> moduleUuids;
    private transient Set<CnATreeElement> threatGroupsFromCompendium;

    public ModelThreatGroupCommand(Set<String> moduleUuids, Set<CnATreeElement> targetElements) {
        super();
        this.moduleUuids = moduleUuids;
        this.targetElements = targetElements;
    }

    @Override
    public Set<CnATreeElement> getElementsFromCompendium() {
        if (threatGroupsFromCompendium == null) {
            loadCompendiumThreatGroups();
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
        return null;
    }

    @Override
    protected boolean isSuitableType(CnATreeElement e1, CnATreeElement e2) {
        return BpThreatGroup.TYPE_ID.equals(e2.getTypeId())
                && BpThreatGroup.TYPE_ID.equals(e1.getTypeId());
    }

    private void loadCompendiumThreatGroups() {
        threatGroupsFromCompendium = new HashSet<>(loadThreatGroupsByModuleUuids());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Threat groups linked to modules: ");
            logElements(threatGroupsFromCompendium);
        }
    }

    private List<CnATreeElement> loadThreatGroupsByModuleUuids() {
        return getMetaDao().loadChildrenLinksParents(moduleUuids, BpThreatGroup.TYPE_ID_HIBERNATE);
    }

}
