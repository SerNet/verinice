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
package sernet.gs.server;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.scheduling.quartz.QuartzJobBean;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A {@link DeleteOrphanTemplateRelationsJob} instance is a job that is to run
 * once every three months.
 * <p>
 * To adjust the iteration of this job, please modify the cron expression in the
 * Spring configuration.
 * </p>
 * <p>
 * An instance of this class is created in the Spring configuration.
 * </p>
 * <p>
 * The class runs a Hibernate callback that deletes all orphan modeling template
 * relations. See hibernate mapping for CnATreeElement.implementedTemplateUuids
 * in {@link CnATreeElement.hbm.xml}).
 * </p>
 * 
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class DeleteOrphanTemplateRelationsJob extends QuartzJobBean implements StatefulJob {

    private static final Logger LOG = Logger.getLogger(DeleteOrphanTemplateRelationsJob.class);

    private IBaseDao<CnATreeElement, Integer> elementDao;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.
     * quartz.JobExecutionContext)
     */
    @Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
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
