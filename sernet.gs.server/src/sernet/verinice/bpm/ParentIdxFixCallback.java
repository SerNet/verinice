/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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

import java.io.Serializable;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Sets the column JBPM4_EXECUTION.PARENT_IDX_ to '0' when it is NULL.
 * This is a workaround for an JBPM bug which sets this column to null
 * sometimes.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ParentIdxFixCallback implements HibernateCallback, Serializable {

    private static final String SQL = "UPDATE JBPM4_EXECUTION SET PARENT_IDX_=0 WHERE PARENT_IDX_ IS NULL";
    
    private static ParentIdxFixCallback instance;
    
    /* (non-Javadoc)
     * @see org.springframework.orm.hibernate3.HibernateCallback#doInHibernate(org.hibernate.Session)
     */
    @Override
    public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.createSQLQuery(SQL);
        int result = query.executeUpdate();
        return result;
    }

    public static HibernateCallback getInstance() {
        if(instance==null) {
            instance = new ParentIdxFixCallback();
        }
        return instance;
    }

}
