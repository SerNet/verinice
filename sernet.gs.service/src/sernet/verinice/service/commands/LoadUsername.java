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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class LoadUsername extends GenericCommand {

    private static final Set<String> PERSON_TYPE_IDS;

    static {
        Set<String> personTypeIDs = new HashSet<>(3);
        personTypeIDs.add(Person.TYPE_ID);
        personTypeIDs.add(PersonIso.TYPE_ID);
        personTypeIDs.add(BpPerson.TYPE_ID);
        PERSON_TYPE_IDS = Collections.unmodifiableSet(personTypeIDs);
    }

    public static final String HQL_FOR_LINK_DOWN_FROM_PERSON = createHQLQuery(true);

    public static final String HQL_FOR_LINK_UP_FROM_PERSON = createHQLQuery(false);

    private static final Logger logger = Logger.getLogger(LoadUsername.class);

    private final String uuid;

    private String username;

    private final String linkId;

    public LoadUsername(String uuidControl, String linkId) {
        super();
        this.uuid = uuidControl;
        this.linkId = linkId;
    }

    /*
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            HuiRelation relation = HUITypeFactory.getInstance().getRelation(linkId);
            String hql;

            if (PERSON_TYPE_IDS.contains(relation.getFrom())) {
                hql = HQL_FOR_LINK_DOWN_FROM_PERSON;
            } else if (PERSON_TYPE_IDS.contains(relation.getTo())) {
                hql = HQL_FOR_LINK_UP_FROM_PERSON;
            } else {
                throw new IllegalStateException("Unable to find assignee for task/control " + uuid
                        + ", unsupported relation type " + linkId);
            }

            IBaseDao<Configuration, Serializable> dao = getDaoFactory().getDAO(Configuration.class);
            List<?> result = dao.findByQuery(hql,
                    new String[] { uuid, linkId, Configuration.PROP_USERNAME });

            if (result != null && !result.isEmpty()) {
                username = (String) result.get(0);
            }
        } catch (Exception t) {
            logger.error("Error while loading username for control uuid: " + uuid, t);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private static final String createHQLQuery(boolean forLinkDownFromPerson) {

        return "select prop.propertyValue from Configuration as conf "
                + "join conf.entity as entity join entity.typedPropertyLists as propertyList "
                + "join propertyList.properties as prop "
                + (forLinkDownFromPerson
                        ? "join conf.person as person join person.linksDown as link join link.dependency as targetObject "
                        : "join conf.person as person join person.linksUp as link join link.dependant as targetObject ")
                + "where targetObject.uuid = ? and link.id.typeId = ? and prop.propertyType = ?";

    }
}
