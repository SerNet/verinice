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
import java.util.List;

import org.apache.log4j.Logger;

public class CnPItems {
	// TODO use system clipboard

	private static final Logger LOG = Logger.getLogger(CnPItems.class);
	
	private static List copyPasteItems = new ArrayList();

	public static void setItems(List items) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setItems: " + items);
		}
		copyPasteItems.addAll(items); 
	}

	public static List getItems() {
		return copyPasteItems;
	}

	public static void clear() {
		copyPasteItems = new ArrayList();
	}
	
	

}
