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

import sernet.verinice.bpm.ProcessContext;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaControlFlowContext extends ProcessContext {
    
    private Control control;
    
    private ControlGroup controlGroup;
    
    private String uuidAudit;
    

    public IsaControlFlowContext() {
        super();
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public ControlGroup getControlGroup() {
        return controlGroup;
    }

    public void setControlGroup(ControlGroup controlGroup) {
        this.controlGroup = controlGroup;
    }

    public String getUuidAudit() {
        return uuidAudit;
    }

    public void setUuidAudit(String uuidAudit) {
        this.uuidAudit = uuidAudit;
    }
    
}
