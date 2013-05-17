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
package sernet.verinice.interfaces.bpm;

import java.util.Set;

/**
 * Process service interface to manage Greenbone vulnerabilities.
 * Process definition is: gsm-ism-execute.jpdl.xml
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IGsmService extends IProcessServiceGeneric {
    
    /**
     * @param orgId
     * @return
     */
    IGsmValidationResult validateOrganization(Integer orgId);

    /**
     * @param orgId
     * @return
     */
    IProcessStartInformation startProcessesForOrganization(Integer orgId);
    
    /**
     * Deletes all links between asset and scenarios in one process.
     * elementUuidSet contains UUIDs of all elements in one process.
     * 
     * After deleting the links. State of controls is updated.
     * If scenarios a has no more links to assets control state is implemented: yes.
     * If there are any links left state is implemented: partly.
     * 
     * @param elementUuidSet Uuids of all elements in one process
     */
    int deleteAssetScenarioLinks(Set<String> elementUuidSet);
}

