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

import sernet.verinice.interfaces.bpm.ITask;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TaskInformation implements ITask,Serializable {

    
    String id;
    
    Date createDate;
    
    String name;
    
    String controlTitle;
    
    String controlUuid;
    
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
    public String getControlUuid() {
        return controlUuid;
    }

    public void setControlUuid(String controlUuid) {
        this.controlUuid = controlUuid;
    }

    

}
