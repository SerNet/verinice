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
package sernet.verinice.interfaces.bpm;

import java.util.Date;
import java.util.List;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public interface ITask extends Comparable<ITask> {

    public static final String STYLE_READ = "read"; 
    
    public static final String STYLE_UNREAD = "unread"; 
    
    /**
     * @return
     */
    String getId();
    
    /**
     * @return
     */
    String getName();
    
    /**
     * @return
     */
    String getControlTitle();

    /**
     * @return
     */
    Date getCreateDate();
    
    /**
     * @return
     */
    Date getDueDate();
    
    /**
     * @return
     */
    String getUuid();

    /**
     * @return
     */
    String getType();
    
    String getSortValue();
    
    String getAssignee();
    
    /**
     * Returns a map with outcomes of this task.
     * Key is the id of the outcome, value the translated title.
     * 
     * @return a map with outcomes
     */
    List<KeyValue> getOutcomes();
    
    /**
     * Sets the map of outcomes of this task.
     * Key is the id of the outcome, value the translated title.
     * 
     * @param outcomeMap a map with outcomes
     */
    void setOutcomes(List<KeyValue> outcomes);
    
    void setIsRead(boolean isRead);
    
    boolean getIsRead();
    
    String getStyle();
    
    void setStyle(String style);
    
    String getUuidAudit();
    
    String getAuditTitle();

}
