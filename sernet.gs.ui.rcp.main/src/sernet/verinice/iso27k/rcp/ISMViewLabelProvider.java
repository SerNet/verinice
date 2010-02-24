/*******************************************************************************
 * Copyright (c) 2009  Daniel Murygin <dm@sernet.de>,
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
 *      Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.verinice.iso27k.model.Asset;
import sernet.verinice.iso27k.model.Audit;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.Document;
import sernet.verinice.iso27k.model.Evidence;
import sernet.verinice.iso27k.model.Exception;
import sernet.verinice.iso27k.model.Finding;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.model.IISO27kElement;
import sernet.verinice.iso27k.model.IISO27kGroup;
import sernet.verinice.iso27k.model.Incident;
import sernet.verinice.iso27k.model.IncidentScenario;
import sernet.verinice.iso27k.model.Interview;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.model.PersonIso;
import sernet.verinice.iso27k.model.Requirement;
import sernet.verinice.iso27k.model.Threat;
import sernet.verinice.iso27k.model.Vulnerability;
import sernet.verinice.iso27k.service.Item;
import sernet.verinice.iso27k.service.ItemControlTransformer;

/**
 * Label provider for ISO 27000 model elements.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 * 
 */
public class ISMViewLabelProvider extends LabelProvider {

	public ISMViewLabelProvider(TreeViewerCache cache) {
		super();
		this.cache = cache;
	}

	private TreeViewerCache cache;

	@Override
	public Image getImage(Object obj) {
		Image image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
		
		if (!(obj instanceof IISO27kElement))
			return image;
		
		else if (obj instanceof Group) {
			Group group = (Group) obj;
            image = ImageCache.getInstance().getISO27kTypeImage(group.getChildTypes()[0]);
			return image;
		}

		else if (obj instanceof Control) {
			Control control = (Control) obj;
//			if (Control.isSufficientlyMature(control))
//				image = ImageCache.getInstance().getImage(ImageCache.ISO27K_CONTROL_YES);
//			else
//				image = ImageCache.getInstance().getImage(ImageCache.ISO27K_CONTROL_NO);
			image = ImageCache.getInstance().getControlImplementationImage(control.getImplemented());
		}
		else {
			// else return type icon:
			IISO27kElement elmt = (IISO27kElement) obj;
			image = ImageCache.getInstance().getISO27kTypeImage(elmt.getTypeId());
		}
		
		return image;
	}

	@Override
	public String getText(Object obj) {
		String title = "unknown";
		if (obj != null) {	
			Object cachedObject = cache.getCachedObject(obj);
			if (cachedObject == null) {
				cache.addObject(obj);
			} else {
				obj = cachedObject;
			}
			if (obj instanceof CnATreeElement) {
				CnATreeElement element = (CnATreeElement) obj;
				title = ItemControlTransformer.addLineBreaks(element.getTitle(),40);
				if(element instanceof Control) {
					String abbreviation = ((Control)element).getAbbreviation();
					if(Pattern.matches(Item.NUMBER_REGEX_PATTERN,abbreviation) 
							&& (title==null || !title.startsWith(abbreviation))) {
						title = new StringBuilder(abbreviation).append(" ").append(title).toString();
					}
				}
			}
		}
		return title;
	}

}
