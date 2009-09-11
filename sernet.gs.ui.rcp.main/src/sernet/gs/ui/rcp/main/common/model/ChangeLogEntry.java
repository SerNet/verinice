/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Transaction log to log modifications to database items.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
@SuppressWarnings("serial")
public class ChangeLogEntry implements Serializable {

	public static final int TYPE_UPDATE = 0;
	public static final int TYPE_INSERT = 1;
	public static final int TYPE_DELETE = 2;
	public static final int TYPE_PERMISSION = 3;

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

	public String getStationId() {
		return stationId;
	}

	ChangeLogEntry() {
		// default constructor for hibernate
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

	/**
	 * @return
	 */
	public String getChangeDescription() {
		switch (this.change) {
		case TYPE_UPDATE:
			return "update";

		case TYPE_DELETE:
			return "delete";

		case TYPE_INSERT:
			return "insert";
		default:
			break;
		}
		return "";
	}

}
