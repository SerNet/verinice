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

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.TimeFormatter;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

public class LoadElementByUuid<T extends CnATreeElement> extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadElementByUuid.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadElementByUuid.class);
        }
        return log;
    }
    
	private String uuid;
	protected T element;
    private String typeId;
    private RetrieveInfo ri;

    private transient IBaseDao<T, Serializable> dao;
    
    
	public LoadElementByUuid(String uuid) {
        this(null,uuid,null);
    }
	
	public LoadElementByUuid(String uuid, RetrieveInfo ri) {
        this(null,uuid,ri);
    }

    public LoadElementByUuid(String typeId, String uuid) {
        this(typeId,uuid,null);
    }

    public LoadElementByUuid(String typeId, String uuid, RetrieveInfo ri) {
		super();
        this.uuid= uuid;
		this.typeId = typeId;
		if(ri!=null) {
		    this.ri=ri;
		} else {
		    this.ri = new RetrieveInfo();
		}
	}
	
    public void execute() {
        long start = 0;
        if (getLog().isDebugEnabled()) {
            start = System.currentTimeMillis();
            getLog().debug("execute() called ..."); //$NON-NLS-1$
        }
        element = getDao().findByUuid(this.uuid,ri);       
        if (getLog().isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            getLog().debug("execute() finished in: " + TimeFormatter.getHumanRedableTime(duration)); //$NON-NLS-1$
        }		
    }

	public T getElement() {
		return element;
	}

    /**
     * @return the dao
     */
    public IBaseDao<T, Serializable> getDao() {
        if(dao==null) {
            if(typeId==null) {
                dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(CnATreeElement.class);
            } else {
                dao = getDaoFactory().getDAO(typeId);
            }       
        }
        return dao;
    }
	

}
