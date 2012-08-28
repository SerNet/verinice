/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import java.text.DateFormat;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.KeyValue;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TaskLabelProvider implements ITableLabelProvider {

    boolean onlyMyTasks = true;
    
    private static final Map<String, String> prioImageMap;
    
    static {
        prioImageMap = new Hashtable<String, String>();
        prioImageMap.put(ITask.PRIO_LOW, ImageCache.PRIORITY_LOW);
        prioImageMap.put(ITask.PRIO_NORMAL, ImageCache.PRIORITY_NORMAL);
        prioImageMap.put(ITask.PRIO_HIGH, ImageCache.PRIORITY_HIGH);
    }
    
    /**
     * @param onlyMyTasks
     */
    public TaskLabelProvider(boolean onlyMyTasks) {
        this.onlyMyTasks = onlyMyTasks;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        Image image = null;
        if(element instanceof ITask) {
            ITask task = (ITask) element;
            switch (columnIndex) {
            case 0:
                image = ImageCache.getInstance().getImage(prioImageMap.get(task.getPriority()));
                break;
            }
        }
        return image;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        String text = null;
        if(element instanceof KeyValue) {
            if(columnIndex==0) {
                text = ((KeyValue)element).getValue();
            }
        }
        if(element instanceof ITask) {
            ITask task = (ITask) element;
            switch (columnIndex) {
            case 0:
                text = task.getControlTitle();
                break;
            case 1:
                text = task.getProcessName();
                break;
            case 2:
                text = task.getName();
                break;
            case 3:
                IAuthService authService = ServiceFactory.lookupAuthService();
                text = task.getAssignee();
                if(text!=null && text.equals(authService.getUsername())) {
                    text = text + " (you)";
                }                          
                break;    
            case 4:
                if(task.getDueDate()!=null) {
                    text = DateFormat.getDateInstance().format(task.getDueDate());
                }
                break;          
            default:
                break;
            }
        }
        if(text!=null && text.length()>100) {
            text = text.substring(0, 99) + "...";
        }
        return text;
    }

    public boolean isOnlyMyTasks() {
        return onlyMyTasks;
    }

    public void setOnlyMyTasks(boolean onlyMyTasks) {
        this.onlyMyTasks = onlyMyTasks;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void addListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

}
