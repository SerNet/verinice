/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf
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
 ******************************************************************************/
package sernet.verinice.service.commands.crud;

import java.io.Serializable;

import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.groups.BpRequirementGroup;

public class LoadModulesWithParentsAndScope extends GraphCommand implements Serializable {

    private static final long serialVersionUID = -4078449572047545168L;

    public LoadModulesWithParentsAndScope() {
        this(null);
    }

    public LoadModulesWithParentsAndScope(Integer[] rootIds) {
        super();
        setLoadLinks(false);
        GraphElementLoader processLoader = new GraphElementLoader();
        processLoader.setScopeIds(rootIds);
        processLoader.setTypeIds(new String[] { BusinessProcess.TYPE_ID, Application.TYPE_ID,
                Device.TYPE_ID, IcsSystem.TYPE_ID, ItNetwork.TYPE_ID, ItSystem.TYPE_ID,
                Network.TYPE_ID, Room.TYPE_ID, BpRequirementGroup.TYPE_ID });
        addLoader(processLoader);
    }
}