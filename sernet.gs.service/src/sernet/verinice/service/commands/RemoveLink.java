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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class RemoveLink<T extends CnALink> extends GenericCommand implements IChangeLoggingCommand {

private transient Logger log = Logger.getLogger(RemoveLink.class);
    
    public Logger getLog() {
        if(log==null) {
            log = Logger.getLogger(RemoveLink.class);
        }
        return log;
    }
    
    private String stationId;
    private CnALink link;
    private Integer dependantId;
    private Integer dependencyId;
    private String typeId; 

    public RemoveLink(CnALink link) {
        this.stationId = ChangeLogEntry.STATION_ID;
        this.link = link;
    }

    public RemoveLink(Integer dependantId, Integer dependencyId, String typeId) {
        super();
        this.dependantId = dependantId;
        this.dependencyId = dependencyId;
        this.typeId = typeId;
    }

    public void execute() {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Looking for link to remove.");
        }
        
        IBaseDao<CnALink, Serializable> dao = getDaoFactory().getDAO(CnALink.class);
        if(link!=null) {
            link = dao.findById(link.getId());
        } else {
            link = dao.findById(new CnALink.Id(dependantId, dependencyId, typeId));
        }
        if (link != null) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Found link, removing " + link.getId());
            }
            // remember elements for firing update events:
            CnATreeElement dependant = link.getDependant();
            CnATreeElement dependency = link.getDependency();
            link.remove();
            dao.delete(link);
            dao.flush();
            
            // fire updates for both sides of the link, which may now be separate trees:
            getDaoFactory().getDAO(dependant.getTypeId()).merge(dependant, true);
            getDaoFactory().getDAO(dependency.getTypeId()).merge(dependency, true);
            
        } else {
            getLog().warn("Link was already deleted while trying to delete it.");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
     */
    @Override
    public void clear() {
        link = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType
     * ()
     */
    public int getChangeType() {
        return ChangeLogEntry.TYPE_DELETE;
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#
     * getChangedElements()
     */
    public List<CnATreeElement> getChangedElements() {
        // return link category item:
        List<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
        if (link != null) {
            result.add(link.getDependant());
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId
     * ()
     */
    public String getStationId() {
        return stationId;
    }

}
