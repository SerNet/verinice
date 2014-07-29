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
package sernet.verinice.hibernate;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import sernet.verinice.interfaces.IFinishedRiskAnalysisListsDao;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class FinishedRiskAnalysisListsDao extends HibernateDaoSupport implements IFinishedRiskAnalysisListsDao {

    @Override
    public List<FinishedRiskAnalysisLists> findByFinishedRiskAnalysisId(Integer id) {
        DetachedCriteria criteria = DetachedCriteria.forClass(FinishedRiskAnalysisLists.class);
        criteria.add(Restrictions.eq("finishedRiskAnalysisId", id));
        return getHibernateTemplate().findByCriteria(criteria);
    }
    
    @Override
    public void delete(FinishedRiskAnalysisLists ra) {
        getHibernateTemplate().delete(ra);
    }
    
    @Override
    public void flush() {
        getHibernateTemplate().flush();
    }
}
