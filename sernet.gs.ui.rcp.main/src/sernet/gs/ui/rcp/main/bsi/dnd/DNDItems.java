/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("unchecked")
public class DNDItems {
	// TODO use system DND, (dragsource and droptarget)

	private static final Logger LOG = Logger.getLogger(DNDItems.class);
	
	public static final String BAUSTEIN = "baustein"; //$NON-NLS-1$
	public static final Object BAUSTEINUMSETZUNG = "bausteinumsetzung"; //$NON-NLS-1$
	public static final Object CNAITEM = "cnaitem"; //$NON-NLS-1$
	public static final Object RISIKOMASSNAHMENUMSETZUNG = "risikomassnahmenumsetzung";
	
	private static List dndItems = new ArrayList();
	private static Set<String> typeIdSet = new HashSet<String>(1);

	public static void setItems(List items) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setItems, list: " + items);
		}
		dndItems = items; 
		typeIdSet.clear();
		for (Object item : dndItems) {
            if(item instanceof CnATreeElement) {
                typeIdSet.add(((CnATreeElement)item).getTypeId());
            }
        }
	}

	public static List getItems() {
		return dndItems;
	}
	
	public static Set<String> getTypes() {
        return typeIdSet;
    }

	public static void clear() {
		dndItems = new ArrayList();
		typeIdSet = new HashSet<String>(1);
	}
	
}
