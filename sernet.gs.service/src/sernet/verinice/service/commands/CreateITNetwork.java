/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 *     Alexander Ben Nasrallah <an@sernet.de> - contributor
 ******************************************************************************/
package sernet.verinice.service.commands;

import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class CreateITNetwork extends CreateElement<ItNetwork> {

    private static final long serialVersionUID = 1L;

    public CreateITNetwork(CnATreeElement container, Class<ItNetwork> type, boolean createChildren) {
        super(container, type, true, createChildren);
    }

    @Override
    public void execute() {
        super.execute();
        if (super.element instanceof ItNetwork) {
            ItNetwork network = element;
            if (createChildren) {
                network.createNewCategories();
            }
            addPermissionsForScope(network);
            element.setScopeId(element.getDbId());
            for (CnATreeElement group : element.getChildren()) {
                group.setScopeId(element.getDbId());
            }
        }
    }
}