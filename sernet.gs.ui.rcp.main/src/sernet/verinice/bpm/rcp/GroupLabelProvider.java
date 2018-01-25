/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import java.util.Hashtable;
import java.util.Map;

import sernet.verinice.iso27k.rcp.IComboModelLabelProvider;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * Provides labels of groups in task view. A group in task view is a
 * organization, an audit or an IT network.
 * 
 * @see TaskView
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GroupLabelProvider implements IComboModelLabelProvider<CnATreeElement> {
    
    static final Map<String, String> TYPE_SUFFIX_MAP = new Hashtable<String, String>();   
    static {
        TYPE_SUFFIX_MAP.put(Organization.TYPE_ID, Messages.GroupLabelProvider_0);
        TYPE_SUFFIX_MAP.put(ITVerbund.TYPE_ID, Messages.GroupLabelProvider_2);
        TYPE_SUFFIX_MAP.put(ItNetwork.TYPE_ID, Messages.GroupLabelProvider_2);
    }
    
    @Override
    public String getLabel(CnATreeElement element) {           
        return createTitel(element);
    }

    private String createTitel(CnATreeElement element) {
        StringBuilder sb = new StringBuilder();
        sb.append(element.getTitle());
        String suffix = createSuffix(element);
        if(suffix!=null) {
            sb.append(" ").append(suffix); //$NON-NLS-1$
        }
        return sb.toString();
    }

    private String createSuffix(CnATreeElement element) {     
        return TYPE_SUFFIX_MAP.get(element.getTypeId());
    }
}