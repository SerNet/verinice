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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CnATypeMapper;
import sernet.verinice.service.commands.CopyCommand;

/**
 * Abstract base class for modeling modules and safeguard groups. The modules
 * and safeguard groups are copied and pasted as children of the elements.
 */
public abstract class ModelCopyTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(ModelCopyTask.class);
    private static final Pattern RELEASE_PATTERN = Pattern.compile("(\\d{4})-(\\d+)");
    private static final String TITLE_REMOVED = "ENTFALLEN";

    protected final ICommandService commandService;
    protected final IDAOFactory daoFactory;

    protected final ModelingData modelingData;
    private final String handledGroupTypeId;
    private final String elementTypeId;
    private final Set<CnATreeElement> targetElements;
    private final Predicate<CnATreeElement> elementFilter;

    private final String elementReleaseProperty;
    private final String groupReleaseProperty;

    // key: element from compendium, value: element from scope
    private Map<CnATreeElement, CnATreeElement> existingGroupsByCompendiumGroup;

    public ModelCopyTask(ICommandService commandService, IDAOFactory daoFactory,
            ModelingData modelingData, String handledGroupTypeId,
            Predicate<CnATreeElement> elementFilter, String elementReleaseProperty,
            String groupReleaseProperty) {
        this.commandService = commandService;
        this.daoFactory = daoFactory;
        this.modelingData = modelingData;
        this.handledGroupTypeId = handledGroupTypeId;
        this.elementFilter = elementFilter;
        this.elementReleaseProperty = elementReleaseProperty;
        this.groupReleaseProperty = groupReleaseProperty;
        this.elementTypeId = CnATypeMapper.getElementTypeIdFromGroupTypeId(handledGroupTypeId);
        this.targetElements = modelingData.getTargetElements();

    }

    @Override
    public void run() {
        try {
            for (CnATreeElement target : targetElements) {
                copyMissingGroups(target);
                for (Map.Entry<CnATreeElement, CnATreeElement> entry : existingGroupsByCompendiumGroup
                        .entrySet()) {
                    handleExistingGroup(entry.getKey(), entry.getValue());
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

    protected abstract void afterCopyElement(CnATreeElement targetObject, CnATreeElement newElement,
            CnATreeElement compendiumElement);

    protected abstract void afterSkipExistingElement(CnATreeElement targetObject,
            CnATreeElement existingElement, CnATreeElement compendiumElement);

    protected abstract void updateExistingGroup(CnATreeElement targetObject,
            CnATreeElement existingGroup, CnATreeElement compendiumGroup, boolean group);

    protected abstract void updateExistingElement(CnATreeElement targetObject,
            CnATreeElement existingElement, CnATreeElement compendiumElement,
            boolean elementRemoved);

    protected void handleExistingGroup(CnATreeElement groupFromCompendium,
            CnATreeElement groupFromScope) throws CommandException {
        String scopeGroupRelease = groupFromScope.getEntity()
                .getRawPropertyValue(groupReleaseProperty);
        String compendiumGroupRelease = groupFromCompendium.getEntity()
                .getRawPropertyValue(groupReleaseProperty);
        boolean writeGroupPermissionChecked = false;
        if (canUpdateFrom(scopeGroupRelease, compendiumGroupRelease)) {
            boolean elementRemoved = isElementRemoved(groupFromCompendium);
            daoFactory.getDAO(CnATreeElement.class).checkRights(groupFromScope);
            writeGroupPermissionChecked = true;
            updateExistingGroup(groupFromScope.getParent(), groupFromScope, groupFromCompendium,
                    elementRemoved);
        }

        Map<String, CnATreeElement> compendiumElementsByIdentifier = getIdMapOfChildren(
                groupFromCompendium);
        Map<String, CnATreeElement> scopeelementyByIdentifier = getIdMapOfChildren(groupFromScope);
        List<CnATreeElement> missingElements = new LinkedList<>();
        for (Map.Entry<String, CnATreeElement> entry : compendiumElementsByIdentifier.entrySet()) {
            CnATreeElement compendiumElement = entry.getValue();
            if (!scopeelementyByIdentifier.containsKey(entry.getKey())) {
                if (!isElementRemoved(compendiumElement)
                        && (elementFilter == null || elementFilter.test(compendiumElement))) {
                    missingElements.add(compendiumElement);
                }
            } else {
                CnATreeElement scopeElement = scopeelementyByIdentifier.get(entry.getKey());
                modelingData.addMappingForExistingElement(compendiumElement, scopeElement);
                String scopeElementRelease = scopeElement.getEntity()
                        .getRawPropertyValue(elementReleaseProperty);
                String compendiumElementRelease = compendiumElement.getEntity()
                        .getRawPropertyValue(elementReleaseProperty);

                if (shouldUpdate(scopeElement, compendiumElement)
                        && canUpdateFrom(scopeElementRelease, compendiumElementRelease)) {
                    boolean elementRemoved = isElementRemoved(compendiumElement);
                    daoFactory.getDAO(CnATreeElement.class).checkRights(scopeElement);
                    updateExistingElement(groupFromScope.getParent(), scopeElement,
                            compendiumElement, elementRemoved);
                } else {
                    afterSkipExistingElement(groupFromScope.getParent(), scopeElement,
                            compendiumElement);
                }
            }
        }
        if (!missingElements.isEmpty()) {
            if (!writeGroupPermissionChecked) {
                daoFactory.getDAO(CnATreeElement.class).checkRights(groupFromScope);
            }
            CopyCommand copyCommand = new ModelingCopyCommand(
                    groupFromScope.getParent(), groupFromScope.getUuid(), missingElements.stream()
                            .map(CnATreeElement::getUuid).collect(Collectors.toList()),
                    modelingData);
            commandService.executeCommand(copyCommand);
        }
    }

    protected boolean shouldUpdate(CnATreeElement scopeElement, CnATreeElement compendiumElement) {
        return true;
    }

    private static boolean isElementRemoved(CnATreeElement compendiumElement) {
        return TITLE_REMOVED.equals(compendiumElement.getTitle());
    }

    /**
     * Checks whether an update from one version to another is possible.<br>
     * An update is possible if the compendium release is higher and does not
     * skip major versions (i.e. years).<br>
     * Supported:
     * <ul>
     * <li>2019-0 -> 2019-5
     * <li>2019-0 -> 2020-0
     * <li>2019-1 -> 2020-0
     * <li>2019-0 -> 2020-3
     *
     * </ul>
     * Unsupported:
     * <ul>
     * <li>2019-0 -> 2018-0
     * <li>2019-0 -> 2019-0
     * <li>2019-0 -> 2021-0
     * </ul>
     *
     * @param scopeElementRelease
     *            the scope element's release version
     * @param compendiumElementRelease
     *            the compendium element's release version
     * @return <code>true</code> if the update is supported, <code>false</code>
     *         otherwise
     */
    static boolean canUpdateFrom(String scopeElementRelease, String compendiumElementRelease) {
        if (StringUtils.isEmpty(scopeElementRelease)
                || StringUtils.isEmpty(compendiumElementRelease)) {
            return false;
        }
        Matcher scopeReleaseMatcher = RELEASE_PATTERN.matcher(scopeElementRelease);
        Matcher compendiumReleaseMatcher = RELEASE_PATTERN.matcher(compendiumElementRelease);
        if (!scopeReleaseMatcher.matches() || !compendiumReleaseMatcher.matches()) {
            return false;
        }
        int scopeYearParsed = Integer.parseInt(scopeReleaseMatcher.group(1));
        int compendiumYearParsed = Integer.parseInt(compendiumReleaseMatcher.group(1));
        if (compendiumYearParsed < scopeYearParsed || compendiumYearParsed > scopeYearParsed + 1) {
            return false;
        }
        return compendiumYearParsed > scopeYearParsed
                || Integer.parseInt(compendiumReleaseMatcher.group(2)) > Integer
                        .parseInt(scopeReleaseMatcher.group(2));
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
            List<String> missingGroupUuids = missingGroups.stream().map(CnATreeElement::getUuid)
                    .collect(Collectors.toList());
            CopyCommand copyCommand = new ModelingCopyCommand(target, target.getUuid(),
                    missingGroupUuids, modelingData);
            commandService.executeCommand(copyCommand);
        }
    }

    private List<CnATreeElement> getMissingGroups(CnATreeElement targetWithChildren) {
        List<CnATreeElement> missingGroups = new LinkedList<>();
        Set<CnATreeElement> targetChildren = targetWithChildren.getChildren();
        existingGroupsByCompendiumGroup = new HashMap<>(targetChildren.size());
        for (CnATreeElement group : getGroupsFromCompendium()) {
            CnATreeElement existingGroup = getElementFromChildren(targetChildren, group);
            if (existingGroup == null) {
                if (!isElementRemoved(group)) {
                    missingGroups.add(group);
                }
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

    protected void logElements(Collection<?> collection) {
        for (Object element : collection) {
            LOG.debug(element);
        }

    }

    protected void copyProperties(CnATreeElement compendiumElement, CnATreeElement existingElement,
            String... propertyIds) {
        Map<String, PropertyList> compendiumElementPropertyLists = compendiumElement.getEntity()
                .getTypedPropertyLists();
        Map<String, PropertyList> existingElementPropertyLists = existingElement.getEntity()
                .getTypedPropertyLists();

        for (String propertyId : propertyIds) {
            PropertyList compendiumElementPropertyList = compendiumElementPropertyLists
                    .get(propertyId);
            PropertyList existingElementPropertyList = existingElementPropertyLists.get(propertyId);
            if (compendiumElementPropertyList != null) {
                if (existingElementPropertyList == null) {
                    existingElementPropertyList = new PropertyList(
                            compendiumElementPropertyList.getProperties().size());
                    existingElementPropertyLists.put(propertyId, existingElementPropertyList);
                } else {
                    existingElementPropertyList.getProperties().clear();
                }
                for (Property property : compendiumElementPropertyList.getProperties()) {
                    Property newProperty = property.copy(existingElement.getEntity());
                    existingElementPropertyList.add(newProperty);
                }
            } else if (existingElementPropertyList != null) {
                existingElementPropertyLists.remove(propertyId);
            }
        }
    }

    private final class ModelingCopyCommand extends CopyCommand {
        private static final long serialVersionUID = 6836616806403844422L;

        private transient ModelingData modelingData;

        private transient CnATreeElement targetElement;

        private ModelingCopyCommand(CnATreeElement targetElement, String uuidGroup,
                List<String> uuidList, ModelingData modelingData) {
            super(uuidGroup, uuidList, Collections.emptyList());
            this.targetElement = targetElement;
            this.modelingData = modelingData;
        }

        @Override
        protected void afterCopy(CnATreeElement original, CnATreeElement copy) {
            modelingData.addMappingForNewElement(original, copy);
            if (elementTypeId.equals(copy.getTypeId())) {
                afterCopyElement(targetElement, copy, original);
            }
        }

        @Override
        protected boolean copyDescendant(CnATreeElement descendant, CnATreeElement groupToCopyTo) {
            return !isElementRemoved(descendant)
                    && (elementFilter == null || elementFilter.test(descendant));
        }
    }

}
