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
 *     Daniel Murygin <dm[at]sernet[dot]de> - before/afterUpdate added
  ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.SecurityException;
import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/** 
 * Update the entity of an element only, leaving all collections as they were.
 * Accessing the element is subject to write permissions.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
public class UpdateElementEntity<T extends CnATreeElement> extends GenericCommand implements IChangeLoggingCommand {

    private transient Logger log = Logger.getLogger(UpdateElementEntity.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(UpdateElementEntity.class);
        }
        return log;
    }
    
	private T newElement;
	private boolean fireupdates;
	
	private String stationId;

	public UpdateElementEntity(T element, boolean fireUpdates, String stationId) {
		this.newElement = element;
		this.fireupdates = fireUpdates;
		this.stationId = stationId;
	}

	public void execute() {
	    beforeUpdate();
	    IBaseDao<T, Serializable> elementDao = getDaoFactory().getDAO(newElement.getTypeId());
        try {
            elementDao.checkRights(newElement);
        } catch(SecurityException e) {
            getLog().warn("Can not update entity of element: " + newElement + " security check fails:" + e.getMessage());
            if (getLog().isDebugEnabled()) {
                getLog().debug("stacktrace: ", e);
            }
            throw e;
        }
	    
	    Entity newEntity = newElement.getEntity();
	    IBaseDao<Entity, Serializable> entityDao = getDaoFactory().getDAO(Entity.class);
	    newEntity = entityDao.merge(newEntity);
	    newElement.setEntity(newEntity);
	    afterUpdate();
	}

	/**
     * Called before element entity is updated.
     * You can override this in subclassed to add additional functionality
     */
    protected void beforeUpdate() {
        // empty
    }

    /**
     * Called after element entity was updated.
     * You can override this in subclassed to add additional functionality
     */
    protected void afterUpdate() {
        // empty
    }

    public T getElement() {
		return newElement;
	}

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#getChangeType()
	 */
	public int getChangeType() {
		return ChangeLogEntry.TYPE_UPDATE;
	}


	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		if (newElement instanceof CnATreeElement) {
			ArrayList<CnATreeElement> list = new ArrayList<CnATreeElement>(1);
			list.add((CnATreeElement) newElement);
			return list;
		}
		return null;
	}

}
