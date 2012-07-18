/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.bpm;

import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import sernet.verinice.interfaces.bpm.ITaskService;

/**
 * ICompleteClientHandler are executed on client site before a task is completed.
 * Returned {@link Map} is passed to {@link ITaskService} completeTask methods.
 * 
 * To use a handler you have to register it with a task and outcome name
 * in {@link CompleteHandlerRegistry}. 
 *
 * @see CompleteHandlerRegistry
 * @see {@link CompleteTaskAction}
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ICompleteClientHandler {

    /**
     * Executed on client site before a task is completed.
     * Returned {@link Map} is passed to {@link ITaskService} completeTask methods.
     * 
     * @return Parameter which are passed to task service.
     */
    Map<String, Object> execute();

    /**
     * @param shell The current Shell
     */
    void setShell(Shell shell);

}
