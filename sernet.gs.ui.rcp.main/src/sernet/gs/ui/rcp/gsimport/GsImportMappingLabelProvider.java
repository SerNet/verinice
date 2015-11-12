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
        } else if (element instanceof GstoolImportMappingElement) {
            GstoolImportMappingElement entry = (GstoolImportMappingElement) element;

            switch (columnIndex) {

            case 0:
                return entry.getKey();
            case 1:
                return getHuiTranslation(entry.getValue());
            default:
                return null;
            }
        }
        return null;
    }

    private String getHuiTranslation(String id) {
        if(GstoolImportMappingElement.UNKNOWN.equals(id)) {
            return Messages.UnknownTypeDialog_2;
        }
        return HitroUtil.getInstance().getTypeFactory().getMessage(id);
    }
}
