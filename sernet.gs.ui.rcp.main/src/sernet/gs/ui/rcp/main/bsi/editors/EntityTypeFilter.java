/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.bsi.editors;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.hibernate.dialect.function.CastFunction;

import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Filter elements by entity type.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class EntityTypeFilter extends ViewerFilter {

    private String entityType;
    private StructuredViewer viewer;

    public EntityTypeFilter(StructuredViewer viewer) {
        this.viewer = viewer;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (!(element instanceof CnALink))
            return false;
        
        CnALink link = (CnALink) element;
        CnATreeElement viewInput = (CnATreeElement) viewer.getInput();
        return link.getRelationObject(viewInput, link).getEntity().getEntityType().equals(entityType);
    }

    /**
     * @param entType
     */
    public void setEntityType(String entType) {
        boolean active = entityType != null;
        if (entType != null && entType.length() > 0) {
            entityType = entType;
            if (active) {
                viewer.refresh();
            }
            else {
                viewer.addFilter(this);
            }
            return;
        }
        entityType = null;
        if (active)
            viewer.removeFilter(this);
    }

}


