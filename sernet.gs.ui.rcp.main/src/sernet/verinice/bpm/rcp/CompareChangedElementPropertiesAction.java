/******************************************************************************* 
 * Copyright (c) 2016 Viktor Schmidt. 
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation 
 ******************************************************************************/ 
package sernet.verinice.bpm.rcp;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.model.bpm.TaskInformation;

/**
 * GUI action which compares changed element properties of task.
 * 
 * Instances of this action are created on demand in {@link TaskView} after the
 * user selects a task in the view.
 * 
 * To compare changes this action creates a {@link CompleteTaskJob} and executes
 * it by {@link ExecutorService}.
 * 
 * @see TaskView
 * @see CompleteTaskJob
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de> 
 */ 
final class CompareChangedElementPropertiesAction extends Action {

    private static final Logger LOG = Logger.getLogger(CompareChangedElementPropertiesAction.class);

    private final TaskView taskView;
    private final ITask task;
    final String id = TaskView.class.getName() + ".compare.changed.element.properties"; //$NON-NLS-1$

    public CompareChangedElementPropertiesAction(TaskView taskView, ITask task) {
        super();
        this.taskView = taskView;
        this.task = task;
        setId(id); // $NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            if (task.isWithAReleaseProcess()) {
                final CompareChangedElementPropertiesDialog dialog = new CompareChangedElementPropertiesDialog(taskView.getSite().getShell(), task);
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        dialog.open();
                    }
                });
            }
        } catch (Exception t) {
            LOG.error("Error while comparing tasks.", t); //$NON-NLS-1$
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    

    
}