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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CopyCommand;

/**
 * Abstract base class for modeling modules and safeguard groups. The modules
 * and safeguard groups are copied and pasted as childs of the elements.
 */
public abstract class ModelCopyCommand extends ChangeLoggingCommand {

    private static final long serialVersionUID = 5344935376238004516L;

    private static final Logger LOG = Logger.getLogger(ModelCopyCommand.class);

    private String stationId;
    private transient ModelingMetaDao metaDao;

    protected Set<CnATreeElement> targetElements;
    protected Set<String> newModuleUuids = Collections.emptySet();
    // key: element from compendium, value: element from scope
    protected Map<CnATreeElement, CnATreeElement> elementMap;

    public ModelCopyCommand() {
        super();
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
            handleElement();
        } catch (CommandException e) {
            LOG.error("Error while modeling.", e);
            throw new RuntimeCommandException("Error while modeling.", e);
        }
    }

    protected abstract Set<CnATreeElement> getElementsFromCompendium();

    protected abstract void handleChild(CnATreeElement target, CnATreeElement elementCompendium,
            CnATreeElement elementScope) throws CommandException;

    protected abstract boolean isSuitableType(CnATreeElement e1, CnATreeElement e2);

    protected abstract String getIdentifier(CnATreeElement element);

    private void handleElement() throws CommandException {
        for (CnATreeElement target : targetElements) {
            copyMissingElements(target);
            handleChildren(target);
        }
    }

    private void copyMissingElements(CnATreeElement target) throws CommandException {
        List<String> missingUuids = createListOfMissingUuids(target);
        if (!missingUuids.isEmpty()) {
            CopyCommand copyCommand = new CopyCommand(target.getUuid(), missingUuids);
            copyCommand = getCommandService().executeCommand(copyCommand);
            newModuleUuids = new HashSet<>(copyCommand.getNewElements());
        }
    }

    private void handleChildren(CnATreeElement target) throws CommandException {
        for (Map.Entry<CnATreeElement, CnATreeElement> entry : elementMap.entrySet()) {
            loadAndHandleChild(target, entry);
        }
    }

    private void loadAndHandleChild(CnATreeElement target,
            Map.Entry<CnATreeElement, CnATreeElement> entry) throws CommandException {
        String uuidCompendium = entry.getKey().getUuid();
        String uuidScope = entry.getValue().getUuid();
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
        handleChild(target, elementCompendium, elementScope);
    }

    private List<String> createListOfMissingUuids(CnATreeElement targetWithChildren) {
        List<String> uuids = new LinkedList<>();
        Set<CnATreeElement> targetChildren = targetWithChildren.getChildren();
        elementMap = new HashMap<>(targetChildren.size());
        for (CnATreeElement module : getElementsFromCompendium()) {
            CnATreeElement existingElement = getElementFromChildren(targetChildren, module);
            if (existingElement == null) {
                uuids.add(module.getUuid());
            } else if (isSuitableType(existingElement, module)) {
                elementMap.put(module, existingElement);
            }
        }
        return uuids;
    }

    /**
     * @param targetChildren
     *            A set of child elements
     * @param element
     *            An element
     * @return The element with the same identifier with element from set
     */
    protected CnATreeElement getElementFromChildren(Set<CnATreeElement> targetChildren,
            CnATreeElement element) {
        for (CnATreeElement child : targetChildren) {
            if (isEqual(element, child)) {
                return child;
            }
        }
        return null;
    }

    protected boolean isEqual(CnATreeElement e1, CnATreeElement e2) {
        boolean equals = false;
        if (isSuitableType(e1, e2)
                && ModelCommand.nullSafeEquals(getIdentifier(e1), getIdentifier(e2))) {
            equals = true;
        }
        return equals;
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

    public Set<CnATreeElement> getTargetElements() {
        return targetElements;
    }

    public Set<String> getNewModuleUuids() {
        return newModuleUuids;
    }

    public Set<String> getModuleUuidsFromScope() {
        Set<String> uuids = new HashSet<>(newModuleUuids);
        for (CnATreeElement element : elementMap.values()) {
            uuids.add(element.getUuid());
        }
        return uuids;
    }

    protected void logElements(Collection<?> collection) {
        for (Object element : collection) {
            LOG.debug(element);
        }

    }

}
