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

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.KeyValue;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
@SuppressWarnings("restriction")
public class TaskContentProvider implements ITreeContentProvider {

    private TreeViewer viewer;

    private TaskTreeModel model;
    
    /**
     * @param treeViewer
     */
    public TaskContentProvider(TreeViewer treeViewer) {
        this.viewer = treeViewer;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof List/* <ITask> */) {
            this.model = new TaskTreeModel((List<ITask>) newInput);
        }
        if (newInput instanceof TaskTreeModel) {
            this.model = (TaskTreeModel) newInput;
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return model.getRootElementArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        Object[] children = new Object[0];
        if (parentElement instanceof KeyValue) {
            children = model.getChildrenArray((KeyValue) parentElement);
        }
        return children;
    }

    @Override
    public Object getParent(Object element) {
        Object parent = null;
        if (element instanceof ITask) {
            parent = new KeyValue(((ITask) element).getUuidAudit(), ((ITask) element).getAuditTitle());
        }
        return parent;
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    public void removeTask(final ITask task) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                model.remove(task);
                viewer.remove(task);
                viewer.refresh();
            }
        });
    }
    
    public int getNumberOfGroups() {
        return model.getRootElementSet().size();
    }

}
