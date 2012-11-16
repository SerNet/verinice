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
package sernet.verinice.service.commands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.SecurityException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CheckWritingPermission extends GenericCommand {

    private transient Logger log = Logger.getLogger(CheckWritingPermission.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CheckWritingPermission.class);
        }
        return log;
    }
   
    String uuid;
    
    String username; 
    
    boolean isWriteAllowed;
    
    public CheckWritingPermission(String uuid, String username) {
        super();
        this.uuid = uuid;
        this.username = username;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {         
            IBaseDao<CnATreeElement, Serializable> daoElement = (IBaseDao<CnATreeElement, Serializable>) getDaoFactory().getDAO(CnATreeElement.class);
            CnATreeElement element = null;
            try {
                LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid(uuid,new RetrieveInfo());
                command = getCommandService().executeCommand(command);
                element = command.getElement();
                if(element!=null) {
                    daoElement.checkRights(element, username);
                    setWriteAllowed(true);
                }
            } catch(SecurityException e) {
                if (log.isInfoEnabled()) {
                    log.info("User " + username + " is not allowed to write element: " + element);
                }
                if (log.isDebugEnabled()) {
                    log.debug("SecurityException stacktrace: ", e);
                }
                setWriteAllowed(false);                          
            }             
        } catch (Throwable t) {
            getLog().error("Error while checking writing permission for element uuid: " + uuid, t);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isWriteAllowed() {
        return isWriteAllowed;
    }

    public void setWriteAllowed(boolean isWriteAllowed) {
        this.isWriteAllowed = isWriteAllowed;
    }

  

}
