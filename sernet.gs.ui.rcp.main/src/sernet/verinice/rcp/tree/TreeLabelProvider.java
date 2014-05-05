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
package sernet.verinice.rcp.tree;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.iso27k.ItemControlTransformer;

/**
 * Label provider for ISO 27000 model elements.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class TreeLabelProvider extends LabelProvider  {

    private static final Logger LOG = Logger.getLogger(TreeLabelProvider.class);
    
	public TreeLabelProvider() {
		super();
	}

	private ControlMaturityService maturityService = new ControlMaturityService();

    @Override
	public Image getImage(Object obj) {
		Image image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
		try {
    		if (!(obj instanceof IISO27kElement) && !(obj instanceof CnATreeElement)) {
    			return image;
    		} else {
    		    return getImage((IISO27kElement) obj);
    		} 		
		} catch(Exception e) {
            LOG.error("Error while getting image for tree item.", e);
            return image;
        }		
	}
    
    private Image getImage(IISO27kElement element) {
        Image image = CnAImageProvider.getCustomImage((CnATreeElement)element);
        if(image!=null) {
            return image;
        }
        if (element instanceof Group && !(element instanceof ImportIsoGroup)) {
            Group group = (Group) element;
            // TODO - getChildTypes()[0] might be a problem for more than one type
            image = ImageCache.getInstance().getISO27kTypeImage(group.getChildTypes()[0]);
        } else if (element instanceof SamtTopic) {
              SamtTopic topic = (SamtTopic) element;
              image = ImageCache.getInstance().getControlImplementationImage(maturityService.getIsaState(topic));
        } else if (element instanceof Control) {
            Control control = (Control) element;
            image = ImageCache.getInstance().getControlImplementationImage(control.getImplementation());
            
        } else {
            // else return type icon:
            IISO27kElement elmt = (IISO27kElement) element;
            image = ImageCache.getInstance().getISO27kTypeImage(elmt.getTypeId());
        }
        
        if(image==null) {
            image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
        }
        return image;
    }

    @Override
    public String getText(Object obj) {
        final int maxTextWidth = 80;
        String text = "unknown";
        try {
            if (obj instanceof CnATreeElement) {
                CnATreeElement element = (CnATreeElement) obj;
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
                String title = element.getTitle();
                if(title!=null && !title.isEmpty()) {
                    sb.append(title);
                }
                if(sb.length()>0) {
                    text = ItemControlTransformer.truncate(sb.toString(),maxTextWidth) ;
                }
                if (LOG.isDebugEnabled()) {
                    text = text + " (" + element.getScopeId() + "," + element.getUuid() + ")";
                }

            }
        } catch(Exception e) {
            LOG.error("Error while getting label for tree item.", e);
        }
		return text;
	}

}
