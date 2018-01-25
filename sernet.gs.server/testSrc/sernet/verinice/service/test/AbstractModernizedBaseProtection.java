/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertNotNull;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.CreateITNetwork;
import sernet.verinice.service.commands.UpdateElement;
import sernet.verinice.service.commands.crud.CreateBpModel;
import sernet.verinice.service.model.LoadModel;

/**
 * Basis methods for mbp tests.
 * 
 * @author uz[at]sernet.de
 *
 */
public abstract class AbstractModernizedBaseProtection extends CommandServiceProvider {

    public AbstractModernizedBaseProtection() {
        super();
    }

    /**
     * Update the given element in the database.
     */
    protected <T extends CnATreeElement> T update(T element) throws CommandException {
        UpdateElement<T> command = new UpdateElement<>(element, true, null);
        commandService.executeCommand(command);
        return command.getElement();
    }

    /**
     * Create a {@link Group} in the given container.
     */
    protected <T extends Group<?>> T createGroup(CnATreeElement container, Class<T> type)
            throws CommandException {
        return createElement(container, type);
    }

    /**
     * Create a {@link CnATreeElement} in the given container.
     */
    protected <T extends CnATreeElement> T createElement(CnATreeElement container, Class<T> type)
            throws CommandException {
        CreateElement<T> saveCommand = new CreateElement<>(container, type, "");
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = commandService.executeCommand(saveCommand);
        return saveCommand.getNewElement();
    }

    /**
     * Create a new root network for the modernized base protection.
     * 
     * @return the itnetwork
     */
    protected ItNetwork createNewBPOrganization() throws CommandException {
        LoadModel<BpModel> loadModel = new LoadModel<>(BpModel.class);
        loadModel = commandService.executeCommand(loadModel);
        BpModel model = loadModel.getModel();
        if (model == null) {
            CreateBpModel createBpModel = new CreateBpModel();
            createBpModel = commandService.executeCommand(createBpModel);
            model = createBpModel.getElement();
        }

        assertNotNull("BP model is null.", model);
        CreateITNetwork saveCommand = new CreateITNetwork(model, true);
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = commandService.executeCommand(saveCommand);
        return saveCommand.getNewElement();
    }

    /**
     * Create a {@link SafeguardGroup} in the given container.
     */
    protected SafeguardGroup createSafeguardGroup(CnATreeElement container)
            throws CommandException {
        return createGroup(container, SafeguardGroup.class);// saveCommand.getNewElement();
    }

    /**
     * Create a {@link Safeguard} in the given container.
     */
    protected Safeguard createSafeguard(CnATreeElement container) throws CommandException {
        return createElement(container, Safeguard.class);
    }

    /**
     * Create a {@link BpRequirementGroup} in the given container.
     */
    protected BpRequirementGroup createRequirementGroup(CnATreeElement container)
            throws CommandException {
        return createGroup(container, BpRequirementGroup.class);
    }

    /**
     * Create a {@link SafeguardGroup} in the given container.
     */
    protected BpRequirement createBpRequirement(CnATreeElement container) throws CommandException {
        return createElement(container, BpRequirement.class);
    }

}