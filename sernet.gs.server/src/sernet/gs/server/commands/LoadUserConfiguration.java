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
package sernet.gs.server.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.common.configuration.Configuration;

@SuppressWarnings("serial")
public class LoadUserConfiguration extends GenericCommand {
	
	private static final String QUERY = "from Entity entity " +
	"join fetch entity.typedPropertyLists " +
	"where entity.entityType = ?"; //$NON-NLS-1$
	
	private String username = "";
	

	/**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    private List<Entity> entities;

	public LoadUserConfiguration() {
	}

	public void execute() {
		IBaseDao<Entity, Serializable> dao = getDaoFactory().getDAO(Entity.class);
		entities = dao.findByQuery(QUERY, new String[] {"configuration"});
		// only searched user if present, otherwise return all:
		if (username != null && username.length()>0) {
		    allResults: for (Entity entity : entities) {
		        if (username.equals(entity.getSimpleValue(Configuration.PROP_USERNAME))) {
		            //HydratorUtil.hydrateEntity(dao, entity);
		            entities = new ArrayList<Entity>();
		            entities.add(entity);
		            break allResults;
		        }
		    }
		}
		else {
		    allResults: for (Entity entity : entities) {
		        HydratorUtil.hydrateEntity(dao, entity);
		    }
		}
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.interfaces.GenericCommand#clear()
	 */
	@Override
	public void clear() {
	    super.clear();
	    this.username = "";
	}
	
	

}
