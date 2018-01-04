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
 * Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.bp.exceptions.BpModelingException;

/**
 * This command models modules from the compendium with target objects from an
 * information group. Supported types of target objects are: IT networks,
 * business processes, other/IoT systems, ICS systems, IT systems, networks and
 * rooms.
 * 
 * Modeling process:
 * 
 * Modules / Requirements
 * 
 * The module and all requirements in the module are copied from the ITBP
 * Compendium to the information network. The group structure of the modules
 * from the compendium is retained in the information network. The modules and
 * groups are created only once per IT network.
 * 
 * Safeguards
 * 
 * If there is a safeguard for a requirement in the compendium, the safeguard is
 * copied to the information network. The group structure of the modules from
 * the compendium is retained in the information network. Safeguards and groups
 * are only created once in the IT network.
 * 
 * Elemental threats
 * 
 * If there is a elemental threats for a requirement in the compendium, the
 * threat is copied to the information network. The group structure of the
 * threats from the compendium is retained in the information network. Threats
 * and groups are only created once in the IT network.
 * 
 * Links
 * 
 * Links are generated from the target object to requirements. Links are
 * generated from the target object to all threats associated with the
 * requirements.
 * 
 * Links of modeled requirements are created according to the links in the
 * compendium. Two links are created: Requirement to safeguard and requirement
 * to elemental threat if the relevant objects exist.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelCommand extends ChangeLoggingCommand {

    private static final long serialVersionUID = -4024742735347303204L;

    private transient Logger log = Logger.getLogger(ModelCommand.class);

    private transient ModelingMetaDao metaDao;

    private Set<String> moduleUuidsFromCompendium;
    private transient Set<String> newModuleUuidsFromScope = Collections.emptySet();
    private List<String> targetUuids;
    private transient Set<BpRequirementGroup> requirementGroups;
    private transient Set<CnATreeElement> targetElements;
    private transient ItNetwork itNetwork;

    // Return values
    private String proceedingLable;

    private String stationId;

    public ModelCommand(Set<String> compendiumUuids, List<String> targetUuids) {
        super();
        this.stationId = ChangeLogEntry.STATION_ID;
        validateParameter(compendiumUuids, targetUuids);
        this.moduleUuidsFromCompendium = compendiumUuids;
        this.targetUuids = targetUuids;
    }

    @Override
    public void execute() {
        try {
            loadElements();
            handleModules();
            if (!newModuleUuidsFromScope.isEmpty()) {
                handleSafeguards();
                handleThreats();
            }
            createLinks();
            saveReturnValues();
        } catch (CommandException e) {
            getLog().error("Error while modeling.", e);
            throw new RuntimeCommandException("Error while modeling.", e);
        }
    }

    private void handleModules() throws CommandException {
        ModelModulesCommand modelModulesCommand = new ModelModulesCommand(requirementGroups,
                itNetwork);
        modelModulesCommand = getCommandService().executeCommand(modelModulesCommand);
        newModuleUuidsFromScope = modelModulesCommand.getModuleUuidsFromScope();
    }

    private void handleSafeguards() throws CommandException {
        ModelSafeguardsCommand modelSafeguardsCommand = new ModelSafeguardsCommand(
                moduleUuidsFromCompendium, getTargetScopeId());
        getCommandService().executeCommand(modelSafeguardsCommand);
    }

    private void handleThreats() throws CommandException {
        ModelThreatsCommand modelThreatsCommand = new ModelThreatsCommand(moduleUuidsFromCompendium,
                getTargetScopeId());
        getCommandService().executeCommand(modelThreatsCommand);
    }

    private void createLinks() throws CommandException {
        ModelLinksCommand modelLinksCommand = new ModelLinksCommand(moduleUuidsFromCompendium,
                newModuleUuidsFromScope, itNetwork, targetElements);
        getCommandService().executeCommand(modelLinksCommand);
    }

    private void saveReturnValues() {
        proceedingLable = itNetwork.getProceeding();
    }

    private Integer getTargetScopeId() {
        if (targetElements == null || targetElements.isEmpty()) {
            return null;
        }
        return targetElements.iterator().next().getScopeId();
    }

    private void loadElements() {
        List<CnATreeElement> elements = getMetaDao().loadElementsWithProperties(getAllUuids());
        distributeElements(new HashSet<>(elements));
        loadItNetwork();
    }

    protected void distributeElements(Collection<CnATreeElement> elements) {
        requirementGroups = new HashSet<>();
        targetElements = new HashSet<>();
        for (CnATreeElement element : elements) {
            if (moduleUuidsFromCompendium.contains(element.getUuid())
                    && element instanceof BpRequirementGroup) {
                requirementGroups.add((BpRequirementGroup) element);
            }
            if (targetUuids.contains(element.getUuid())) {
                targetElements.add(element);
            }
        }
    }

    private void loadItNetwork() {
        Integer targetScopeId = getTargetScopeId();
        CnATreeElement element = getMetaDao().loadElementWithProperties(targetScopeId);
        if (element == null) {
            throw new BpModelingException("No it network found with db id: " + targetScopeId);
        }
        if (!ItNetwork.isItNetwork(element)) {
            throw new BpModelingException("Elmenent is not an it network, db id: " + targetScopeId);
        }
        itNetwork = (ItNetwork) element;
    }

    private List<String> getAllUuids() {
        List<String> allUuids = new LinkedList<>();
        allUuids.addAll(moduleUuidsFromCompendium);
        allUuids.addAll(targetUuids);
        return allUuids;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    private void validateParameter(Set<String> compendiumUuids, List<String> targetUuids) {
        if (compendiumUuids == null) {
            throw new IllegalArgumentException("Compedium ids must not be null.");
        }
        if (targetUuids == null) {
            throw new IllegalArgumentException("Target element ids must not be null.");
        }
        if (compendiumUuids.isEmpty()) {
            throw new IllegalArgumentException("Compedium uuid list is empty.");
        }
        if (targetUuids.isEmpty()) {
            throw new IllegalArgumentException("Target element uuid list is empty.");
        }
    }

    public static boolean nullSafeEquals(String targetModuleId, String moduleId) {
        if (targetModuleId == null || moduleId == null) {
            return false;
        }
        return targetModuleId.equals(moduleId);
    }

    public String getProceedingLable() {
        return proceedingLable;
    }

    public void setProceedingLable(String proceedingLable) {
        this.proceedingLable = proceedingLable;
    }

    public ModelingMetaDao getMetaDao() {
        if (metaDao == null) {
            metaDao = new ModelingMetaDao(getDao());
        }
        return metaDao;
    }

    @Override
    public String getStationId() {
        return stationId;
    }

    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ModelCommand.class);
        }
        return log;
    }
}
