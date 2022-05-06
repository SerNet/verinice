/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.TimeFormatter;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

public class LoadElementById<T extends CnATreeElement> extends GenericCommand {

    private static final long serialVersionUID = 770900125484825373L;

    private static final Logger log = Logger.getLogger(LoadElementById.class);

    private Integer id;
    protected T element;
    private String typeId;
    protected RetrieveInfo ri;

    private transient IBaseDao<T, Serializable> dao;

    public LoadElementById(Integer id) {
        this(null, id, null);
    }

    public LoadElementById(Integer id, RetrieveInfo ri) {
        this(null, id, ri);
    }

    public LoadElementById(String typeId, Integer id) {
        this(typeId, id, null);
    }

    public LoadElementById(String typeId, Integer id, RetrieveInfo ri) {
        super();
        this.id = id;
        this.typeId = typeId;
        if (ri != null) {
            this.ri = ri;
        } else {
            this.ri = new RetrieveInfo();
        }
    }

    public void execute() {
        long start = 0;
        if (log.isDebugEnabled()) {
            start = System.currentTimeMillis();
            log.debug("execute() called ..."); //$NON-NLS-1$
        }
        element = getDao().retrieve(this.id, ri);
        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("execute() finished in: " + TimeFormatter.getHumanRedableTime(duration)); //$NON-NLS-1$
        }
    }

    public T getElement() {
        return element;
    }

    /**
     * @return the dao
     */
    public IBaseDao<T, Serializable> getDao() {
        if (dao == null) {
            if (typeId == null) {
                dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(CnATreeElement.class);
            } else {
                dao = getDaoFactory().getDAO(typeId);
            }
        }
        return dao;
    }

}
