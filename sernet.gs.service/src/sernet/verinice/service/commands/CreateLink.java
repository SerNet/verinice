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
package sernet.verinice.service.commands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Create and save new element of type type to the database using its class to lookup
 * the DAO from the factory.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
public class CreateLink<T extends CnALink, U extends CnATreeElement, V extends CnATreeElement> 
extends GenericCommand {
    
    private transient Logger log = Logger.getLogger(CreateLink.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CreateLink.class);
        }
        return log;
    }

    private U dependant;
	private V dependency;
	private CnALink link;
	private String relationId;
	private String comment;

	public CreateLink(U dependant, V dependency) {
		this(dependant, dependency, "", "");
	}
	
	public CreateLink(U dependant, V dependency, String relationId) {
		this(dependant, dependency, relationId, "");
	}
	
	public CreateLink(U dependant, V dependancy, String relationId, String comment) {
		this.dependant = dependant;
		this.dependency = dependancy;
		this.relationId = relationId;
		this.comment = comment;
	}
	
	public void execute() {
	    if (getLog().isDebugEnabled()) {
            getLog().debug("Creating link from " + dependency.getTypeId() + " to " + dependant.getTypeId());
        }
	    
		IBaseDao<CnALink, Serializable> linkDao 
			= (IBaseDao<CnALink, Serializable>) getDaoFactory().getDAO(CnALink.class);
		
		IBaseDao<U, Serializable> targetDao 
		= (IBaseDao<U, Serializable>) getDaoFactory().getDAO(dependant.getTypeId());

		IBaseDao<V, Serializable> draggedDao 
		= (IBaseDao<V, Serializable>) getDaoFactory().getDAO(dependency.getTypeId());

//		// if dependancy or dependant are cglib enhanced, we won't get a DAO, but in this case we don't need to reload
//		// because we're already inside the session:
//		if (draggedDao != null && targetDao != null) {
//			draggedDao.reload(dependancy, dependancy.getDbId());
//			targetDao.reload(dependant, dependant.getDbId());
//		}
		dependency = draggedDao.findById(dependency.getDbId());
		dependant = targetDao.findById(dependant.getDbId());
		
		link = new CnALink(dependant, dependency, relationId, comment);
		try {
		    linkDao.merge(link, true);
        } catch (Exception e) {
            getLog().error("Could not create link from " + dependency.getTypeId() + " to " + dependant.getTypeId() );
        }
		
	}

	public CnALink getLink() {
		return link;
	}


}
