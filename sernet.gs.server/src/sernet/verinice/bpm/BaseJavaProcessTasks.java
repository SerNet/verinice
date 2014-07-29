/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;

import sernet.hui.common.VeriniceContext;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class BaseJavaProcessTasks {

    protected Map<String, Object> loadVariablesForProcess(String executionId) {
        Set<String> varNameSet = getExecutionService().getVariableNames(executionId);
        Map<String, Object> varMap = Collections.emptyMap();
        if(varNameSet!=null && !varNameSet.isEmpty()) {
            varMap = getExecutionService().getVariables(executionId,varNameSet);
        }
        return varMap;
    }

    private ExecutionService getExecutionService() {
        return getProcessEngine().getExecutionService();
    }

    protected ProcessEngine getProcessEngine() {
        return (ProcessEngine) VeriniceContext.get(VeriniceContext.JBPM_PROCESS_ENGINE);
    }
}
