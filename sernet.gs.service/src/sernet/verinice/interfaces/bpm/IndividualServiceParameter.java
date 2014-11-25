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
package sernet.verinice.interfaces.bpm;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndividualServiceParameter implements Serializable, Comparable<IndividualServiceParameter> {

    private String uuid;
    
    private String typeId;
    
    private String assignee;
    
    private String assigneeRelationId;

    private String assigneeRelationName;
    
    private Date dueDate;
    
    private String title;
    
    private String description;
    
    private Integer reminderPeriodDays;
    
    private Set<String> properties;

    private Set<String> propertyNames;

    public String getUuid() {
        return uuid;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getAssigneeRelationId() {
        return assigneeRelationId;
    }

    public void setAssigneeRelationId(String assigneeRelationId) {
        this.assigneeRelationId = assigneeRelationId;
    }
    

    public void setAssigneeRelationName(String assigneeRelationName) {
        this.assigneeRelationName = assigneeRelationName;      
    }

    public String getAssigneeRelationName() {
        return assigneeRelationName;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getReminderPeriodDays() {
        return reminderPeriodDays;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReminderPeriodDays(Integer reminderPeriodDays) {
        this.reminderPeriodDays = reminderPeriodDays;
    }

    public Set<String> getProperties() {
        return properties;
    }

    public void setProperties(Set<String> properties) {
        this.properties = properties;
    }
    
    public void setPropertyNames(Set<String> propertyNames) {
        this.propertyNames = propertyNames;       
    }

    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(IndividualServiceParameter param) {
        int result = -1;
        if(param!=null && param.getTitle()!=null) {
            if(this.getTitle()!=null) {
                result = this.getTitle().compareTo(param.getTitle());
            } else {
                result = 1;
            }
        }
        return result;
    }

    

    
}
