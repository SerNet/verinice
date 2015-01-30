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
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.bpm.ITask;

/**
 * Label provider for the table in task view.
 * 
 * @see sernet.verinice.bpm.rcp.TaskView
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskLabelProvider implements ITableLabelProvider {
    
    private static final Map<String, String> PRIO_IMAGE_MAP;
    
    static {
        PRIO_IMAGE_MAP = new Hashtable<String, String>();
        PRIO_IMAGE_MAP.put(ITask.PRIO_LOW, ImageCache.PRIORITY_LOW);
        PRIO_IMAGE_MAP.put(ITask.PRIO_NORMAL, ImageCache.PRIORITY_NORMAL);
        PRIO_IMAGE_MAP.put(ITask.PRIO_HIGH, ImageCache.PRIORITY_HIGH);
    }
    
    /**
     * @param onlyMyTasks
     */
    public TaskLabelProvider() {
        super();
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
                image = ImageCache.getInstance().getImage(PRIO_IMAGE_MAP.get(task.getPriority()));
                break;
            default:
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
        final int maxColumnTextLength = 100;
        String text = null;
        if(element instanceof ITask) {
            ITask task = (ITask) element;
            text = getColumnText(columnIndex, task);
        }
        if(element instanceof PlaceHolder) {
            PlaceHolder placeHolder = (PlaceHolder) element;
            text = getColumnText(columnIndex, placeHolder);
        }
        if(text!=null && text.length()>maxColumnTextLength) {
            text = text.substring(0, maxColumnTextLength - 1) + "...";
        }
        return text;
    }

    private String getColumnText(int columnIndex, ITask task) {
        String text = null;
        switch (columnIndex) {
        case 1:
            text = task.getGroupTitle();
            break;
        case 2:
            text = task.getElementTitle();
            break;
        case 3:
            text = task.getProcessName();
            break;
        case 4:
            text = task.getName();
            break;
        case 5:
            IAuthService authService = ServiceFactory.lookupAuthService();
            text = task.getAssignee();
            if(text!=null && text.equals(authService.getUsername())) {
                text = text + " (you)";
            }                          
            break;    
        case 6:
            if(task.getDueDate()!=null) {
                text = DateFormat.getDateInstance().format(task.getDueDate());
            }
            break;          
        default:
            break;
        }
        return text;
    }
    
    private String getColumnText(int columnIndex, PlaceHolder placeHolder) {
        String text = null;
        switch (columnIndex) {       
        case 2:
            text = placeHolder.getTitle();
            break;        
        default:
            break;
        }
        return text;
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

}
