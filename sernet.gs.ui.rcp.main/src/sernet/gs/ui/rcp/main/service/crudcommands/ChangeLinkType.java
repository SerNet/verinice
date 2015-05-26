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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.RemoveLink;

/**
 * Changes the link type. Because links are immutable, the link will be deleted and inserted again with new link type.
 * The newly created link will be in the command after execution and requested using the getLink() method.
 * Because the created link is directly used to refresh the view, this command hydrates the newly created link 
 * before returning it to the view layer.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ChangeLinkType extends GenericCommand {

	private CnALink link;
	
	private transient Logger log = Logger.getLogger(ChangeLinkType.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ChangeLinkType.class);
        }
        return log;
    }
	
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
	    if (getLog().isDebugEnabled()) {
            getLog().debug("Changing link type.");
        }
	    
		try {
			IBaseDao<CnALink, Serializable> dao = getDaoFactory().getDAO(CnALink.class);
			link = dao.findById(link.getId());
			
			if (link == null) {
			    if (getLog().isDebugEnabled()) {
                    getLog().warn("Could not find link to change.");
                }
			    return;
			}

			CnATreeElement dependant = link.getDependant();
			CnATreeElement dependency = link.getDependency();
			
			// links are immutable, so we have to recreate the link:
			RemoveLink<CnALink> command3 = new RemoveLink<CnALink>(link);
			getCommandService().executeCommand(command3);
			
			CreateLink<CnALink, CnATreeElement, CnATreeElement> command4 
				= new CreateLink<CnALink, CnATreeElement, CnATreeElement>(dependant, dependency, linkTypeID, comment);
			command4 = getCommandService().executeCommand(command4);
			link = command4.getLink();
			
			HydratorUtil.hydrateElement(dao, link.getDependency(), false);
			HydratorUtil.hydrateElement(dao, link.getDependant(), false);
			
		} catch (CommandException e) {
		}
	}


}
