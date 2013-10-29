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
package sernet.verinice.bpm.gsm;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.bpm.BaseJavaProcessTasks;
import sernet.verinice.interfaces.bpm.IGsmIsmExecuteProzess;
import sernet.verinice.interfaces.bpm.IGsmService;

/**
 * Java activity class for GSM vulnerability tracking process.
 * Process definition is: gsm-ism-execute.jpdl.xml 
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class JavaProcessTasks extends BaseJavaProcessTasks {

    private static final Logger LOG = Logger.getLogger(JavaProcessTasks.class);
    
    private IGsmService gsmService; 
    
    /**
     * Java activity: "java.deleteAssetScenarioLinks"
     * 
     * @param executionId
     */
    public void deleteAssetScenarioLinks(String executionId) {
        Map<String, Object> processVars = loadVariablesForProcess(executionId);
                
        Object value = processVars.get(IGsmIsmExecuteProzess.VAR_ELEMENT_UUID_SET);
        if(!(value instanceof Set<?>)) {
            LOG.error("Process variable " + IGsmIsmExecuteProzess.VAR_ELEMENT_UUID_SET + " is not a Set. This is nasty...");
            return;
        }
        @SuppressWarnings("unchecked")
        Set<String> elementUuidSet = (Set<String>) value; 
        deleteAssetScenarioLinks(elementUuidSet);
    }

    private void deleteAssetScenarioLinks(Set<String> elementUuidSet) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting links from assets to scenario...");
        }
        getGsmService().deleteAssetScenarioLinks(elementUuidSet);    
    }
    
    protected IGsmService getGsmService() {
        if(gsmService==null) {
            gsmService = (IGsmService) VeriniceContext.get(VeriniceContext.GSM_SERVICE);
        }
        return gsmService;
    }
}
