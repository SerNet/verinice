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

import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.KeyValue;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TaskInformation implements ITask,Serializable {

    String id;
    
    Date createDate;
    
    Date dueDate;
    
    String name;
    
    String controlTitle;
    
    String uuid;
    
    String type;
    
    String sortValue;
    
    List<KeyValue> outcomeList;

    boolean isRead = false;
    
    String style;

    private String uuidAudit;

    private String auditTitle;
    
    private String assignee;
    
    /**
     * 
     */
    public TaskInformation() {
        super();
    }
    
    public TaskInformation(String name, Date createDate) {
        super();
        this.name = name;
        this.createDate = createDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#getCreateDate()
     */
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#getName()
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#getControlTitle()
     */
    public String getControlTitle() {
        return controlTitle;
    }

    public void setControlTitle(String controlTitle) {
        this.controlTitle = controlTitle;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#getControlUuid()
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSortValue() {
        return sortValue;
    }

    public void setSortValue(String sortValue) {
        this.sortValue = sortValue;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#getOutcomes()
     */
    public List<KeyValue> getOutcomes() {
        return outcomeList;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#setOutcomes(java.util.Set)
     */
    @Override
    public void setOutcomes(List<KeyValue> outcomes) {
        this.outcomeList = outcomes;        
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#isRead()
     */
    @Override
    public boolean getIsRead() {
        return isRead;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#setIsRead(boolean)
     */
    @Override
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;     
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
    
    /**
     * @param uuidAudit
     */
    public void setUuidAudit(String uuidAudit) {
        this.uuidAudit = uuidAudit;  
    }

    public String getUuidAudit() {
        return uuidAudit;
    }

    /**
     * @param title
     */
    public void setAuditTitle(String title) {
       this.auditTitle =title;
    }

    public String getAuditTitle() {
        return auditTitle;
    }

  

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TaskInformation other = (TaskInformation) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ITask o) {
        int result = 0;
        if(this.getName()!=null && o!=null && o.getName()!=null) {
            result = this.getName().compareTo(o.getName());
        }
        if(result==0
           && this.getSortValue()!=null && o!=null && o.getSortValue()!=null) {
            result = this.getSortValue().compareTo(o.getSortValue());
        }
        return result;
    }

}
