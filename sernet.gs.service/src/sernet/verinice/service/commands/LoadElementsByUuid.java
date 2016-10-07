/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.*;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.TimeFormatter;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Load multiple elements by a list of uuids
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class LoadElementsByUuid<T extends CnATreeElement> extends GenericCommand {

    private static final long serialVersionUID = 1L;
    private transient Logger log = Logger.getLogger(LoadElementsByUuid.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadElementsByUuid.class);
        }
        return log;
    }
    
    private HashSet<String> uuids;
    protected HashSet<T> elements;
    private String typeId;
    protected RetrieveInfo ri;

    private transient IBaseDao<T, Serializable> dao;
    
    
    public LoadElementsByUuid(List<String> uuids) {
        this(null, uuids, null);
    }
	
    public LoadElementsByUuid(List<String> uuids, RetrieveInfo ri) {
        this(null, uuids, ri);
    }

    public LoadElementsByUuid(String typeId, List<String> uuids) {
        this(typeId, uuids, null);
    }

    public LoadElementsByUuid(String typeId, List<String> uuids, RetrieveInfo ri) {
		super();
        this.uuids = new HashSet<>(uuids);
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
        elements = new HashSet<>(uuids.size());
        for (String uuid : uuids) {
            T element = getDao().findByUuid(uuid, ri);
            if (element != null) {

                elements.add(element);
            } else {
                getLog().warn("element " + uuid + " not found!");
            }
        }

        if (getLog().isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            getLog().debug("execute() finished in: " + TimeFormatter.getHumanRedableTime(duration)); //$NON-NLS-1$
        }		
    }

    public Set<T> getElements() {
        return elements;
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
