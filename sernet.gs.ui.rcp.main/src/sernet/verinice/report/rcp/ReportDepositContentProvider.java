/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
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
package sernet.verinice.report.rcp;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 *
 */
public class ReportDepositContentProvider implements IStructuredContentProvider {

    TableViewer viewer;

    ReportDepositView depositView;

    public ReportDepositContentProvider(ReportDepositView view) {
        this.depositView = view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
        // empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
     * .viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        this.viewer = (TableViewer) v;
        viewer.refresh();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.
     * lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof PlaceHolder) {
            return new Object[] { inputElement };
        } else if (inputElement instanceof ReportTemplateMetaData[]
                && ((ReportTemplateMetaData[]) inputElement).length > 0) {
            return (ReportTemplateMetaData[]) inputElement;
        }

        return new Object[] { new PlaceHolder(Messages.ReportDepositView_4) };
    }

}
