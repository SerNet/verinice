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
package sernet.verinice.bpm;

import java.util.List;
import java.util.Set;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.samt.SamtTopic;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ProcessDao extends HibernateDaoSupport {
    
    public static final String HQL = "select props.propertyValue " +
    "from Configuration as conf " +
    "join conf.person as person " +
    "join conf.entity as entity " +
    "join entity.typedPropertyLists as propertyList " +
    "join propertyList.properties as props " +
    "where person.uuid = ? " +
    "and props.propertyType = ?";
    
    public String getAssignee(Control control) {
        String uuidAssignee = null;
        Set<CnALink> linkSet = control.getLinksDown();
        for (CnALink link : linkSet) {
            if(Control.REL_CONTROL_PERSON_ISO.equals(link.getRelationId())) {
                uuidAssignee = link.getDependency().getUuid();            
                break;
            }
        } 
        return loadUsername(uuidAssignee);
    }
    
    public String getAssignee(SamtTopic topic) {
        String uuidAssignee = null;     
        Set<CnALink> linkSet = topic.getLinksDown();
        for (CnALink link : linkSet) {
            if(SamtTopic.REL_SAMTTOPIC_PERSON_ISO.equals(link.getRelationId())) {
                uuidAssignee = link.getDependency().getUuid();            
                break;
            }
        }
        return loadUsername(uuidAssignee);
    }

    public String loadUsername(String uuidAssignee) {
        String username = null;
        if(uuidAssignee!=null) {
            List<String> result = getHibernateTemplate().find(ProcessDao.HQL, new String[] {uuidAssignee,Configuration.PROP_USERNAME});
            if(result!=null && !result.isEmpty()) {
                username = result.get(0);
            }
        }
        return username;
    }
}
