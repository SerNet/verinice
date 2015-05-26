/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * @author Daniel Murygin <dm@sernet.de>
 * 
 */
@SuppressWarnings("restriction")
public class HideEmptyFilter extends ViewerFilter {

    private static final Logger LOG = Logger.getLogger(HideEmptyFilter.class);
    
    private StructuredViewer viewer;
    private boolean hideEmpty;

    public HideEmptyFilter(StructuredViewer viewer) {
        this.viewer = viewer;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers
     * .Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object o) {
        boolean visible = true;
        try {
            if (hideEmpty 
                && o instanceof IISO27kGroup 
                && o instanceof CnATreeElement
                && !(o instanceof Audit)
                && !(o instanceof Asset)) { 
                CnATreeElement element = (CnATreeElement) o;
                Set<CnATreeElement> children = element.getChildren();
                visible = (children != null && children.size() > 0);
            }
        } catch (Exception e) {
            LOG.error("Error", e);
        }
        return visible;
    }

    /**
     * @param hideEmpty
     */
    public void setHideEmpty(boolean hideEmpty) {
        if (this.hideEmpty!=hideEmpty) {
            this.hideEmpty = hideEmpty;
            if(this.hideEmpty) {
                viewer.addFilter(this);
            } else {
                viewer.removeFilter(this);
            }
            viewer.refresh();
            
        }
    }
    
    public boolean isHideEmpty() {
        return hideEmpty;
    }

    /**
     * @return
     */
    public boolean isActive() {
        return isHideEmpty();
    }

}
