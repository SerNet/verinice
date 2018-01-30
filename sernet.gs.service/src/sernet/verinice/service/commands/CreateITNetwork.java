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
 * Sebastian Hagedorn - initial API and implementation
 * Alexander Ben Nasrallah <an@sernet.de> - contributor
 ******************************************************************************/
package sernet.verinice.service.commands;

import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Sebastian Hagedorn
 */
public class CreateITNetwork extends CreateElement<ItNetwork> {

    private static final long serialVersionUID = 1524831573368593526L;

    public CreateITNetwork(CnATreeElement container, boolean createChildren) {
        super(container, ItNetwork.class, true, createChildren);
    }

}