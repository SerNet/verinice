package sernet.verinice.rcp;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * A ColumnLabelProvider which returns return always 
 * null as text. Therefore only an image is displayed in the column.
 * 
 * Overwrite getImage to implement which image ist displayed.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class ImageColumnProvider extends ColumnLabelProvider {
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
     */
    @Override
    public abstract Image getImage(Object element);
}
