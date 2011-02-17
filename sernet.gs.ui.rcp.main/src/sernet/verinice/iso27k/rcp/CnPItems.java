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
package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class CnPItems {
	// TODO use system clipboard

	private static final Logger LOG = Logger.getLogger(CnPItems.class);
	
	private static List copyItems = new ArrayList();
	
	private static List cutItems = new ArrayList();

	public static void setCopyItems(List items) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setCopyItems: " + items);
		}
		copyItems.addAll(items); 
	}
	
	public static void setCutItems(List items) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setCutItems: " + items);
		}
		cutItems.addAll(items); 
	}

	public static List getCopyItems() {
		return copyItems;
	}
	
	public static List getCutItems() {
		return cutItems;
	}

	public static void clearCopyItems() {
		copyItems.clear();
	}
	
	public static void clearCutItems() {
		cutItems.clear();
	}
	
	

}
