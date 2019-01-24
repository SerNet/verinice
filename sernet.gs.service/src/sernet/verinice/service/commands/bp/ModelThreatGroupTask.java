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

import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.groups.BpThreatGroup;
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

    private final Set<String> moduleUuids;

    private Set<CnATreeElement> threatGroupsFromCompendium;

    public ModelThreatGroupTask(ModelingMetaDao modelingMetaDao, ICommandService commandService,
            IDAOFactory daoFactory, ModelingData modelingData) {
        super(modelingMetaDao, commandService, daoFactory, modelingData, BpThreatGroup.TYPE_ID);
        this.moduleUuids = modelingData.getModuleUuidsFromCompendium();
    }

    @Override
    public Set<CnATreeElement> getGroupsFromCompendium() {
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

    private void loadCompendiumThreatGroups() {
        threatGroupsFromCompendium = new HashSet<>(loadThreatGroupsByModuleUuids());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Threat groups linked to modules: ");
            logElements(threatGroupsFromCompendium);
        }
    }

    private List<CnATreeElement> loadThreatGroupsByModuleUuids() {
        return metaDao.loadChildrenLinksParents(moduleUuids, BpThreatGroup.TYPE_ID);
    }

}
