/**
 *
 */
package sernet.gs.ui.rcp.gsimport;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.hui.common.connect.HitroUtil;

/**
 * @author shagedorn
 *
 */
public class GsImportMappingLabelProvider extends LabelProvider implements ITableLabelProvider {


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object arg0, int arg1) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof PlaceHolder) {
            if (columnIndex == 1) {
                PlaceHolder ph = (PlaceHolder) element;
                return ph.getTitle();
            }
            return ""; //$NON-NLS-1$
        } else if(element instanceof Object[]) {
            Object[] entry = (Object[])element;
            switch (columnIndex) {

            case 0:
                return (String)entry[0];
            case 1:
                return getHuiTranslation((String)entry[1]);
            default:
                return null;
            }
        }
        return null;
    }

    private String getHuiTranslation(String id) {
        return HitroUtil.getInstance().getTypeFactory().getMessage(id);
    }
}
