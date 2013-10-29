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

import java.util.Hashtable;
import java.util.Map;

import sernet.verinice.interfaces.bpm.IIsaControlFlowProcess;
import sernet.verinice.interfaces.bpm.IIsaQmProcess;

/**
 * CompleteHandlerRegistry is a registry for {@link ICompleteClientHandler}s.
 * Key to register a handler is: [TASK_NAME].[TRANSITION_NAME] from jBPM process definition.
 * When a task is completed {@link CompleteTaskAction} checks this registry for a handler.
 * If there is a handler it is executed an client site before task is completed.
 *
 * @see {@link ICompleteClientHandler}
 * @see {@link CompleteTaskAction}
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class CompleteHandlerRegistry {

    private static final Map<String, ICompleteClientHandler> HANDLER;
    
    /**
     * Register your handler here.
     * Key is [TASK_NAME].[TRANSITION_NAME] from jBPM process definition.
     */
    static {
        HANDLER = new Hashtable<String, ICompleteClientHandler>();
        HANDLER.put(IIsaQmProcess.TASK_IQM_SET_ASSIGNEE + "." + IIsaQmProcess.TRANS_IQM_SET_ASSIGNEE, new SetAssigneeClientHandler());
        HANDLER.put(IIsaControlFlowProcess.TASK_EXECUTE + "." + IIsaControlFlowProcess.TRANS_ERROR, new NewQmIssueClientHandler());
    }
    
    /**
     * Returns a handler for a task and a transition.
     * If no handler is registered, null is returned. 
     * 
     * @param id [TASK_NAME].[TRANSITION_NAME] from jBPM process definition
     * @return A handler or null if no handler is registered.
     */
    public static ICompleteClientHandler getHandler(String id) {
        return HANDLER.get(id);
    }
    
    /**
     * Adds a handler to the registry.
     * 
     * @param id [TASK_NAME].[TRANSITION_NAME] from jBPM process definition
     * @param handler A handler
     */
    public static void registerHandler(String id, ICompleteClientHandler handler) {
        CompleteHandlerRegistry.HANDLER.put(id,handler);
    }
    
    /**
     * Renovesd a handler from the registry.
     * 
     * @param id [TASK_NAME].[TRANSITION_NAME] from jBPM process definition
     */
    public static void removeHandler(String id) {
        CompleteHandlerRegistry.HANDLER.remove(id);
    }

}
