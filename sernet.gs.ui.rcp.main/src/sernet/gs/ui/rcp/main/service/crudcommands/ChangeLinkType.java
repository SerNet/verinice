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

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.common.model.CnALink.Id;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.DAOFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * Changes the link type. Because links are immutable, the link will be deleted and inserted again with new link type.
 * The newly created link will be in the command after execution and requested using the getLink() method.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ChangeLinkType extends GenericCommand {

	private CnALink link;
	
	public CnALink getLink() {
		return link;
	}

	private String linkTypeID;
	private String comment;

	/**
	 * Change the link type of a link.
	 * Because links are immutable, the link has to be deleted an recreated.
	 * 
	 * @param link The link that should be changed
	 * @param linkTypeID the new id of the link type according to the XML definition, i.e. "server_to_person_responsible"
	 * @param comment a user's comment that should be saved along with the link
	 */
	public ChangeLinkType(CnALink link, String linkTypeID, String comment) {
		this.link = link;
		this.linkTypeID = linkTypeID;
		this.comment = comment;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		linkTypeID = null;
		comment = null;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		try {
			IBaseDao<CnALink, Serializable> dao = getDaoFactory().getDAO(CnALink.class);
			dao.reload(link, link.getId());

			
			CnATreeElement dependant = link.getDependant();
			CnATreeElement dependency = link.getDependency();
			// links are immutable, so we have to recreate the link:
			RemoveLink<CnALink> command3 = new RemoveLink<CnALink>(link);
			command3 = getCommandService().executeCommand(command3);
			
			CreateLink<CnALink, CnATreeElement, CnATreeElement> command4 
				= new CreateLink<CnALink, CnATreeElement, CnATreeElement>(dependant, dependency, linkTypeID, comment);
			command4 = getCommandService().executeCommand(command4);
			link = command4.getLink();
			
			
		} catch (CommandException e) {
		}
	}

}
