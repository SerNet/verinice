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

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.KeyValue;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TaskInformation implements ITask, Serializable {

    private static final Logger LOG = Logger.getLogger(TaskInformation.class);
    
    private static final NumericStringComparator COMPARATOR = new NumericStringComparator();
    
    private String id;
    
    private String type;
    
    private Date createDate;
    
    private Date dueDate;
    
    private String name;
    
    private String processName;
    
    private String description;
    
    private String elementTitle;
    
    private String uuid;
    
    private String elementType;
    
    private String sortValue;
    
    private List<KeyValue> outcomeList;

    private boolean isRead = false;
    
    private String style;

    private String uuidGroup;

    private String groupTitle;
    
    private String assignee;
    
    private boolean processed = false;
    
    private String priority;
    
    private Set<String> properties;
    
    private boolean withAReleaseProcess = false;
    
    /**
     * 
     */
    public TaskInformation() {
        super();
    }
    
    public TaskInformation(String name, Date createDate) {
        super();
        this.name = name;
        this.createDate = (createDate != null)  ? (Date)createDate.clone() : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#getCreateDate()
     */
    public Date getCreateDate() {
        return (this.createDate != null) ? (Date)this.createDate.clone() : null;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = (createDate != null) ? (Date)createDate.clone() : null;
    }

    public Date getDueDate() {
        return (this.dueDate != null) ? (Date)this.dueDate.clone() : null;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = (dueDate != null) ? (Date)dueDate.clone() : null;
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

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITask#getControlTitle()
     */
    public String getElementTitle() {
        return elementTitle;
    }

    public void setElementTitle(String elementTitle) {
        this.elementTitle = elementTitle;
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

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String type) {
        this.elementType = type;
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
        addStyle((isRead) ? ITask.STYLE_READ : ITask.STYLE_UNREAD);
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
    
    public void addStyle(String style) {
        if(this.style==null) {
            setStyle(style);
        } else {
            this.style = this.style + " " + style;
        }
    }
    
    /**
     * @param uuidGroup
     */
    public void setUuidGroup(String uuidGroup) {
        this.uuidGroup = uuidGroup;  
    }

    public String getUuidGroup() {
        return uuidGroup;
    }

    /**
     * @param title
     */
    public void setGroupTitle(String title) {
       this.groupTitle =title;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

  

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
    
    
    /**
     * @param processed the processed to set
     */
    public void setIsProcessed(boolean processed) {
        this.processed = processed;
        addStyle((processed) ? ITask.STYLE_PROCESSED : ITask.STYLE_UNPROCESSED);
    }

    @Override
    public boolean getIsProcessed() {
        return processed;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Set<String> getProperties() {
        return properties;
    }

    public void setProperties(Set<String> properties) {
        this.properties = properties;
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
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        TaskInformation other = (TaskInformation) obj;
        if (id == null) {
            if (other.id != null){
                return false;
            }
        } else if (!id.equals(other.id)){
            return false;
        }
        return true;
    }

    /**
     * Sort order: groupTitle, elementTitle, processName, name
     * NumericStringComparator is used for sorting.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ITask t) {
        setSortValue(getGroupTitle());
        t.setSortValue(t.getGroupTitle());
        int result = compareBySortValue(t);
        if(result==0) {
            setSortValue(getElementTitle());
            t.setSortValue(t.getElementTitle());
            result = compareBySortValue(t);  
        }
        if(result==0) {
            setSortValue(getProcessName());
            t.setSortValue(t.getProcessName());
            result = compareBySortValue(t);  
        }
        if(result==0) {
            setSortValue(getName());
            t.setSortValue(t.getName());
            result = compareBySortValue(t);  
        }
        return result;
    }
    
    public int compareBySortValue(ITask t) {
        int result = 0;
        if(this.getSortValue()!=null && t!=null && t.getSortValue()!=null) {
            result = COMPARATOR.compare(this.getSortValue(), t.getSortValue());
            if (LOG.isDebugEnabled()) {
                LOG.debug("compare: " + result + " (" + this.getSortValue() + " and " + t.getSortValue() + ")");
            }
        }
        return result;
    }

    /**
     * @return the withAReleaseProcess
     */
    public boolean isWithAReleaseProcess() {
        return withAReleaseProcess;
    }

    /**
     * @param withAReleaseProcess the withAReleaseProcess to set
     */
    public void setWithAReleaseProcess(boolean withAReleaseProcess) {
        this.withAReleaseProcess = withAReleaseProcess;
    }
}
