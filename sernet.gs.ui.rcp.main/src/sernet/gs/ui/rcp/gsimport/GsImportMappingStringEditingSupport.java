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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

/**
 * @author shagedorn
 *
 */
public class GsImportMappingStringEditingSupport extends EditingSupport {
    
    private static final Logger LOG = Logger.getLogger(GsImportMappingStringEditingSupport.class);
    
    private TableViewer viewer;
    private GstoolImportMappingView view;
    
    public GsImportMappingStringEditingSupport(TableViewer viewer, GstoolImportMappingView view) {
        super(viewer);
        this.viewer = viewer;
        this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
     */
    @Override
    protected boolean canEdit(Object arg0) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
     */
    @Override
    protected CellEditor getCellEditor(Object arg0) {
        return new TextCellEditor(viewer.getTable());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
     */
    @Override
    protected Object getValue(Object element) {
        if (element instanceof GstoolImportMappingElement) {
            GstoolImportMappingElement entry = (GstoolImportMappingElement) element;
            return entry.getKey();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
     */
    @Override
    protected void setValue(Object element, Object value) {
        String val;
        if (value instanceof String) {
            val = (String) value;
            if (element instanceof GstoolImportMappingElement) {
                GstoolImportMappingElement oldEntry = (GstoolImportMappingElement) element;
                GstoolImportMappingElement newEntry = new GstoolImportMappingElement(val, oldEntry.getValue());
                GstoolTypeMapper.editGstoolSubtypeToPropertyFile(oldEntry, newEntry);
                view.refresh();
                viewer.setSelection(new StructuredSelection(newEntry), true);
            } else {
                LOG.error("Class of Element:\t" + element.getClass().getCanonicalName());
            }
        } else {
            LOG.error("Class of value-Element:\t" + element.getClass().getCanonicalName());
        }
    }

}
