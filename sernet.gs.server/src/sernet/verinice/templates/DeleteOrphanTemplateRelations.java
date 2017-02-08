/*******************************************************************************  
 * Copyright (c) 2017 Viktor Schmidt.  
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.verinice.templates;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class DeleteOrphanTemplateRelations {

    private static final Logger LOG = Logger.getLogger(DeleteOrphanTemplateRelations.class);

    private IBaseDao<CnATreeElement, Integer> elementDao;

    public void init() {
        Integer numberOfDeletedRelations = (Integer) getElementDao().executeCallback(new CleanTemplateRelations());
         if (LOG.isInfoEnabled() && numberOfDeletedRelations.intValue() > 0) {
            LOG.info("Found orpahn template relations.");
            LOG.info(numberOfDeletedRelations + " orphan template relations deleted");
         }
    }

    private class CleanTemplateRelations implements HibernateCallback {

        private String query = "DELETE FROM templaterelations rel WHERE NOT EXISTS (" //$NON-NLS-1$
                + "SELECT ce.uuid FROM cnatreeelement ce "//$NON-NLS-1$
                + "WHERE ce.template_type like 'TEMPLATE' AND ce.uuid = rel.template_uuid)";

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.springframework.orm.hibernate3.HibernateCallback#doInHibernate(
         * org.hibernate.Session)
         */
        @Override
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            return session.createSQLQuery(query).executeUpdate();
        }

    }

    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }
}
