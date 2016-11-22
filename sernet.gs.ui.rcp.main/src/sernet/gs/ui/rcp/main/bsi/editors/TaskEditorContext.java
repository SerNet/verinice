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
package sernet.gs.ui.rcp.main.bsi.editors;

import sernet.verinice.model.bpm.TaskInformation;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class TaskEditorContext {
    private TaskInformation task;
    private CnATreeElement element;
    
    /**
     * @param task
     * @param element
     */
    public TaskEditorContext(TaskInformation task, CnATreeElement element) {
        super();
        this.task = task;
        this.element = element;
    }
    /**
     * @return the task
     */
    public TaskInformation getTask() {
        return task;
    }
    /**
     * @return the element
     */
    public CnATreeElement getElement() {
        return element;
    }
}
