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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.iso27k.ControlMaturityService;
import sernet.verinice.service.iso27k.ItemControlTransformer;

/**
 * Label provider for ISO 27000 model elements.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class TreeLabelProvider extends LabelProvider {

    private static final Logger LOG = Logger.getLogger(TreeLabelProvider.class);
    private static final int MAX_TEXT_WIDTH = 80;

    public TreeLabelProvider() {
        super();
    }

    private ControlMaturityService maturityService = new ControlMaturityService();

    @Override
    public Image getImage(Object obj) {
        Image image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
        try {
            if (!(obj instanceof CnATreeElement)) {
                return image;
            } else {
                return getImage((CnATreeElement) obj);
            }
        } catch (Exception e) {
            LOG.error("Error while getting image for tree item.", e);
            return image;
        }
    }

    private Image getImage(CnATreeElement element) {
        Image image = CnAImageProvider.getCustomImage((CnATreeElement) element);
        if (image != null) {
            return image;
        }
        if (element instanceof Group && !(element instanceof ImportIsoGroup)
                && !(element instanceof ImportBpGroup)) {
            Group group = (Group) element;
            // TODO - getChildTypes()[0] might be a problem for more than one
            // type
            image = ImageCache.getInstance().getImageForTypeId(group.getChildTypes()[0]);
        } else if (element instanceof SamtTopic) {
            SamtTopic topic = (SamtTopic) element;
            image = ImageCache.getInstance()
                    .getControlImplementationImage(maturityService.getIsaState(topic));
        } else if (element instanceof Control) {
            Control control = (Control) element;
            image = ImageCache.getInstance()
                    .getControlImplementationImage(control.getImplementation());
        } else {
            // else return type icon:
            image = ImageCache.getInstance().getImageForTypeId(element.getTypeId());
        }

        if (image == null) {
            image = ImageCache.getInstance().getImage(ImageCache.UNKNOWN);
        }
        return image;
    }

    @Override
    public String getText(Object obj) {
        String text = "unknown";
        if (!(obj instanceof CnATreeElement)) {
            return text;
        }
        try {
            CnATreeElement element = (CnATreeElement) obj;
            StringBuilder sb = new StringBuilder();
            sb.append(getPrefix(element));
            String title = element.getTitle();
            if (title != null) {
                sb.append(title);
            }
            text = ItemControlTransformer.truncate(sb.toString(), MAX_TEXT_WIDTH);
            if (LOG.isDebugEnabled()) {
                text = text + " (scope: " + element.getScopeId() + "," + " uu: " + element.getUuid()
                        + ", ext: " + element.getExtId() + ")";
            }
        } catch (Exception e) {
            LOG.error("Error while getting label for tree item.", e);
        }
        return text;
    }

    private String getPrefix(CnATreeElement element) {
        if (element instanceof IISO27kElement) {
            String abbreviation = ((IISO27kElement) element).getAbbreviation();
            return StringUtils.isEmpty(abbreviation) ? "" : abbreviation.concat(" ");
        } else if (element instanceof Safeguard) {
            Safeguard safeguard = (Safeguard) element;
            return String.format("%s [%s] ", safeguard.getIdentifier(), safeguard.getQualifier());
        } else if (element instanceof BpRequirement) {
            BpRequirement requirement = (BpRequirement) element;
            return String.format("%s [%s] ", requirement.getIdentifier(),
                    requirement.getQualifier());
        } else if (element instanceof BpThreat) {
            BpThreat requirement = (BpThreat) element;
            return requirement.getIdentifier().concat(" ");
        }
        return "";
    }

}
