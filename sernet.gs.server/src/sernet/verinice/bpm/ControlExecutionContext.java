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
package sernet.verinice.bpm;

import java.io.Serializable;

import sernet.verinice.model.iso27k.IControl;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ControlExecutionContext implements Serializable {
    
    private String adminUuid;
    
    private String assigneeUuid;
    
    private String controlUuid;
    
    private String implementation;
    
    public ControlExecutionContext(String assigneeUuid, String controlUuid) {
        super();
        this.assigneeUuid = assigneeUuid;
        this.controlUuid = controlUuid;
        setImplementation(IControl.IMPLEMENTED_NOTEDITED);
    }

    public String getAdminUuid() {
        return adminUuid;
    }

    public void setAdminUuid(String adminUuid) {
        this.adminUuid = adminUuid;
    }

    public String getAssigneeUuid() {
        return assigneeUuid;
    }

    public void setAssigneeUuid(String assigneeUuid) {
        this.assigneeUuid = assigneeUuid;
    }

    public String getControlUuid() {
        return controlUuid;
    }

    public void setControlUuid(String controlUuid) {
        this.controlUuid = controlUuid;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

}
