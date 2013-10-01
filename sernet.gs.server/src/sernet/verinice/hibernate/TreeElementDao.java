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
package sernet.verinice.hibernate;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IRetrieveInfo;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.InheritLogger;

public class TreeElementDao<T, ID extends Serializable> extends HibernateDao<T, ID> implements IBaseDao<T, ID> {

    private static final Logger LOG = Logger.getLogger(TreeElementDao.class);
    private static final InheritLogger LOG_INHERIT = InheritLogger.getLogger(TreeElementDao.class);
    
    public TreeElementDao(Class<T> type) {
        super(type);
    }

    /*
     * It has to be noted that updates can happen without any call being made to
     * any of these methods. When an object has been loaded into the session,
     * any changes made by a command are persisted by hibernate into the
     * database directly.
     * 
     * Therefore a hibernate interceptor should be used for to check
     * authorization during object's lifecycle events.
     */

    /*
     * For more complex methods, i.e. all that execute a SQL query, the caller
     * (command, webservice or similar, but always on the server side!) may be
     * responsible for checking security. To ensure this contract, a security
     * callback object must be passed to every such method, the caller must
     * implement the method hasWritePersmission():
     * 
     * interface SecurityCallback { public void hasWritePermission() throws
     * AuthorizationException; }
     */

    /*
     * It is much more easier to implement the check in a class which extends
     * TreeElementDao. Since this is the only possible entry point for updates
     * its an appropriate way to go.
     * 
     * see http://zimbra:81/cgi-bin/bugzilla/show_bug.cgi?id=5
     */

    /*
     * SecureTreeElementDao extends TreeElementDao and overrides the merge and
     * delete method. All cnaTreeElement daos must be changed to this type in
     * spring configuration.
     */

    public void saveOrUpdate(T entity) { 
        if(LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("saveOrUpdate...");
        }
        super.saveOrUpdate(entity);
        if (entity instanceof CnATreeElement) {
            CnATreeElement elmt = (CnATreeElement) entity;
            fireChange(elmt);
        }
    }

    public List findAll(IRetrieveInfo ri) {
        // this could be used to limit result size:
        // DetachedCriteria criteria = DetachedCriteria.forClass(type);
        // List results = getHibernateTemplate().findByCriteria(criteria, 0,
        // 1000);
        // return results;
        IRetrieveInfo ri0 = (ri == null) ? new RetrieveInfo() : ri;
        DetachedCriteria criteria = DetachedCriteria.forClass(type);
        configureCriteria(criteria, ri0);
        return findByCriteria(criteria);
    }

    public T findById(ID id) {
        // NEVER use load() because it does not use the filter used to restrict
        // read access!
        // return (T) getHibernateTemplate().load(type, id);
        return retrieve(id, (new RetrieveInfo()).setProperties(true));
    }
    
    public T findByUuid(String uuid, IRetrieveInfo ri) {
        IRetrieveInfo ri0 = (ri == null) ? new RetrieveInfo() : ri;
        DetachedCriteria criteria = DetachedCriteria.forClass(type);
        criteria.add(Restrictions.eq("uuid", uuid));
        configureCriteria(criteria, ri0);
        return loadByCriteria(criteria);
    }

    public T retrieve(ID id, IRetrieveInfo ri) {
        IRetrieveInfo ri0 = null;
        if(LOG.isDebugEnabled()) {
            LOG.debug("retrieve - id: " + id + " " + ri);
        }
        if (ri == null) {
            ri0 = new RetrieveInfo();
        } else {
            ri0 = ri;
        }

        DetachedCriteria criteria = DetachedCriteria.forClass(type);
        if (CnALink.class.isAssignableFrom(type)) {
            criteria.add(Restrictions.eq("id", id));
        } else {
            criteria.add(Restrictions.eq("dbId", id));
        }
        configureCriteria(criteria, ri0);

        return loadByCriteria(criteria);
    }

    private void configureCriteria(DetachedCriteria criteria, IRetrieveInfo ri) {
        if (ri.isProperties()) {
            criteria.setFetchMode("entity", FetchMode.JOIN);
            criteria.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
            criteria.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        }
        if (ri.isPermissions()) {
            criteria.setFetchMode("permissions", FetchMode.JOIN);
        }

        if (ri.isLinksDown()) {
            criteria.setFetchMode("linksDown", FetchMode.JOIN);
            criteria.setFetchMode("linksDown.dependency", FetchMode.JOIN);
            if (ri.isLinksDownProperties()) {
                criteria.setFetchMode("linksDown.dependency.entity", FetchMode.JOIN);
                criteria.setFetchMode("linksDown.dependency.entity.typedPropertyLists", FetchMode.JOIN);
                criteria.setFetchMode("linksDown.dependency.entity.typedPropertyLists.properties", FetchMode.JOIN);
            }
        }
        if (ri.isLinksUp()) {
            criteria.setFetchMode("linksUp", FetchMode.JOIN);
            criteria.setFetchMode("linksUp.dependant", FetchMode.JOIN);
            if (ri.isLinksUpProperties()) {
                criteria.setFetchMode("linksUp.dependant.entity", FetchMode.JOIN);
                criteria.setFetchMode("linksUp.dependant.entity.typedPropertyLists", FetchMode.JOIN);
                criteria.setFetchMode("linksUp.dependant.entity.typedPropertyLists.properties", FetchMode.JOIN);
            }
        }
        if (ri.isParent()) {
            criteria.setFetchMode("parent", FetchMode.JOIN);
            if (ri.isSiblings()) {
                criteria.setFetchMode("parent.children", FetchMode.JOIN);
            }
            if (ri.isParentPermissions()) {
                criteria.setFetchMode("parent.permissions", FetchMode.JOIN);
            }
        }
        if (ri.isChildren()) {
            criteria.setFetchMode("children", FetchMode.JOIN);
            DetachedCriteria criteriaChildren = null, criteriaEntity = null;
            if (ri.isInnerJoin()) {
                criteriaChildren = criteria.createCriteria("children");
            }
            if (ri.isChildrenProperties()) {
                criteria.setFetchMode("children.entity", FetchMode.JOIN);
                if (ri.isInnerJoin()) {
                    criteriaEntity = criteriaChildren.createCriteria("entity");
                }
                criteria.setFetchMode("children.entity.typedPropertyLists", FetchMode.JOIN);
                if (ri.isInnerJoin()) {
                    criteriaEntity.createCriteria("typedPropertyLists");
                }
                criteria.setFetchMode("children.entity.typedPropertyLists.properties", FetchMode.JOIN);
            }
            if (ri.isChildrenPermissions()) {
                criteria.setFetchMode("children.permissions", FetchMode.JOIN);
                if (ri.isInnerJoin()) {
                    criteriaChildren.createCriteria("permissions");
                }
            }
        }
        if (ri.isGrandchildren()) {
            criteria.setFetchMode("children.children", FetchMode.JOIN);
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }

    private T loadByCriteria(DetachedCriteria criteria) {
        List<T> resultList = findByCriteria(criteria);
        T result = null;
        if (resultList != null) {
            if (resultList.size() > 1) {
                final String message = "More than one entry found, criteria is: " + criteria.toString();
                LOG.error(message);
                throw new RuntimeException(message);
            }
            if (resultList.size() == 1) {
                result = resultList.get(0);
            }
        }
        return result;
    }

    public T merge(T entity, boolean fireChange) {
        if(LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("merge...");
        }
        
        T mergedElement = super.merge(entity);

        if (fireChange && mergedElement instanceof CnATreeElement) {
            CnATreeElement elmt = (CnATreeElement) mergedElement;
            fireChange(elmt);
        }

        if (fireChange && mergedElement instanceof CnALink) {
            CnALink link = (CnALink) mergedElement;
            fireChange(link.getDependency());
        }

        return mergedElement;
    }

    /**
     * Causes changes in protection level (schutzbedarf) to be propagated.
     * 
     * @param elmt
     *            the element that had its protection level or protection level
     *            description changed.
     */
    protected void fireChange(CnATreeElement elmt) {  
        if(LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("fireChange...");
        }
        elmt.fireIntegritaetChanged(new CascadingTransaction());
        elmt.fireVerfuegbarkeitChanged(new CascadingTransaction());
        elmt.fireVertraulichkeitChanged(new CascadingTransaction());   
    }

    public Class<T> getType() {
        return this.type;
    }

    /**
     * Empty by default.
     * Override this in subclasses to check user rights.
     * 
     * @see sernet.verinice.interfaces.IBaseDao#checkRights(java.lang.Object)
     */
    @Override
    public void checkRights(T entity) /*throws SecurityException*/ {
        // empty    
    }

    @Override
    public void checkRights(T entity, String username) {
        // empty
    }

}
