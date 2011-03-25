/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.SecurityException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Control;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class LoadUsername extends GenericCommand {

    private transient Logger log = Logger.getLogger(LoadUsername.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadUsername.class);
        }
        return log;
    }
    
    public final static String HQL = "select props.propertyValue " +
    "from Configuration as conf " +
    "join conf.person as person " +
    "join conf.entity as entity " +
    "join entity.typedPropertyLists as propertyList " +
    "join propertyList.properties as props " +
    "where person.uuid = ? " +
    "and props.propertyType = ?";
    
    String uuid;
    
    String username; 
    
    String linkId;
    
    public LoadUsername(String uuidControl, String linkId) {
        super();
        this.uuid = uuidControl;
        this.linkId = linkId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            String uuidAssignee = null;
            RetrieveInfo ri = new RetrieveInfo();
            ri.setLinksUp(true);
            ri.setPermissions(true);
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid(uuid,ri);
            command = getCommandService().executeCommand(command);
            CnATreeElement control = command.getElement();
            if(control!=null) {
                Set<CnALink> linkSet = control.getLinksDown();
                for (CnALink link : linkSet) {
                    if(this.linkId.equals(link.getRelationId())) {
                        uuidAssignee = link.getDependency().getUuid();            
                        break;
                    }
                }      
                if(uuidAssignee!=null) {
                    IBaseDao<Configuration, Serializable> dao = (IBaseDao<Configuration, Serializable>) getDaoFactory().getDAO(Configuration.class);
                    List<String> result = dao.findByQuery(HQL, new String[] {uuidAssignee,Configuration.PROP_USERNAME});
                    if(result!=null && !result.isEmpty()) {
                        username = result.get(0);                      
                    }
                }
            }
        } catch (Throwable t) {
            getLog().error("Error while loading username for control uuid: " + uuid, t);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
