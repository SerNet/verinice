/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade <jk[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package sernet.verinice.model.bp.groups;

import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.elements.BpRecord;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;

@SuppressWarnings("serial")
public class BpRecordGroup extends Group<BpRecord> implements IBpGroup {

    public static final String TYPE_ID = "bp_record_group"; //$NON-NLS-1$
    public static final String PROP_NAME = "bp_record_group_name"; //$NON-NLS-1$

    public static final String[] CHILD_TYPES = new String[] { BpRecord.TYPE_ID };

    public BpRecordGroup() {
        super();
    }

    public BpRecordGroup(CnATreeElement parent) {
        super(parent);
        init();
    }

    /*
     * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public String getTitle() {
        return getEntity().getPropertyValue(PROP_NAME);
    }

    @Override
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }

    /*
     * @see sernet.verinice.iso27k.model.Group#getChildTypes()
     */
    @Override
    public String[] getChildTypes() {
        return CHILD_TYPES;
    }

}
