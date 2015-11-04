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
        //
        // String[] currentLinkTypeNames = { "1", "2", "3" };//
        // getPossibleLinkTypeNames(link);
        // ComboBoxCellEditor choiceEditor = new
        // ComboBoxCellEditor(viewer.getTable(), currentLinkTypeNames,
        // SWT.READ_ONLY);
        // choiceEditor.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
        //
        // return choiceEditor;

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
     */
    @Override
    protected Object getValue(Object element) {
        if(element instanceof Object[]) {
            Object[] entry = (Object[])element;
            return entry[0];
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
     */
    @Override
    protected void setValue(Object element, Object value) {
        if(element instanceof Object[] && value instanceof String) {
            try {
                Object[] oldEntry = (Object[]) element;
                Object[] newEntry = new Object[] { value, oldEntry[1] };
                GstoolTypeMapper.editGstoolSubtypeToPropertyFile(oldEntry[0], newEntry);
                view.refresh();
            } catch (IOException e) {
                LOG.error("writing of property to gstool-subtypes-mapping file fails", e);
            }
        } else {
            LOG.error("Class of Element:\t" + element.getClass().getCanonicalName());
        }
        // somehow save that stuff to file in workspace, gstool-subtypes.properties
    }

}
