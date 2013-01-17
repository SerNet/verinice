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
package sernet.verinice.bpm.isam;

import java.util.List;
import java.util.Map;

import sernet.verinice.interfaces.bpm.IIsaControlFlowService;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.bpm.ProcessInformation;

/**
 * Dummy implementation for standalone version.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaControlFlowServiceDummy implements IIsaControlFlowService {

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceGeneric#findProcessDefinitionId(java.lang.String)
     */
    @Override
    public String findProcessDefinitionId(String processDefinitionKey) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceGeneric#startProcess(java.lang.String, java.util.Map)
     */
    @Override
    public void startProcess(String processDefinitionKey, Map<String, ?> variables) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceGeneric#deleteProcess(java.lang.String)
     */
    @Override
    public void deleteProcess(String id) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceGeneric#isActive()
     */
    @Override
    public boolean isActive() {
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIsaControlFlowService#startProcessesForControls(java.util.List)
     */
    @Override
    public IProcessStartInformation startProcessesForControls(List<String> controlUuids) {
        return new ProcessInformation(0);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIsaControlFlowService#startProcessesForGroups(java.util.List)
     */
    @Override
    public IProcessStartInformation startProcessesForGroups(List<String> groupUuids) {
        return new ProcessInformation(0);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IIsaControlFlowService#startProcessesForAudits(java.util.List)
     */
    @Override
    public IProcessStartInformation startProcessesForAudits(List<String> auditUuids) {
        return new ProcessInformation(0);
    }

}
