/*******************************************************************************
 * Copyright (c) 2009  Daniel Murygin <dm[at]sernet[dot]de>,
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
 *      Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.rcp.tree.TreeLabelProvider;
import sernet.verinice.service.iso27k.ItemControlTransformer;

/**
 * Label provider for ISO 27000 model elements.
 * 
 * @deprecated Use {@link TreeLabelProvider}
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ISMViewLabelProvider extends LabelProvider  {

    private static final Logger LOG = Logger.getLogger(ISMViewLabelProvider.class);
    
	public ISMViewLabelProvider(TreeViewerCache cache) {
		super();
		this.cache = cache;
	}

	private TreeViewerCache cache;
	
	private ControlMaturityService maturityService = new ControlMaturityService();
	
	

	@SuppressWarnings("unchecked")
    @Override
	public Image getImage(Object obj) {
		Image image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
		
		if (!(obj instanceof IISO27kElement)){
			return image;
		
		} else if (obj instanceof Group && !(obj instanceof ImportIsoGroup)) {
			Group group = (Group) obj;
			// TODO - getChildTypes()[0] might be a problem for more than one type
            image = ImageCache.getInstance().getISO27kTypeImage(group.getChildTypes()[0]);
			return image;
		} else if (obj instanceof SamtTopic) {
	          SamtTopic topic = (SamtTopic) obj;
	          image = ImageCache.getInstance().getControlImplementationImage(maturityService.getIsaState(topic));
	    } else if (obj instanceof Control) {
		    Control control = (Control) obj;
		    // using maturity:
			//image = ImageCache.getInstance().getControlImplementationImage(maturityService.getImplementationState(control));
		    // using implementation dropdown:
		    image = ImageCache.getInstance().getControlImplementationImage(control.getImplementation());
		    
		} else {
			// else return type icon:
			IISO27kElement elmt = (IISO27kElement) obj;
			image = ImageCache.getInstance().getISO27kTypeImage(elmt.getTypeId());
		}
		
		return image;
	}

	@Override
	public String getText(Object obj) {
	    final int maxWidth = 80;
		Object obj0 = obj;
	    String text = "unknown";
		if (obj0 != null) {	
		    
		    Object cachedObject = null;
            if(obj0 instanceof CnATreeElement) {
                cachedObject = cache.getCachedObject((CnATreeElement) obj0);
                if (cachedObject == null) {
                    cache.addObject(obj0);
                } else {
                    obj0 = cachedObject;
                }
            }
            
			if (obj0 instanceof CnATreeElement) {
				CnATreeElement element = (CnATreeElement) obj0;
				StringBuilder sb = new StringBuilder();
				if(element instanceof Control) {
					String abbreviation = ((Control)element).getAbbreviation();
					if(abbreviation!=null && !abbreviation.isEmpty()) {
					    sb.append(abbreviation).append(" ");
					}
				}
				
				if(element instanceof IISO27kElement) {
                    String abbreviation = ((IISO27kElement)element).getAbbreviation();
                    if(abbreviation!=null && !abbreviation.isEmpty()) {
                        sb.append(abbreviation).append(" ");
                    }
                }
				
				element = Retriever.checkRetrieveElement(element);
				String title = element.getTitle();
                if(title!=null && !title.isEmpty()) {
                    sb.append(title);
                }
                if(sb.length()>0) {
                    text = ItemControlTransformer.truncate(sb.toString(),maxWidth) ;
                }
                if (LOG.isDebugEnabled()) {
                    text = text + " (" + element.getScopeId() + ")";
                }
			}
		}
		return text;
	}
}
