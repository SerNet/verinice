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
package sernet.verinice.web;

import java.util.Hashtable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.bpm.IIsaControlFlowProcess;


/**
 * CompleteWebHandlerRegistry is a registry for {@link ICompleteWebHandler}s.
 * Key to register a handler is: [TASK_NAME].[TRANSITION_NAME] from jBPM process definition.
 * When a task is completed {@link TaskBean} checks this registry for a handler.
 * If there is a handler it is executed an client site before task is completed.
 *
 * @see {@link ICompleteWebHandler}
 * @see {@link TaskBean}
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class CompleteWebHandlerRegistry {

    private static final Logger LOG = Logger.getLogger(CompleteWebHandlerRegistry.class);
    
    private static Hashtable<String, String> handler;

    /**
     * Register your handler here.
     * Key is [TASK_NAME].[TRANSITION_NAME] from jBPM process definition.
     */
    static {
        handler = new Hashtable<String, String>();
        handler.put(IIsaControlFlowProcess.TASK_EXECUTE + "." + IIsaControlFlowProcess.TRANS_ERROR, NewQmIssueBean.NAME);
    }
    
    
    /**
     * Returns a handler for a task and a transition.
     * If no handler is registered, null is returned. 
     * 
     * @param id [TASK_NAME].[TRANSITION_NAME] from jBPM process definition
     * @return A handler or null if no handler is registered.
     */
    public static ICompleteWebHandler getHandler(String id) {
        String beanName = handler.get(id);
        if(beanName!=null) {
            return getManagedBean(beanName);
        } else {
            return null;
        }
    }
    
    private static ICompleteWebHandler getManagedBean(String beanName) {
        try {
            Map<String,Object> beanMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            return (ICompleteWebHandler) beanMap.get(beanName);
        } catch(Exception e) {
            LOG.error("Error while getting CompleteWebHandlerr JSF bean", e);
            return null;
        }
    }

    
}
