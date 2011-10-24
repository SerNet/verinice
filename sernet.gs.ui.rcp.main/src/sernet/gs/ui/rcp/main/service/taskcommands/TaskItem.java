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
package sernet.gs.ui.rcp.main.service.taskcommands;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TaskItem implements Comparable<TaskItem> {
    
    private Integer id;
    
    private Long timestamp;

    public TaskItem(Integer id, Object timestamp) {
        super();
        this.id = id;
        if(timestamp instanceof Long) {
            this.timestamp = (Long) timestamp;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TaskItem o) {
        final int LESS = -1;
        final int EQUAL = 0;
        final int GREATER = 1;
        int result = GREATER;
        if(o!=null) {
            if(o.getTimestamp()!=null) {
                if(this.getTimestamp()!=null) {
                    result = this.getTimestamp().compareTo(o.getTimestamp());
                } else {
                    result = LESS;
                }
            } else if(this.getTimestamp()==null) {
                result = EQUAL;
            }
        }
        return result;
    }
}
