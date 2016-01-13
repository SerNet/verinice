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
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.common.ChangeLogEntry;

/**
 * Checks whether the ownGefaehrdung is already used - means if there is a
 * GefaehrdungsUmsetzung made of the OwnGefaehrdung
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class CheckOwnGefaehrdungInUseCommand extends GenericCommand {

    private static final long serialVersionUID = 6512515989362442858L;
    private OwnGefaehrdung ownGefaehrdung;
    private boolean isInUse = true;
    private transient Logger log = Logger.getLogger(CheckOwnGefaehrdungInUseCommand.class);

    public CheckOwnGefaehrdungInUseCommand(OwnGefaehrdung ownGefaehrdungToCheck) {
        super();
        ownGefaehrdung = ownGefaehrdungToCheck;
    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CheckOwnGefaehrdungInUseCommand.class);
        }
        return log;
    }

    @Override
    public void execute() {

        IBaseDao<ChangeLogEntry, Serializable> dao = getDaoFactory().getDAO(ChangeLogEntry.class);
        List<?> entries = (List<?>) dao
                .findByCallback(new Callback(ownGefaehrdung.getId()));
        isInUse = !entries.isEmpty();
        if (getLog().isInfoEnabled()) {
            if (isInUse) {
                getLog().info(ownGefaehrdung.getId() + " is in use");
            } else {
                getLog().info(ownGefaehrdung.getId() + " is unused");
            }
        }
    }

    public boolean isInUse() {
        return isInUse;
    }

    private static class Callback implements HibernateCallback, Serializable {

        private static final long serialVersionUID = -3853601020062695555L;

        private String id;

        Callback(String id) {
            this.id = id;
        }

        public Object doInHibernate(Session session) throws HibernateException, SQLException {

            Query query = session.createSQLQuery(
                    "SELECT cnatreeelement.uuid,"
                            + " properties.propertytype,"
                            + " properties.propertyvalue "
                            + " FROM cnatreeelement"
                            + " JOIN entity ON cnatreeelement.entity_id=entity.dbid"
                            + " JOIN propertylist ON propertylist.typedlist_id=entity.dbid"
                            + " JOIN properties ON properties.properties_id=propertylist.dbid"
                            + " WHERE "
                            + " properties.propertytype='gefaehrdungsumsetzung_id' AND properties.propertyvalue='"
                            + id + "'");
            return query.list();
        }

    }
}
