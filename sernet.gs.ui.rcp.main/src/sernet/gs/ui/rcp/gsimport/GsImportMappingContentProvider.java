/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.gsimport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;

/**
 * @author shagedorn
 *
 */
public class GsImportMappingContentProvider implements IStructuredContentProvider {

    GstoolImportMappingView view;

    TableViewer viewer;

    public GsImportMappingContentProvider(GstoolImportMappingView view) {
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
        // empty
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer v, Object arg1, Object arg2) {
        this.viewer = (TableViewer)v;
        this.viewer.refresh();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        List<GstoolImportMappingElement> gsToolMappings = null;
        if (inputElement instanceof PlaceHolder) {
            return new Object[] {inputElement};
        } else if (inputElement instanceof Map) {
            Map<String, String> map = (Map<String, String>) inputElement;
            gsToolMappings = new ArrayList<>();
            for (String key : map.keySet()) {
                gsToolMappings.add(new GstoolImportMappingElement(key, map.get(key)));
            }
        } else if (inputElement instanceof List)  {
            gsToolMappings = (List<GstoolImportMappingElement>) inputElement;
        } else{
            throw new IllegalArgumentException("Type not supported");
        }
        return gsToolMappings.toArray();

    }

}
