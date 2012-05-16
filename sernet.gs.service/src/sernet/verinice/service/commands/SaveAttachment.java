/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import sernet.verinice.model.bsi.AttachmentFile;

/**
 * Saves AttachmentFiles in database.
 * Sets element to null in clear to remove
 * large file data.
 * 
 * @see AttachmentEditor
 * @author Daniel <dm[at]sernet[dot]de>
 */
public class SaveAttachment extends SaveElement<AttachmentFile> {

	/**
	 * @param element
	 */
	public SaveAttachment(AttachmentFile element) {
		super(element);
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.crudcommands.SaveElement#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		element = null;
	}

}
