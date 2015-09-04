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
package sernet.verinice.hibernate;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import sernet.verinice.interfaces.IDao;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class HibernateDao<T, ID extends Serializable> extends HibernateDaoSupport implements IDao<T, ID>  {

    protected Class<T> type;

    private static final Logger LOG = Logger.getLogger(HibernateDao.class);

    public HibernateDao(Class<T> type) {
        this.type = type;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDao#delete(java.lang.Object)
     */
    @Override
    public void delete(T entity) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting element: " + entity);
        }
        // TODO akoderman update protection requirements on delete (see how it's
        // done during merge())
        getHibernateTemplate().delete(entity);  
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDao#findAll()
     */
    @Override
    public List<T> findAll() {
        // this could be used to limit result size:
        // DetachedCriteria criteria = DetachedCriteria.forClass(type);
        // List results = getHibernateTemplate().findByCriteria(criteria, 0,
        // 1000);
        DetachedCriteria criteria = DetachedCriteria.forClass(type);
        return findByCriteria(criteria);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDao#findByCriteria(org.hibernate.criterion.DetachedCriteria)
     */
    @Override
    public List<T> findByCriteria(DetachedCriteria criteria) {
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDao#findById(java.io.Serializable)
     */
    @Override
    public T findById(ID id) {
        return (T) getHibernateTemplate().load(type, id);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDao#findByQuery(java.lang.String, java.lang.Object[])
     */
    @Override
    public List<T> findByQuery(String hqlQuery, Object[] params) {
        return getHibernateTemplate().find(hqlQuery, params);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDao#findByQuery(java.lang.String, java.lang.Object[])
     */
    @Override
    public List<T> findByQuery(String hqlQuery, String[] paramNames, Object[] paramValues) {
        return getHibernateTemplate().findByNamedParam(hqlQuery, paramNames, paramValues);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDao#merge(java.lang.Object)
     */
    @Override
    public T merge(T entity) {
        return (T) getHibernateTemplate().merge(entity);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IDao#saveOrUpdate(java.lang.Object)
     */
    @Override
    public void saveOrUpdate(T entity) {
        getHibernateTemplate().saveOrUpdate(entity);
    }
    
    @Override
    public <E> E initializeAndUnproxy(E entity) {
        if (entity == null) {
            throw new NullPointerException("Entity passed for initialization is null");
        }

        Hibernate.initialize(entity);
        if (entity instanceof HibernateProxy) {
            entity = (E) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }
    
    public List findByCallback(HibernateCallback hcb) {
        return getHibernateTemplate().executeFind(hcb);
    }

    public Object executeCallback(HibernateCallback hcb) {
        return getHibernateTemplate().execute(hcb);
    }

    public int updateByQuery(String hqlQuery, Object[] values) {
        return getHibernateTemplate().bulkUpdate(hqlQuery, values);
    }

    public void initialize(Object proxy) {
        getHibernateTemplate().initialize(proxy);
    }

    public void flush() {
        getHibernateTemplate().flush();
    }
    
    public void clear() {
        getHibernateTemplate().clear();
    }
    
    public void reload(T element, Serializable id) {
        // FIXME ak we need to get rid of load() becuase it does not check read
        // access!
        getHibernateTemplate().load(element, id);
    }
    
    public boolean contains(T element) {
        return getHibernateTemplate().contains(element);
    }

}
