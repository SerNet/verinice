/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.iso27k.rcp.CatalogView;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.ThreatGroup;

/**
 * Transforms {@link IItem} from {@link CatalogView} to ISO 27k {@link Threat}s
 * or {@link ThreatGroup}s
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @author koderman[at]sernet[dot]de
 */
public class ItemThreatTransformer {

	
	public static Threat transform(IItem item) {
	    Threat threat = new Threat();
		if(item.getName()!=null) {
		    threat.setTitel(item.getName().replaceAll("\\s", " ")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		threat.setDescription(item.getDescription());
		
		return threat;
	}

	
	public static ThreatGroup transformToGroup(IItem item) {
		ThreatGroup threatGroup = new ThreatGroup();
		if(item.getName()!=null) {
			// replace all whitespace with " "
			threatGroup.setTitel(item.getName().replaceAll("\\s", " ")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return threatGroup;
	}
	
	/**
	 * Truncates a text if it is longer than maxWidth.
	 * If text is truncated three dots ("...") are added in the end.
	 * 
	 * @param text
	 * @param maxWidth
	 * @return truncated text (if text is longer than maxWidth) and three dots ("...") 
	 */
	public static String truncate(String text, int maxWidth) {
	    String truncatedText = text;
	    if(text!=null && text.length()>maxWidth) {
	        truncatedText = new StringBuilder(text.substring(0, (maxWidth-3))).append("...").toString(); //$NON-NLS-1$
	    }
	    return truncatedText;
	}
	

	

}
