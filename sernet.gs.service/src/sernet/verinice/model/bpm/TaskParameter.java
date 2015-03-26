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
import java.util.List;
import java.util.Set;

import sernet.verinice.interfaces.bpm.ITaskParameter;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TaskParameter implements ITaskParameter, Serializable {
 
    private Boolean read = Boolean.TRUE;    
    private Boolean unread = Boolean.TRUE;   
    private Date since;
    private Date dueDateFrom;
    private Date dueDateTo;
    private String username;
    private String auditUuid;
    private List<String> groupIdList;
    private boolean allUser;
    private String processKey;
    private String taskId;
    private Set<String> blacklist;
    
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

    @Override
    public Boolean getRead() {    
        return read;
    }

    @Override
    public void setRead(Boolean read) {
        this.read = read;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskParameter#getUnread()
     */
    @Override
    public Boolean getUnread() {
        return unread;
    }

    @Override
    public void setUnread(Boolean unread) {
        this.unread = unread;
    }

    @Override
    public Date getSince() {
        return since;
    }

    @Override
    public void setSince(Date since) {
        this.since = since;
    }

    public Date getDueDateFrom() {
        return dueDateFrom;
    }

    public void setDueDateFrom(Date dueDateFrom) {
        this.dueDateFrom = dueDateFrom;
    }

    public Date getDueDateTo() {
        return dueDateTo;
    }

    public void setDueDateTo(Date dueDateTo) {
        this.dueDateTo = dueDateTo;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getAuditUuid() {
        return auditUuid;
    }

    @Override
    public void setAuditUuid(String auditUuid) {
        this.auditUuid = auditUuid;
    }
    
    @Override
    public List<String> getGroupIdList() {
        return this.groupIdList;
    }
    
    @Override
    public void setGroupIdList(List<String> groupIdList) {
        this.groupIdList = groupIdList;
    }

    @Override
    public boolean getAllUser() {
        return allUser;
    }

    @Override
    public void setAllUser(boolean allUser) {
        this.allUser = allUser;
    }

    @Override
    public String getProcessKey() {
        return processKey;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public void setProcessKey(String processKey) {
        this.processKey = processKey;
    }

    @Override
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Set<String> getBlacklist() {
        return blacklist;
    }

    @Override
    public void setBlacklist(Set<String> blacklist) {
        this.blacklist = blacklist;
    }


}
