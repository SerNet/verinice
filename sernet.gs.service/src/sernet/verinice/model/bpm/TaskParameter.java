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
package sernet.verinice.model.bpm;

import java.io.Serializable;
import java.util.Date;

import sernet.verinice.interfaces.bpm.ITaskParameter;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TaskParameter implements ITaskParameter, Serializable {
 
    Boolean read;
    
    Boolean unread;
    
    Date since;
    
    String username;
    
    String auditUuid;
    
    boolean allUser;
    
    String processKey;
    
    String taskId;
    
    public TaskParameter() {
        super();
    }

    /**
     * @param username2
     */
    public TaskParameter(String username) {
        super();
        setUsername(username);
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskParameter#getUnread()
     */
    public Boolean getUnread() {
        return unread;
    }

    public void setUnread(Boolean unread) {
        this.unread = unread;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuditUuid() {
        return auditUuid;
    }

    public void setAuditUuid(String auditUuid) {
        this.auditUuid = auditUuid;
    }

    public boolean getAllUser() {
        return allUser;
    }

    public void setAllUser(boolean allUser) {
        this.allUser = allUser;
    }

    public String getProcessKey() {
        return processKey;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }


}
