/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public interface IISO27KModelListener {
	
	/**
	 * Element was deleted in database.
	 * Hint to view that it should reload from database.
	 * 
	 * @param child
	 */
	void databaseChildRemoved(CnATreeElement child);

	/**IBSIModelListener
	 * @param category
	 * @param child
	 */
	void childAdded(CnATreeElement category, CnATreeElement child);

	/**
	 * @param child
	 */
	void databaseChildAdded(CnATreeElement child);
	
	/**
	 * @param object
	 */
	void modelRefresh(Object object);

	/**
	 * @param category
	 * @param child
	 */
	void childRemoved(CnATreeElement category, CnATreeElement child);

	/**
	 * @param category
	 * @param child
	 */
	void childChanged(CnATreeElement category, CnATreeElement child);

	/**
	 * @param child
	 */
	void databaseChildChanged(CnATreeElement child);

	/**
	 * @param entry
	 */
	void databaseChildRemoved(ChangeLogEntry entry);

	/**
	 * @param link
	 */
	void linkChanged(CnALink old, CnALink link, Object source);

	/**
	 * @param link
	 */
	void linkRemoved(CnALink link);

	/**
	 * @param link
	 */
	void linkAdded(CnALink link);

	/**
	 * @param newModel
	 */
	void modelReload(ISO27KModel newModel);
	


	
}
