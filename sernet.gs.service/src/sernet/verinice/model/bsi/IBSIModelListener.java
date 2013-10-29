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
package sernet.verinice.model.bsi;

import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.validation.CnAValidation;


public interface IBSIModelListener {
	
	public static final String SOURCE_BULK_EDIT 	= "source bulk edit";
	public static final String SOURCE_KONSOLIDATOR 	= "source konsolidator";
	public static final String SOURCE_EDITOR 		= "source editor";

	void childAdded(CnATreeElement category, CnATreeElement child);

	void childRemoved(CnATreeElement category, CnATreeElement child);

	void childChanged(CnATreeElement child);
	
	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	void modelRefresh();

	/**
	 * Request complete refresh of model, it depends on the listener how this method will
	 * be implemented, i.e. what parts of the model really need to be refreshed (only visible objects etc.).
	 * 
	 * @param source the cause or source for this model refresh. Should be the object causing the model change
	 * or one of the predefined sources given in {@link IBSIModelListener}. 
	 */
	void modelRefresh(Object source);
	
	void linkChanged(CnALink oldLink, CnALink newLink, Object source);
	
	void linkRemoved(CnALink link);
	
	void linkAdded(CnALink link);
	
	
	
	/**
	 * New element was added to the database (by another client), 
	 * hint to view that it should reload from database.
	 * @param child
	 */
	void databaseChildAdded(CnATreeElement child);
	
	/**
	 * Element was deleted in database.
	 * Hint to view that it should reload from database.
	 * 
	 * @param child
	 */
	void databaseChildRemoved(CnATreeElement child);

	/**
	 * Element was removed.
	 * Hint to view that it should reload from database.
	 * 
	 * @param entry entry from transaction log with details about deleted object
	 */
	void databaseChildRemoved(ChangeLogEntry entry);
	
	/**
	 * Element was changed in the database either by another client or by a cascading action performed
	 * on the server.
	 * 
	 * Hint to view that it should reload from the database.
	 * 
	 * @param child
	 */
	void databaseChildChanged(CnATreeElement child);
	
	public void modelReload(BSIModel newModel);
	
	public void validationAdded(Integer scopeId);
	
	public void validationRemoved(Integer scopeId);
	
	public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation);
	
}
