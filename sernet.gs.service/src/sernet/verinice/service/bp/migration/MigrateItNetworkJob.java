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
package sernet.verinice.service.bp.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Migrates the requirements, safeguards and threats of all elements in an IT
 * network. See ModelingMigrationServiceImpl for more details.
 */
public class MigrateItNetworkJob {

    private static final Logger log = Logger.getLogger(MigrateItNetworkJob.class);

    private static final String[] TARGET_ELEMENT_TYPE_IDS = { Application.TYPE_ID,
            BusinessProcess.TYPE_ID,
            Device.TYPE_ID, IcsSystem.TYPE_ID, ItNetwork.TYPE_ID, ItSystem.TYPE_ID, Network.TYPE_ID,
            Room.TYPE_ID };

    private static final String[] COMPENDIUM_ELEMENTS_TYPE_IDS = { BpRequirement.TYPE_ID,
            Safeguard.TYPE_ID, BpThreat.TYPE_ID };

    private static final String[] COMPENDIUM_ELEMENT_GROUPS_TYPE_IDS = { BpRequirementGroup.TYPE_ID,
            SafeguardGroup.TYPE_ID, BpThreatGroup.TYPE_ID };

    private Integer itNetworkDbId;

    private Set<CnATreeElement> elementsToDelete;

    private IGraphService graphService;
    private ICommandService commandService;
    private IBaseDao<CnATreeElement, Serializable> elementDao;
    private ObjectFactory migrateElementJobFactory;

    public MigrateItNetworkJob() {
        super();
        this.elementsToDelete = new HashSet<>();
    }

    public void migrateModeling() {
        if (log.isInfoEnabled()) {
            log.info("Migrating modeling of IT network, DB ID: " + itNetworkDbId + "...");
        }
        VeriniceGraph veriniceGraph = createGraph();
        if (log.isDebugEnabled()) {
            log.debug("Graph loaded, number of elements in graph: "
                    + veriniceGraph.getElements().size());
        }
        List<CnATreeElement> targetElements = getTargetElements(veriniceGraph);
        int numberOfElements = targetElements.size();
        int numberProcessed = 0;
        if (log.isInfoEnabled()) {
            log.info("Migrating " + numberOfElements + " elements...");
        }
        for (CnATreeElement element : targetElements) {
            MigrateElementJob elementJob = (MigrateElementJob) migrateElementJobFactory
                    .getObject();
            elementJob.migrateModeling(element, veriniceGraph);
            elementsToDelete.addAll(elementJob.getElementsToDelete());
            numberProcessed++;
            if (log.isInfoEnabled()) {
                log.info(numberProcessed + "/" + numberOfElements + " processed: " + element);
            }
        }
        deleteGlobalCompendiumElements();
    }

    private List<CnATreeElement> getTargetElements(VeriniceGraph veriniceGraph) {
        List<CnATreeElement> targetElements = new LinkedList<>();
        for (String typeId : TARGET_ELEMENT_TYPE_IDS) {
            targetElements.addAll(veriniceGraph.getElements(typeId));
        }
        return targetElements;
    }

    private VeriniceGraph createGraph() {
        IGraphElementLoader loader = new GraphElementLoader();
        loader.setScopeId(itNetworkDbId);
        loader.setTypeIds(getAllTypeIds());
        return getGraphService().create(Collections.singletonList(loader));
    }

    private void deleteGlobalCompendiumElements() {
        if (log.isInfoEnabled()) {
            log.info("Deleting " + elementsToDelete.size() + " elements...");
        }
        for (CnATreeElement element : elementsToDelete) {
            getElementDao().delete(element);
        }
        if (log.isDebugEnabled()) {
            log.debug("Deleting finished");
        }
    }

    private String[] getAllTypeIds() {
        ArrayList<String> allIds = new ArrayList<>(TARGET_ELEMENT_TYPE_IDS.length
                + COMPENDIUM_ELEMENTS_TYPE_IDS.length + COMPENDIUM_ELEMENT_GROUPS_TYPE_IDS.length);
        allIds.addAll(Arrays.asList(TARGET_ELEMENT_TYPE_IDS));
        allIds.addAll(Arrays.asList(COMPENDIUM_ELEMENTS_TYPE_IDS));
        allIds.addAll(Arrays.asList(COMPENDIUM_ELEMENT_GROUPS_TYPE_IDS));
        return allIds.toArray(new String[allIds.size()]);
    }

    public void setItNetworkDbId(Integer itNetworkDbId) {
        this.itNetworkDbId = itNetworkDbId;
    }

    public IGraphService getGraphService() {
        return graphService;
    }

    public void setGraphService(IGraphService graphService) {
        this.graphService = graphService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public void setMigrateElementJobFactory(ObjectFactory migrateElementJobJobFactory) {
        this.migrateElementJobFactory = migrateElementJobJobFactory;
    }

    public IBaseDao<CnATreeElement, Serializable> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Serializable> elementDao) {
        this.elementDao = elementDao;
    }

}
