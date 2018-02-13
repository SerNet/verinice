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
package sernet.verinice.model.bp.groups;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ImportBpGroup extends Group<Organization> implements IBpGroup {
    
    private static final long serialVersionUID = -7286059698308443978L;
    
    public static final String TYPE_ID = "bp_import_group";
    
    public static final String[] CHILD_TYPES = new String[] { 
            ItNetwork.TYPE_ID,
            Application.TYPE_ID,
            BpPerson.TYPE_ID,
            BpRequirement.TYPE_ID,
            BpThreat.TYPE_ID,
            BusinessProcess.TYPE_ID,
            Device.TYPE_ID,
            IcsSystem.TYPE_ID,
            ItSystem.TYPE_ID,
            Network.TYPE_ID,
            Room.TYPE_ID,
            Safeguard.TYPE_ID
    };

    protected ImportBpGroup() {}
    
    public ImportBpGroup(CnATreeElement model) {
        super(model);
        setEntity(new Entity(TYPE_ID));
    }
    
    @Override
    public String getTitle() {
        return "imported Objects"; // TODO internationalize
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    @Override
    public boolean canContain(Object obj) {      
        return isNotImportBpGroup(obj) && super.canContain(obj);
    }

    protected boolean isNotImportBpGroup(Object obj) {
        CnATreeElement element = (CnATreeElement)obj;
        return !(this.getTypeId().equals(element.getTypeId()));
    }
    
    @Override
    public String[] getChildTypes() {
        return CHILD_TYPES;
    }

}
