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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bp.elements.ItNetwork;
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
 * Compendium to the information network. The module is copied and pasted as
 * child of the element.
 *
 * Safeguards (optional)
 *
 * If there is a safeguard for a requirement in the compendium, optional the
 * safeguard is copied to the information network and pasted as child of the
 * element. Optional dummy safeguards are created if no safeguard is linked to a
 * requirement in compendium.
 *
 * Elemental threats
 *
 * If there is a elemental threats for a requirement in the compendium, the
 * threat is copied to the information network. The threat is copied and pasted
 * as child of the element in a group called "Elemental threats".
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
public class ModelCommand extends GenericCommand implements IChangeLoggingCommand {

    private static final long serialVersionUID = -7021777504561600179L;

    private Set<String> moduleUuidsFromCompendium;
    private List<String> targetUuids;

    private boolean handleSafeguards = true;
    private boolean handleDummySafeguards = true;

    // Return values
    private String proceedingLabel;

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
        @SuppressWarnings("unchecked")
        Set<CnATreeElement> requirementGroups = new HashSet<>(
                getDao().findByCriteria(DetachedCriteria.forClass(CnATreeElement.class)
                        .add(Restrictions.in(CnATreeElement.UUID, moduleUuidsFromCompendium))
                        .setFetchMode("children", FetchMode.JOIN)
                        .setFetchMode("children.linksDown", FetchMode.JOIN)));
        @SuppressWarnings("unchecked")
        Set<CnATreeElement> targetElements = new HashSet<>(getDao().findByCriteria(DetachedCriteria
                .forClass(CnATreeElement.class).setFetchMode("children", FetchMode.JOIN)
                .setFetchMode("children.children", FetchMode.JOIN)
                .add(Restrictions.in(CnATreeElement.UUID, targetUuids))));
        getDao().checkRights(targetElements);
        ItNetwork itNetwork = loadItNetwork(targetElements);
        ModelingData modelingData = new ModelingData(requirementGroups, targetElements);

        handleModules(modelingData);
        if (isHandleSafeguards()) {
            handleSafeguards(modelingData);
        }
        handleThreats(modelingData);
        if (isHandleSafeguards() && isHandleDummySafeguards()) {
            createDummySafeguards(modelingData);
        }
        saveReturnValues(itNetwork);

    }

    private void handleModules(ModelingData modelingData) {
        ModelCopyTask modelModulesTask = new ModelModulesTask(getCommandService(), getDaoFactory(),
                modelingData);
        modelModulesTask.run();
    }

    private void handleSafeguards(ModelingData modelingData) {
        ModelSafeguardGroupTask modelSafeguardsTask = new ModelSafeguardGroupTask(
                getCommandService(), getDaoFactory(), modelingData);
        modelSafeguardsTask.run();
    }

    private void handleThreats(ModelingData modelingData) {
        ModelThreatGroupTask modelThreatsTask = new ModelThreatGroupTask(getCommandService(),
                getDaoFactory(), modelingData);
        modelThreatsTask.run();
    }

    private void createDummySafeguards(ModelingData modelingData) {
        ModelDummySafeguards modelDummySafeguards = new ModelDummySafeguards(getCommandService(),
                getDaoFactory(), modelingData);
        modelDummySafeguards.run();
    }

    private void saveReturnValues(ItNetwork itNetwork) {
        if (itNetwork != null && itNetwork.getProceeding() != null) {
            proceedingLabel = itNetwork.getProceeding().getLabel();
        }
    }

    private Integer getTargetScopeId(Set<CnATreeElement> targetElements) {
        if (targetElements == null || targetElements.isEmpty()) {
            return null;
        }
        return targetElements.iterator().next().getScopeId();
    }

    private ItNetwork loadItNetwork(Set<CnATreeElement> targetElements) {
        Integer targetScopeId = getTargetScopeId(targetElements);
        CnATreeElement element = getDao().retrieve(targetScopeId,
                RetrieveInfo.getPropertyInstance());
        if (element == null) {
            throw new BpModelingException("No it network found with db id: " + targetScopeId);
        }
        if (!ItNetwork.isItNetwork(element)) {
            throw new BpModelingException("Elmenent is not an it network, db id: " + targetScopeId);
        }
        return (ItNetwork) element;
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

    public String getProceedingLabel() {
        return proceedingLabel;
    }

    public void setProceedingLabel(String proceedingLable) {
        this.proceedingLabel = proceedingLable;
    }

    public boolean isHandleSafeguards() {
        return handleSafeguards;
    }

    public void setHandleSafeguards(boolean handleSafeguards) {
        this.handleSafeguards = handleSafeguards;
    }

    public boolean isHandleDummySafeguards() {
        return handleDummySafeguards;
    }

    public void setHandleDummySafeguards(boolean handleDummySafeguards) {
        this.handleDummySafeguards = handleDummySafeguards;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    @Override
    public String getStationId() {
        return stationId;
    }

    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

}
