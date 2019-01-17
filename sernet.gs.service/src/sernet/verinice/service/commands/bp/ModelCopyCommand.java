/*******************************************************************************
 * Copyright (c) 2018 <Vorname> <Nachname>.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IPostProcessor;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CopyCommand;

/**
 * Abstract base class for modeling modules and safeguard groups. The modules
 * and safeguard groups are copied and pasted as childs of the elements.
 */
public abstract class ModelCopyCommand extends ChangeLoggingCommand {

    private static final long serialVersionUID = -17533077608768778L;

    private static final Logger LOG = Logger.getLogger(ModelCopyCommand.class);

    private String stationId;
    private transient ModelingMetaDao metaDao;

    private Set<CnATreeElement> targetElements;
    // key: element from compendium, value: element from scope
    private Map<CnATreeElement, CnATreeElement> existingGroupsByCompendiumGroup;

    private List<IPostProcessor> copyPostProcessors;

    private final String handledGroupTypeId;

    protected transient ModelingData modelingData;

    public ModelCopyCommand(ModelingMetaDao modelingMetaDao, ModelingData modelingData,
            String handledGroupTypeId, IPostProcessor... elementCopyPostProcessors) {
        this.metaDao = modelingMetaDao;

        this.modelingData = modelingData;
        this.targetElements = modelingData.getTargetElements();
        this.handledGroupTypeId = handledGroupTypeId;
        this.copyPostProcessors = Arrays.asList(elementCopyPostProcessors);
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    @Override
    public String getStationId() {
        return stationId;
    }

    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    @Override
    public void execute() {
        try {
            for (CnATreeElement target : targetElements) {
                copyMissingGroups(target);
                for (Map.Entry<CnATreeElement, CnATreeElement> entry : existingGroupsByCompendiumGroup
                        .entrySet()) {
                    loadAndHandleExistingGroup(entry.getKey(), entry.getValue());
                    modelingData.addMappingForExistingElement(entry.getKey(), entry.getValue());
                }
            }
        } catch (CommandException e) {
            LOG.error("Error while modeling.", e);
            throw new RuntimeCommandException("Error while modeling.", e);
        }
    }

    protected abstract Set<CnATreeElement> getGroupsFromCompendium();

    private boolean isSuitableType(CnATreeElement e1, CnATreeElement e2) {
        return handledGroupTypeId.equals(e2.getTypeId())
                && handledGroupTypeId.equals(e1.getTypeId());
    }

    protected abstract String getIdentifier(CnATreeElement element);

    protected void handleExistingGroup(CnATreeElement groupFromCompendium,
            CnATreeElement groupFromScope) throws CommandException {
        Map<String, CnATreeElement> compendiumElementsByIdentifier = getIdMapOfChildren(
                groupFromCompendium);
        Map<String, CnATreeElement> scopeelementyByIdentifier = getIdMapOfChildren(groupFromScope);
        List<CnATreeElement> missingElements = new LinkedList<>();
        for (Map.Entry<String, CnATreeElement> entry : compendiumElementsByIdentifier.entrySet()) {
            CnATreeElement compendiumElement = entry.getValue();
            if (!scopeelementyByIdentifier.containsKey(entry.getKey())) {
                missingElements.add(compendiumElement);
            } else {
                CnATreeElement scopeElement = scopeelementyByIdentifier.get(entry.getKey());
                modelingData.addMappingForExistingElement(compendiumElement, scopeElement);
            }
        }
        if (!missingElements.isEmpty()) {

            CopyCommand copyCommand = new ModelingCopyCommand(groupFromScope.getUuid(),
                    missingElements.stream().map(CnATreeElement::getUuid)
                            .collect(Collectors.toList()),
                    copyPostProcessors, modelingData);
            getCommandService().executeCommand(copyCommand);
        }
    }

    private Map<String, CnATreeElement> getIdMapOfChildren(CnATreeElement element) {
        Map<String, CnATreeElement> idMap = new HashMap<>();
        for (CnATreeElement child : element.getChildren()) {
            idMap.put(getIdentifier(child), child);
        }
        return idMap;
    }

    private void copyMissingGroups(CnATreeElement target) throws CommandException {
        List<CnATreeElement> missingGroups = getMissingGroups(target);
        if (!missingGroups.isEmpty()) {
            CopyCommand copyCommand = new CopyCommand(target.getUuid(), missingGroups.stream()
                    .map(CnATreeElement::getUuid).collect(Collectors.toList()));
            copyCommand = getCommandService().executeCommand(copyCommand);
            List<String> newGroupUuids = copyCommand.getNewElements();
            @SuppressWarnings("unchecked")
            List<CnATreeElement> newGroups = getDaoFactory().getDAO(handledGroupTypeId)
                    .findByCriteria(DetachedCriteria.forClass(CnATreeElement.class)
                            .add(Restrictions.in(CnATreeElement.UUID, newGroupUuids)));
            if (newGroups.size() != missingGroups.size()) {
                throw new IllegalStateException("Expected " + missingGroups.size()
                        + " elements to be copied but got " + newGroups.size());
            }
            for (int i = 0; i < newGroupUuids.size(); i++) {
                modelingData.addMappingForNewElement(missingGroups.get(i), newGroups.get(i));
            }
        }
    }

    private void loadAndHandleExistingGroup(CnATreeElement groupFromCompendium,
            CnATreeElement groupFromScope) throws CommandException {
        String uuidCompendium = groupFromCompendium.getUuid();
        String uuidScope = groupFromScope.getUuid();
        CnATreeElement elementCompendium = null;
        CnATreeElement elementScope = null;
        Set<CnATreeElement> elementsWithChildren = new HashSet<>(getMetaDao()
                .loadElementsWithChildrenProperties(Arrays.asList(uuidCompendium, uuidScope)));
        for (CnATreeElement element : elementsWithChildren) {
            if (element.getUuid().equals(uuidCompendium)) {
                elementCompendium = element;
            }
            if (element.getUuid().equals(uuidScope)) {
                elementScope = element;
            }
        }
        handleExistingGroup(elementCompendium, elementScope);
    }

    private List<CnATreeElement> getMissingGroups(CnATreeElement targetWithChildren) {
        List<CnATreeElement> missingGroups = new LinkedList<>();
        Set<CnATreeElement> targetChildren = targetWithChildren.getChildren();
        existingGroupsByCompendiumGroup = new HashMap<>(targetChildren.size());
        for (CnATreeElement group : getGroupsFromCompendium()) {
            CnATreeElement existingGroup = getElementFromChildren(targetChildren, group);
            if (existingGroup == null) {
                missingGroups.add(group);
            } else if (isSuitableType(existingGroup, group)) {
                existingGroupsByCompendiumGroup.put(group, existingGroup);
            }
        }
        return missingGroups;
    }

    /**
     * @param targetChildren
     *            A set of child elements
     * @param element
     *            An element
     * @return The element with the same identifier with element from set
     */
    private CnATreeElement getElementFromChildren(Set<CnATreeElement> targetChildren,
            CnATreeElement element) {
        for (CnATreeElement child : targetChildren) {
            if (isEqual(element, child)) {
                return child;
            }
        }
        return null;
    }

    private boolean isEqual(CnATreeElement e1, CnATreeElement e2) {
        return isSuitableType(e1, e2)
                && ModelCommand.nullSafeEquals(getIdentifier(e1), getIdentifier(e2));
    }

    protected ModelingMetaDao getMetaDao() {
        if (metaDao == null) {
            metaDao = new ModelingMetaDao(getDao());
        }
        return metaDao;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    protected void logElements(Collection<?> collection) {
        for (Object element : collection) {
            LOG.debug(element);
        }

    }

    private static final class ModelingCopyCommand extends CopyCommand {
        private static final long serialVersionUID = 6836616806403844422L;

        private transient ModelingData modelingData;

        private ModelingCopyCommand(String uuidGroup, List<String> uuidList,
                List<IPostProcessor> postProcessorList, ModelingData modelingData) {
            super(uuidGroup, uuidList, postProcessorList);
            this.modelingData = modelingData;
        }

        @Override
        protected void afterCopy(CnATreeElement original, CnATreeElement copy) {
            modelingData.addMappingForNewElement(original, copy);
        }
    }

}
