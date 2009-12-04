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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.CnALink.Id;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ChangeLinkType extends GenericCommand {

	private Id linkId;
	private String linkTypeID;
	private String comment;

	/**
	 * @param id
	 * @param linkTypeID 
	 */
	public ChangeLinkType(Id id, String linkTypeID, String comment) {
		this.linkId = id;
		this.linkTypeID = linkTypeID;
		this.comment = comment;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		linkId = null;
		linkTypeID = null;
		comment = null;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		try {
			IBaseDao<CnALink, Serializable> dao = getDaoFactory().getDAO(CnALink.class);
			CnALink link = dao.findById(linkId);
			CnATreeElement dependant = link.getDependant();
			CnATreeElement dependency = link.getDependency();
			
			// links are immutable, so we have to recreate the link:
			
			RemoveLink<CnALink> command = new RemoveLink<CnALink>(linkId);
			command = getCommandService().executeCommand(command);
			
			CreateLink<CnALink, CnATreeElement, CnATreeElement> command2 = new CreateLink<CnALink, CnATreeElement, CnATreeElement>(dependant, dependency, linkTypeID, comment);
			command2 = getCommandService().executeCommand(command2);
			
		} catch (CommandException e) {
		}
	}

}
