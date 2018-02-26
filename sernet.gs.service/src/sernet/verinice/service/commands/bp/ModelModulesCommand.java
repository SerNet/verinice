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

import java.util.Set;

import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.CnATreeElement;

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

    protected boolean isEqual(CnATreeElement e1, CnATreeElement e2) {
        boolean equals = false;
        if (BpRequirementGroup.TYPE_ID.equals(e2.getTypeId()) && BpRequirementGroup.TYPE_ID.equals(e1.getTypeId())) {
            BpRequirementGroup targetModule = (BpRequirementGroup) e2;
            BpRequirementGroup module = (BpRequirementGroup) e1;
            if (ModelCommand.nullSafeEquals(targetModule.getIdentifier(), module.getIdentifier())) {
                equals = true;
            }
        }
        return equals;
    }

    public Set<CnATreeElement> getElementsFromCompendium() {
        return modulesCompendium;
    }

}
