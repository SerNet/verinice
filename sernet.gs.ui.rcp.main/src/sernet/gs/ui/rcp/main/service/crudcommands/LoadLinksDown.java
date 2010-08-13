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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This command loads the accessory links (linksDown) for a CnATreeElement.
 * To excute 
 * <ul>
 * <li>create this command by passing a CnATreeElement</li>
 * <li>call method <code>execute()</code></li>
 * <li>get links by calling method <code>getLinksDown()</code></li>
 * </ul>
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LoadLinksDown extends GenericCommand {

	private static final long serialVersionUID = 130016664627158967L;

	private static final Logger LOG = Logger.getLogger(LoadLinksDown.class);
	
	private CnATreeElement parent;
	
	private Set<CnALink> linksDown;
	
	/**
	 * Creates a new command to load the accessory links of <code>parent</code>.
	 * 
	 * @param parent A CnATreeElement, must not be null
	 */
	public LoadLinksDown(CnATreeElement parent) {
		super();
		this.parent = parent;
	}

	/**
	 * Executes this command.
	 * Loads accessory links of <code>CnATreeElement parent</code> by calling dao method retrieve.
	 * 
	 * @see sernet.verinice.interfaces.ICommand#execute()
	 */
	public void execute() {
		if(this.parent==null) {
			LOG.error("Can not execute command, parent is null");
			throw new RuntimeException("Can not execute command, parent is null");
		}
		if(this.parent.getDbId()==null) {
			LOG.error("Can not execute command, dbid of parent is null");
			throw new RuntimeException("Can not execute command, dbif of parent is null");
		}
		
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(this.parent.getTypeId());
		
		RetrieveInfo ri = new RetrieveInfo();
		ri.setLinksDown(true).setLinksDownProperties(true);
		CnATreeElement parentWithLinksDown = dao.retrieve(parent.getDbId(),ri);
		linksDown = parentWithLinksDown.getLinksDown();
	}

	/**
	 * Returns the accessory links of <code>CnATreeElement parent</code>.
	 * Call exceute before this method. Otherwise null is returned.
	 * 
	 * @return the accessory links of <code>CnATreeElement parent</code>
	 */
	public Set<CnALink> getLinksDown() {
		return linksDown;
	}

}
