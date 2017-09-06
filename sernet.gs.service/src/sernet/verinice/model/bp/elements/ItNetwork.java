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
package sernet.verinice.model.bp.elements;

import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.DeviceGroup;
import sernet.verinice.model.bp.groups.IcsSystemGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;

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
