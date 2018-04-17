/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.fei.service;

import java.io.Serializable;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class PathFinderCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(PathFinderCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(PathFinderCommand.class);
        }
        return log;
    }
    
    private static final String PARENT = "..";
    
    private String path;    
    private String uuidStart;
    
    private CnATreeElement target = null;
    
    private transient IBaseDao<CnATreeElement, Serializable> dao;

   
    public PathFinderCommand(String uuid, String path) {
        this.path = path;
        this.uuidStart = uuid;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(path==null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(path, "/");
        if (!st.hasMoreElements()) {
            return;
        }
        traverse(uuidStart, st);       
    }

    private void traverse(String uuid, StringTokenizer st) {
        if(!st.hasMoreTokens()) {
            loadTarget(uuid);
            return;
        }
        String next = st.nextToken();
        if (getLog().isDebugEnabled()) {
            getLog().debug("Next token: " + next);
        }
        RetrieveInfo ri = new RetrieveInfo();
        if(next.equals(PARENT)) {
            ri.setParent(true);
        } else {
            ri.setChildren(true);
            ri.setChildrenProperties(true);
        }
        CnATreeElement startGroup = getDao().findByUuid(uuid, ri);
        if(next.equals(PARENT)) {
            traverse(startGroup.getParent().getUuid(),st);
        } else {
            CnATreeElement child = findChild(startGroup,next);
            if(child!=null) {
                traverse(child.getUuid(),st);
            }
        }      
    }
    
    private void loadTarget(String uuid) {
        target = getDao().findByUuid(uuid, RetrieveInfo.getPropertyInstance());       
    }

    private CnATreeElement findChild(CnATreeElement element, String name) {
        for (CnATreeElement child : element.getChildren()) {
           if(child.getTitle().startsWith(name)) {
               if (getLog().isDebugEnabled()) {
                   getLog().debug("Child found: " + child.getTitle());
               }
               return child;
           }
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Child not found, token " + name);
        }
        return null;
    }

    public CnATreeElement getTarget() {
        return target;
    }
    
    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

}
