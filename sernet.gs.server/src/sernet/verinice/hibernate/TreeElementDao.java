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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.CollectionUtil;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.IElementTitleCache;
import sernet.verinice.interfaces.IRetrieveInfo;
import sernet.verinice.interfaces.search.IJsonBuilder;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.TransactionAbortedException;
import sernet.verinice.model.iso27k.InheritLogger;
import sernet.verinice.model.iso27k.ProtectionRequirementUtils;
import sernet.verinice.search.IElementSearchDao;

public class TreeElementDao<T, ID extends Serializable> extends HibernateDao<T, ID>
        implements IBaseDao<T, ID> {

    private static final Logger LOG = Logger.getLogger(TreeElementDao.class);
    private static final InheritLogger LOG_INHERIT = InheritLogger.getLogger(TreeElementDao.class);
    private IElementSearchDao searchDao;
    private IJsonBuilder jsonBuilder;
    private IElementTitleCache titleCache;

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
     * see http://bob.sernet.private:8180/browse/VN-1
     */

    /*
     * SecureTreeElementDao extends TreeElementDao and overrides the merge and
     * delete method. All cnaTreeElement daos must be changed to this type in
     * spring configuration.
     */

    public void saveOrUpdate(T entity) {
        if (LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("saveOrUpdate...");
        }
        super.saveOrUpdate(entity);
        if (entity instanceof CnATreeElement) {
            CnATreeElement elmt = (CnATreeElement) entity;
            index(Set.of(elmt));
            notifyChangedElement(elmt);
        }
    }

    @SuppressWarnings("unchecked")
    public void saveOrUpdateAll(Collection<T> entities) {
        if (LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("saveOrUpdateAll...");
        }
        super.saveOrUpdateAll(entities);
        if (!entities.isEmpty() && entities.iterator().next() instanceof CnATreeElement) {
            index((Collection<CnATreeElement>) entities);
            for (T entity : entities) {
                CnATreeElement elmt = (CnATreeElement) entity;
                notifyChangedElement(elmt);
            }
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
        ri0.configureCriteria(criteria);
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
        ri0.configureCriteria(criteria);
        return loadByCriteria(criteria);
    }

    public T retrieve(ID id, IRetrieveInfo ri) {
        IRetrieveInfo ri0 = null;
        if (LOG.isDebugEnabled()) {
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
        ri0.configureCriteria(criteria);

        return loadByCriteria(criteria);
    }

    private T loadByCriteria(DetachedCriteria criteria) {
        List<T> resultList = findByCriteria(criteria);
        T result = null;
        if (resultList != null) {
            if (resultList.size() > 1) {
                final String message = "More than one entry found, criteria is: "
                        + criteria.toString();
                LOG.error(message);
                throw new RuntimeException(message);
            }
            if (resultList.size() == 1) {
                result = resultList.get(0);
            }
        }
        return result;
    }

    public T merge(T entity) {
        T mergedElement = super.merge(entity);

        if (mergedElement instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) mergedElement;
            index(element);
        }
        return mergedElement;
    }

    public T merge(T entity, boolean fireChange, boolean updateIndex) {
        if (LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("merge...");
        }

        T mergedElement = super.merge(entity);

        if (mergedElement instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) mergedElement;
            if (updateIndex) {
                index(element);
            }
            if (fireChange) {
                notifyChangedElement(element);
            }
        }

        if (fireChange && mergedElement instanceof CnALink) {
            CnALink link = (CnALink) mergedElement;
            notifyChangedElement(link.getDependency());
        }

        return mergedElement;
    }

    protected void index(CnATreeElement element) {
        index(Set.of(element));
    }

    protected void index(Collection<CnATreeElement> elements) {
        for (CnATreeElement element : elements) {
            updateTitleCache(element);

        }
        updateIndex(elements);
    }

    private void updateIndex(Collection<CnATreeElement> elements) {
        try {
            if (getSearchDao() != null) {
                IJsonBuilder builder = getJsonBuilder();
                if (builder != null) {

                    Map<String, String> idToJson = new HashMap<>(elements.size());
                    for (CnATreeElement element : elements) {
                        if (builder.isIndexableElement(element)) {
                            idToJson.put(element.getUuid(), builder.getJson(element));
                        }
                    }
                    getSearchDao().updateOrIndex(idToJson);
                }
            }
        } catch (Exception e) {
            String uuids = (elements != null) ? elements.stream().map(CnATreeElement::getUuid)
                    .collect(Collectors.joining(", ")) : null;
            LOG.error("Error while updating index, elements: " + uuids, e);
        }
    }

    private void updateTitleCache(CnATreeElement element) {
        try {
            if (getTitleCache() != null && element.isScope()) {
                getTitleCache().update(element.getDbId(), element.getTitle());
            }
        } catch (Exception e) {
            String uuid = (element != null) ? element.getUuid() : null;
            LOG.error("Error while updating title cache, element: " + uuid, e);
        }
    }

    protected void indexDelete(CnATreeElement element) {
        if (getSearchDao() != null) {
            getSearchDao().delete(element.getUuid());
        }
    }

    protected void indexDelete(List<CnATreeElement> elements) {
        if (getSearchDao() != null) {
            getSearchDao().delete(
                    elements.stream().map(CnATreeElement::getUuid).collect(Collectors.toList()));
        }
    }

    /**
     * Calls change listener methods on changed element. Causes changes in
     * protection level (schutzbedarf) to be propagated.
     * 
     * @param elmt
     *            the element that was changed.
     */
    protected void notifyChangedElement(CnATreeElement elmt) {
        elmt.valuesChanged();
        if (LOG_INHERIT.isDebug()) {
            LOG_INHERIT.debug("fireChange...");
        }
        if (ProtectionRequirementUtils.isProtectionRequirementsProvider(elmt.getTypeId())) {
            initializeDeductionTree(elmt);
            elmt.fireIntegritaetChanged(new CascadingTransaction());
            elmt.fireVerfuegbarkeitChanged(new CascadingTransaction());
            elmt.fireVertraulichkeitChanged(new CascadingTransaction());
        }
    }

    private void initializeDeductionTree(CnATreeElement elmt) {
        try {
            Set<Integer> initializeIDs = collectAffectedIDs(elmt);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Initialize " + initializeIDs.size() + " elements");
            }
            List<CnATreeElement> result = new ArrayList<>(initializeIDs.size());

            CollectionUtil.partition(List.copyOf(initializeIDs), IDao.QUERY_MAX_ITEMS_IN_LIST)
                    .forEach(partition -> {
                        DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class)
                                .add(Restrictions.in("dbId", initializeIDs));
                        RetrieveInfo.getPropertyInstance().configureCriteria(crit);
                        result.addAll((Collection<? extends CnATreeElement>) findByCriteria(crit));
                    });
        } catch (TransactionAbortedException e) {
            throw new RuntimeCommandException(e);
        }
    }

    private Set<Integer> collectAffectedIDs(CnATreeElement element)
            throws TransactionAbortedException {
        Set<CnATreeElement> initializeElements = new HashSet<>();
        collectAffectedElementsDown(element, initializeElements);
        if (!initializeElements.isEmpty()) {
            collectAffectedElementsUp(Set.copyOf(initializeElements), initializeElements);
        }
        return initializeElements.stream().map(CnATreeElement::getDbId).collect(Collectors.toSet());
    }

    private void collectAffectedElementsDown(CnATreeElement element,
            Set<CnATreeElement> initializeElements) throws TransactionAbortedException {
        for (CnALink ld : element.getLinksDown()) {
            if (ProtectionRequirementUtils.dependencyIsProtectionRequirementsProvider(ld)
                    && initializeElements.add(ld.getDependency())) {
                collectAffectedElementsDown(ld.getDependency(), initializeElements);
            }
        }
    }

    private void collectAffectedElementsUp(Set<CnATreeElement> elements,
            Set<CnATreeElement> initializeElements) throws TransactionAbortedException {

        Set<CnATreeElement> elementsWithoutInitializedLinksUp = elements.stream()
                .filter(el -> !Hibernate.isInitialized(el.getLinksUp()))
                .collect(Collectors.toSet());

        if (!elementsWithoutInitializedLinksUp.isEmpty()) {

            DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class)
                    .setFetchMode("linksUp", FetchMode.JOIN)
                    .add(Restrictions.in("dbId", elementsWithoutInitializedLinksUp.stream()
                            .map(CnATreeElement::getDbId).collect(Collectors.toSet())));
            findByCriteria(crit);
        }
        Set<CnATreeElement> dependants = new HashSet<>();

        for (CnATreeElement element : elements)
            for (CnALink lu : element.getLinksUp()) {
                if (ProtectionRequirementUtils.dependantIsProtectionRequirementsProvider(lu)
                        && initializeElements.add(lu.getDependant())) {
                    dependants.add(lu.getDependant());
                }
            }
        if (!dependants.isEmpty()) {
            collectAffectedElementsUp(dependants, initializeElements);
        }
    }

    public Class<T> getType() {
        return this.type;
    }

    /**
     * Empty by default. Override this in subclasses to check user rights.
     * 
     * @see sernet.verinice.interfaces.IBaseDao#checkRights(java.lang.Object)
     */
    @Override
    public void checkRights(
            Collection<T> entities) /* throws SecurityException */ {
        // empty
    }

    @Override
    public void checkRights(Collection<T> entities, String username) {
        // empty
    }

    public IElementSearchDao getSearchDao() {
        return searchDao;
    }

    public void setSearchDao(IElementSearchDao searchDao) {
        this.searchDao = searchDao;
    }

    public IJsonBuilder getJsonBuilder() {
        return jsonBuilder;
    }

    public void setJsonBuilder(IJsonBuilder jsonBuilder) {
        this.jsonBuilder = jsonBuilder;
    }

    public IElementTitleCache getTitleCache() {
        return titleCache;
    }

    public void setTitleCache(IElementTitleCache titleCache) {
        this.titleCache = titleCache;
    }

}
