/**
 * 
 */
package sernet.gs.ui.rcp.gsimport;

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
    
    public GsImportMappingStringEditingSupport(TableViewer viewer) {
        super(viewer);
        this.viewer = viewer;
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
        // somehow save that stuff to file in workspace, gstool-subtypes.properties
    }

}
