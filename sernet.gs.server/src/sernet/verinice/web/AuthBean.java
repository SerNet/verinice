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
package sernet.verinice.web;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IRightsServerHandler;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class AuthBean {

    
    public String getUserName() {
        return getAuthService().getUsername();
    }
    
    public boolean getLogoutPossible() {
        return getAuthService().isLogoutPossible();
    }
    
    /**
     * This method is called from client to keep session alive
     * and prevents session timeout.
     */
    public void keepSessionAlive() {
        getAuthService().getUsername();
    }
    
    public boolean getBsiTasks() {
        return isEnabled(ActionRightIDs.BSIMASSNAHMEN);
    }
    
    public boolean getIsoTasks() {
        return isEnabled(ActionRightIDs.TASKVIEW);
    }
    
    public boolean getIsoElements() {
        return isEnabled(ActionRightIDs.ISMVIEWWEB);
    }
    
    public boolean getRelations() {
        return isEnabled(ActionRightIDs.RELATIONS);
    }
    
    public boolean getAttachments() {
        return isEnabled(ActionRightIDs.FILES);
    }
    
    public boolean getAddAttachments() {
        return isEnabled(ActionRightIDs.ADDFILE);
    }
    
    public boolean getDeleteAttachments() {
        return isEnabled(ActionRightIDs.DELETEFILE);
    }
    
    public boolean getAddElement() {
        return isEnabled(ActionRightIDs.ADDISMELEMENT);
    }
    
    public boolean getAddGroup() {
        return isEnabled(ActionRightIDs.ADDISMGROUP);
    }
    
    public boolean getAddOrg() {
        return isEnabled(ActionRightIDs.ADDISMORG);
    }
    
    public boolean isEnabled( String rightId) {
        return getRightsService().isEnabled(getAuthService().getUsername(), rightId);
    }
    
    private IAuthService getAuthService() {
        return (IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE);
    }
    
    private IRightsServerHandler getRightsService() {
        return (IRightsServerHandler) VeriniceContext.get(VeriniceContext.RIGHTS_SERVER_HANDLER);
    }
}
