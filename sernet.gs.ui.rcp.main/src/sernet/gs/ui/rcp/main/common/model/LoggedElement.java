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

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class LoggedElement implements Serializable {
	
	// hibernate constructor
	LoggedElement() {}

	public LoggedElement(CnATreeElement elmt, ChangeLogEntry logEntry) {
		super();
		this.elmt = elmt;
		this.logEntry = logEntry;
	}

	private CnATreeElement elmt;
	private ChangeLogEntry logEntry;

	public CnATreeElement getElmt() {
		return elmt;
	}

	public void setElmt(CnATreeElement elmt) {
		this.elmt = elmt;
	}

	public ChangeLogEntry getLogEntry() {
		return logEntry;
	}

	public void setLogEntry(ChangeLogEntry logEntry) {
		this.logEntry = logEntry;
	}

	public boolean equals(Object o) {
		if (o != null && o instanceof LoggedElement) {
			LoggedElement that = (LoggedElement) o;
			return this.getElmt().getDbId().equals(that.getElmt().getDbId())
				&& this.getLogEntry().getDbId().equals(that.getLogEntry().getDbId());
		} else {
			return false;
		}
	}

	public int hashCode() {
		return elmt.hashCode() + logEntry.hashCode();
	}

}
