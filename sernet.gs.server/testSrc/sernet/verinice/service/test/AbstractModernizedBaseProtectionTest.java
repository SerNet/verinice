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
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.CreateITNetwork;
import sernet.verinice.service.model.LoadModel;

/**
 * Basis methods for mbp tests.
 * @author uz[at]sernet.de
 *
 */
public abstract class AbstractModernizedBaseProtectionTest extends CommandServiceProvider {

    public AbstractModernizedBaseProtectionTest() {
        super();
    }

    /**
     * Create a {@link Safeguard} in the given container.
     */
    protected Safeguard createSafeguard(CnATreeElement container) throws CommandException {
        CreateElement<Safeguard> saveCommand1 = new CreateElement<>(container, Safeguard.class, "");
        saveCommand1.setInheritAuditPermissions(true);
        saveCommand1 = commandService.executeCommand(saveCommand1);
        Safeguard requirment = saveCommand1.getNewElement();

        return requirment;
    }

    /**
     * Create a {@link SafeguardGroup} in the given container.
     */
    protected SafeguardGroup createSafeguardGroup(CnATreeElement container)
            throws CommandException {
        CreateElement<SafeguardGroup> saveCommand = new CreateElement<>(container,
                SafeguardGroup.class, "");
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = commandService.executeCommand(saveCommand);
        SafeguardGroup group = saveCommand.getNewElement();
        return group;
    }

    /**
     * Create a {@link SafeguardGroup} in the given container.
     */
    protected BpRequirement createBpRequirement(CnATreeElement container) throws CommandException {
        CreateElement<BpRequirement> saveCommand = new CreateElement<>(container,
                BpRequirement.class, "");
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = commandService.executeCommand(saveCommand);
        BpRequirement requirement = saveCommand.getNewElement();

        return requirement;
    }

    /**
     * Create a {@link BpRequirementGroup} in the given container.
     */
    protected BpRequirementGroup createRequirementGroup(CnATreeElement container)
            throws CommandException {
        CreateElement<BpRequirementGroup> saveCommand = new CreateElement<>(container,
                BpRequirementGroup.class, "");
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = commandService.executeCommand(saveCommand);
        BpRequirementGroup group = saveCommand.getNewElement();
        return group;
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

        assertNotNull("BP model is null.", model);
        CreateITNetwork saveCommand = new CreateITNetwork(model, ItNetwork.class, true);
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = commandService.executeCommand(saveCommand);
        ItNetwork organization = saveCommand.getNewElement();

        return organization;
    }

}