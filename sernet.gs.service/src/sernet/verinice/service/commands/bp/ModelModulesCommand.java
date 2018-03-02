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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CopyCommand;

/**
 * This command models modules (requirements groups) from the ITBP compendium
 * with certain target object types of an IT network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelModulesCommand extends ModelCopyCommand {

    private static final long serialVersionUID = -8484586873616871231L;

    private transient Set<CnATreeElement> modulesCompendium;

    public ModelModulesCommand(Set<CnATreeElement> modules, Set<CnATreeElement> targetElements) {
        super();
        this.modulesCompendium = modules;
        this.targetElements = targetElements;
    }

    protected void handleChild(CnATreeElement target, CnATreeElement elementCompendium,
            CnATreeElement elementScope) throws CommandException {
        Map<String, CnATreeElement> compendiumIdMap = getIdMapOfChildren(elementCompendium);
        Map<String, CnATreeElement> scopeIdMap = getIdMapOfChildren(elementScope);
        List<String> missingUuids = new LinkedList<>();
        for (Map.Entry<String, CnATreeElement> entry : compendiumIdMap.entrySet()) {
            if (!scopeIdMap.containsKey(entry.getKey())) {
                missingUuids.add(entry.getValue().getUuid());
            }
        }
        if (!missingUuids.isEmpty()) {
            CopyCommand copyCommand = new CopyCommand(elementScope.getUuid(), missingUuids);
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

    protected String getIdentifier(CnATreeElement element) {
        if (element instanceof BpRequirementGroup) {
            return ((BpRequirementGroup) element).getIdentifier();
        }
        if (element instanceof BpRequirement) {
            return ((BpRequirement) element).getIdentifier();
        }
        return null;
    }

    protected boolean isSuitableType(CnATreeElement e1, CnATreeElement e2) {
        return BpRequirementGroup.TYPE_ID.equals(e2.getTypeId()) && BpRequirementGroup.TYPE_ID.equals(e1.getTypeId());
    }

    public Set<CnATreeElement> getElementsFromCompendium() {
        return modulesCompendium;
    }

}
