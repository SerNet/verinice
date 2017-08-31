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
import sernet.verinice.model.moditbp.categories.ApplicationCategory;
import sernet.verinice.model.moditbp.categories.BusinessProcessCategory;
import sernet.verinice.model.moditbp.categories.ICSSystemCategory;
import sernet.verinice.model.moditbp.categories.ITSystemCategory;
import sernet.verinice.model.moditbp.categories.NetworkCategory;
import sernet.verinice.model.moditbp.categories.OtherSystemCategory;
import sernet.verinice.model.moditbp.categories.PersonCategory;
import sernet.verinice.model.moditbp.categories.RoomCategory;

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
        addChild(new ApplicationCategory(this));
        addChild(new BusinessProcessCategory(this));
        addChild(new ICSSystemCategory(this));
        addChild(new ITSystemCategory(this));
        addChild(new NetworkCategory(this));
        addChild(new OtherSystemCategory(this));
        addChild(new PersonCategory(this));
        addChild(new RoomCategory(this));
    }

    /* (non-Javadoc)
     * @see sernet.verinice.model.common.CnATreeElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    
    @Override
    public boolean canContain(Object object) {
        return object instanceof BpRequirement ||
                object instanceof ApplicationCategory ||
                object instanceof BusinessProcessCategory ||
                object instanceof ICSSystemCategory ||
                object instanceof ITSystemCategory ||
                object instanceof NetworkCategory ||
                object instanceof OtherSystemCategory ||
                object instanceof PersonCategory ||
                object instanceof RoomCategory;
    }

}
