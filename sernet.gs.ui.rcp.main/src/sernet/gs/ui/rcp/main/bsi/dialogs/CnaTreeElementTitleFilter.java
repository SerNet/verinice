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
package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.util.regex.Matcher;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.filter.TextFilter;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Filters CnaTreeElemtns by title.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class CnaTreeElementTitleFilter extends TextFilter {

    /**
     * @param viewer
     */
    public CnaTreeElementTitleFilter(TableViewer viewer) {
        super(viewer);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.bsi.filter.TextFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (!(element instanceof CnATreeElement) || regex == null)
            return true;
        
        CnATreeElement elmt = (CnATreeElement) element;
        Matcher matcher = regex.matcher(elmt.getTitle());
        return matcher.find();
    }

}


