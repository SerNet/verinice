/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.common;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.ITypedElement;

/**
 * Transaction log to log modifications to database items.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
@SuppressWarnings("serial")
public class ChangeLogEntry implements Serializable, ITypedElement {

    /**
     * Changes to cnatree elements:
     */
	public static final int TYPE_UPDATE = 0;
	public static final int TYPE_INSERT = 1;
	public static final int TYPE_DELETE = 2;
	
	/**
	 * Changes to permissions, may cause the client to check if it can still read 
	 * displayed items etc.
	 */
	public static final int TYPE_PERMISSION = 3;
	
	/**
	 * Audit trails such as password changes etc. Mostly for later 
	 * investigation by a human. Can be ignored by the client.
	 */
	public static final int TYPE_SYSTEM = 4;

	private Integer dbId;

	private Integer elementId;
	private String elementClass;
	private Date changetime;
	private int change;
	private String stationId;
	private String username;

	/**
	 * Session ID to identify changes made by a particular client during its
	 * lifetime. Static value is initialized on client and used in commands that
	 * are transferred to the server.
	 */
	public final static String STATION_ID = UUID.randomUUID().toString();
    public static final String TYPE_ID = "changelogentry";

	public String getStationId() {
		return stationId;
	}

	ChangeLogEntry() {
		// default constructor for hibernate
	}
	
	 /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }

	/**
	 * Change log entry.
	 * 
	 * @param element
	 *            the element that was changes
	 * @param change
	 *            type of change
	 * @param username
	 *            executing user
	 * @param stationId
	 *            session id of client, used to filter events from self when
	 *            notifying changes.
	 */
	public ChangeLogEntry(CnATreeElement element, int change, String username,
			String stationId, Date now) {
		if (element == null) {
			Logger.getLogger(this.getClass()).debug(
					"Logging attempt for 'null' element failed.");
			return;
		}

		this.elementId = element.getDbId();
		this.elementClass = element.getClass().getName();
		this.change = change;
		this.username = username;
		this.stationId = stationId;
		this.changetime = now;
	}

	public String getElementClass() {
		return elementClass;
	}

	public Date getChangetime() {
		return changetime;
	}

	public Integer getElementId() {
		return elementId;
	}

	public void setElementId(Integer elementId) {
		this.elementId = elementId;
	}

	public int getChange() {
		return change;
	}

	public void setChange(int change) {
		this.change = change;
	}
	
	public String getChangeDescription() {
		String desc = null;
		switch(this.change) {
			case ChangeLogEntry.TYPE_UPDATE:
				desc = "update";
				break;
			case ChangeLogEntry.TYPE_INSERT:
				desc = "insert";
				break;
			case ChangeLogEntry.TYPE_DELETE:
				desc = "delete";
				break;
			case ChangeLogEntry.TYPE_PERMISSION:
				desc = "permission";
				break;
			case ChangeLogEntry.TYPE_SYSTEM:
			    desc= "system";
			    break;
			default:
				desc = "unknown, id: " + this.change;	
		}
		return desc;
	}

	public void setElementClass(String elementClass) {
		this.elementClass = elementClass;
	}

	public void setChangetime(Date changetime) {
		this.changetime = changetime;
	}

	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
