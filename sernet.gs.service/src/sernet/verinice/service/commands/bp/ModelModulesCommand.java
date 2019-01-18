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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IPostProcessor;
import sernet.verinice.model.bp.elements.BpRequirement;
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

    private static final long serialVersionUID = -9211614522872500071L;
    private transient Set<CnATreeElement> modulesCompendium;

    public ModelModulesCommand(Set<CnATreeElement> modules, Set<CnATreeElement> targetElements,
            boolean handleSafeguards) {
        super(targetElements, BpRequirementGroup.TYPE_ID,
                new ChangeDeductionPostProcessor(handleSafeguards));
        this.modulesCompendium = modules;
    }

    @Override
    protected String getIdentifier(CnATreeElement element) {
        if (element instanceof BpRequirementGroup) {
            return ((BpRequirementGroup) element).getIdentifier();
        }
        if (element instanceof BpRequirement) {
            return ((BpRequirement) element).getIdentifier();
        }
        return null;
    }

    @Override
    public Set<CnATreeElement> getGroupsFromCompendium() {
        return modulesCompendium;
    }

    private static final class ChangeDeductionPostProcessor implements IPostProcessor {
        private final boolean handleSafeguards;
        private static final long serialVersionUID = 632719636624957140L;

        private ChangeDeductionPostProcessor(boolean handleSafeguards) {
            this.handleSafeguards = handleSafeguards;
        }

        @Override
        public void process(ICommandService commandService, List<String> copyUuidList,
                Map<String, String> sourceDestMap) {
            ChangeDeductionCommand changeDeductionCommand = new ChangeDeductionCommand(
                    new HashSet<>(sourceDestMap.values()), handleSafeguards);
            try {
                commandService.executeCommand(changeDeductionCommand);
            } catch (CommandException e) {
                throw new RuntimeCommandException(e);
            }

        }
    }

}
