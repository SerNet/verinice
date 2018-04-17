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

import java.util.Collection;

import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.IBpGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementWithChilds;
import sernet.verinice.model.common.ILinkChangeListener;
import sernet.verinice.model.iso27k.ProtectionRequirementsValueAdapter;
import sernet.verinice.model.iso27k.MaximumProtectionRequirementsValueListener;

/**
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 */
public class Application extends ElementWithChilds implements IBpElement, IBpGroup, ITaggableElement {

    private static final long serialVersionUID = -2569916837421863187L;
    
    public static final String TYPE_ID = "bp_application"; //$NON-NLS-1$
    public static final String PROP_NAME = "bp_application_name"; //$NON-NLS-1$
    public static final String PROP_TAG = "bp_application_tag"; //$NON-NLS-1$

    
    public static final String[] CHILD_TYPES = new String[] {BpRequirementGroup.TYPE_ID};
    
    private final ILinkChangeListener linkChangeListener = new MaximumProtectionRequirementsValueListener(this);
    private final IReevaluator protectionRequirementsProvider = new ProtectionRequirementsValueAdapter(this);

    protected Application() {}

    public Application(CnATreeElement parent) {
        super(parent);
        init();
    }
   
    @Override
    public ILinkChangeListener getLinkChangeListener() {
        return linkChangeListener;
    }

    @Override
    public IReevaluator getProtectionRequirementsProvider() {
        return protectionRequirementsProvider;
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
