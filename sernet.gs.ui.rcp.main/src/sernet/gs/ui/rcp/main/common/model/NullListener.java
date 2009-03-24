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

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;

/**
 * Default listener used inside model. Does not do anything.
 *
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class NullListener implements IBSIModelListener {

	public void childAdded(CnATreeElement category, CnATreeElement child) {
		// do nothing
	}

	public void childChanged(CnATreeElement category, CnATreeElement child) {
		// do nothing

	}

	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		// do nothing

	}

	public void linkChanged(CnALink link) {
		// do nothing

	}

	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	public void modelRefresh() {
		modelRefresh(null);
	}

	public void modelRefresh(Object source) {
		// do nothing
	}

	public void linkRemoved(CnALink link) {
		// do nothing
		
	}
	
	public void linkAdded(CnALink link) {
		// do nothing
	}

	public void databaseChildAdded(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void databaseChildChanged(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void databaseChildRemoved(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void modelReload(BSIModel newModel) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildRemoved(sernet.gs.ui.rcp.main.common.model.ChangeLogEntry)
	 */
	public void databaseChildRemoved(ChangeLogEntry entry) {
		// TODO Auto-generated method stub
		
	}

}
