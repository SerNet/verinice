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

import java.util.Collection;

import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class BpPersonGroup extends Group<BpPerson> implements IBpGroup {
    
    private static final long serialVersionUID = 7752776589962581995L;
    
    public static final String TYPE_ID = "bp_person_group"; //$NON-NLS-1$
    public static final String PROP_NAME = "bp_person_group_name"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_person_group_tag"; //$NON-NLS-1$

    
    public static final String[] CHILD_TYPES = new String[] {BpPerson.TYPE_ID};
    
    protected BpPersonGroup() {}
    
    public BpPersonGroup(CnATreeElement parent) {
        super(parent);
        init();
    }    
    
    @Override
    public String getTitle() {
        return getEntity().getPropertyValue(PROP_NAME);
    }
    
    @Override
    public void setTitel(String name) {
        getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
    }

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }
    
    @Override
    public String[] getChildTypes() {
        return CHILD_TYPES;
    }

    @Override
    public Collection<String> getTags() {
        return TagHelper.getTags(getEntity().getPropertyValue(PROP_TAG));
    }

}
