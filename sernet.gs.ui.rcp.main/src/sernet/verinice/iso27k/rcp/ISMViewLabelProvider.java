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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.verinice.iso27k.model.IISO27kElement;
import sernet.verinice.iso27k.model.IISO27kGroup;
import sernet.verinice.iso27k.model.Organization;

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
		Image image = null;
		// TODO: dm - implement this, when icons are ready
		if (obj instanceof IISO27kGroup || obj instanceof Organization) {
			image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN_GROUP);
		} else if (obj instanceof IISO27kElement) {
			image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
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
				title = ((CnATreeElement)obj).getTitle();
			}
		}
		return title;
	}

}
