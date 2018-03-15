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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Creates a dummy safeguard for a requirement if no safeguard is linked to
 * requirement.
 * 
 * Development of this function has begun. However, the further development was
 * interrupted. Executing of this command is disabled in ModelCommand.
 */
public class ModelDummySafeguards extends ChangeLoggingCommand {

    private static final Logger LOG = Logger.getLogger(ModelDummySafeguards.class);

    private Set<String> moduleUuidsFromScope;
    private Set<CnATreeElement> targetElements;

    private transient ModelingMetaDao metaDao;

    private String stationId;

    public ModelDummySafeguards(Set<String> moduleUuidsFromScope,
            Set<CnATreeElement> targetElements) {
        super();
        this.moduleUuidsFromScope = moduleUuidsFromScope;
        this.targetElements = targetElements;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    @Override
    public void execute() {
        for (String uuid : moduleUuidsFromScope) {
            handleModule(uuid);
        }
    }

    private void handleModule(String uuid) {
        Set<CnATreeElement> requirements = findSafeguardsByModuleUuid(uuid);
        for (CnATreeElement requirement : requirements) {
            Set<CnALink> linksToSafeguard = requirement.getLinksDown();
            if (linksToSafeguard.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No safeguards found for module with UUID: " + uuid
                            + ", will create a dummy safewguard now...");
                }
                createDummySafeguardForRequirement(requirement);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Safeguards found for module with UUID: " + uuid);
                    for (CnALink link : linksToSafeguard) {
                        LOG.debug("Link type: " + link.getRelationId());
                    }
                }
            }
        }

    }

    private void createDummySafeguardForRequirement(CnATreeElement requirement) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getStationId() {
        return stationId;
    }

    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    private Set<CnATreeElement> findSafeguardsByModuleUuid(String uuid) {
        return getMetaDao().loadLinkedElementsOfParents(Arrays.asList(uuid));
    }

    public ModelingMetaDao getMetaDao() {
        if (metaDao == null) {
            metaDao = new ModelingMetaDao(getDao());
        }
        return metaDao;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }
}
