/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class DNDItems {
	// TODO use system DND, (dragsource and droptarget)

	private static final Logger LOG = Logger.getLogger(DNDItems.class);
	
	public static final String BAUSTEIN = "baustein"; //$NON-NLS-1$
	public static final Object BAUSTEINUMSETZUNG = "bausteinumsetzung"; //$NON-NLS-1$
	public static final Object CNAITEM = "cnaitem"; //$NON-NLS-1$
	public static final Object RISIKOMASSNAHMENUMSETZUNG = "risikomassnahmenumsetzung";
	
	private static List dndItems = new ArrayList();

	public static void setItems(List items) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setItems, list: " + items);
		}
		dndItems = items; 
	}

	public static List getItems() {
		return dndItems;
	}

	public static void clear() {
		dndItems = new ArrayList();
	}
	
}
