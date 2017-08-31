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
 ******************************************************************************/
package sernet.verinice.model.moditbp.elements;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.moditbp.IBpElement;
import sernet.verinice.model.moditbp.categories.ApplicationGroup;
import sernet.verinice.model.moditbp.categories.BusinessProcessGroup;
import sernet.verinice.model.moditbp.categories.IcsSystemGroup;
import sernet.verinice.model.moditbp.categories.ItSystemGroup;
import sernet.verinice.model.moditbp.categories.NetworkGroup;
import sernet.verinice.model.moditbp.categories.DeviceGroup;
import sernet.verinice.model.moditbp.categories.BpPersonGroup;
import sernet.verinice.model.moditbp.categories.BpRequirementGroup;
import sernet.verinice.model.moditbp.categories.BpThreatGroup;
import sernet.verinice.model.moditbp.categories.RoomGroup;
import sernet.verinice.model.moditbp.categories.SafeguardGroup;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ItNetwork extends CnATreeElement implements IBpElement  {
    
    private static final long serialVersionUID = -542743048413632420L;
       
    public static final String TYPE_ID = "bp_itnetwork"; //$NON-NLS-1$
    
    protected ItNetwork() {}
    
    public ItNetwork(CnATreeElement parent) {
        super(parent);
        init();
    }     
    
    public void createNewCategories() {
        addChild(new ApplicationGroup(this));
        addChild(new BpPersonGroup(this));
        addChild(new BpRequirementGroup(this));
        addChild(new BpThreatGroup(this));
        addChild(new BusinessProcessGroup(this));
        addChild(new DeviceGroup(this));
        addChild(new IcsSystemGroup(this));
        addChild(new ItSystemGroup(this));
        addChild(new NetworkGroup(this));
        addChild(new RoomGroup(this));
        addChild(new SafeguardGroup(this));
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    @Override
    public boolean canContain(Object object) {
        return object instanceof BpRequirement ||
               object instanceof ApplicationGroup ||
               object instanceof BpPersonGroup ||
               object instanceof BpRequirementGroup ||
               object instanceof BpThreatGroup ||
               object instanceof BusinessProcessGroup ||
               object instanceof DeviceGroup ||
               object instanceof IcsSystemGroup ||
               object instanceof ItSystemGroup ||
               object instanceof NetworkGroup ||
               object instanceof RoomGroup ||
               object instanceof SafeguardGroup;
    }

}
