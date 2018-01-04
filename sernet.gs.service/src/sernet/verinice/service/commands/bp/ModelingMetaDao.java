/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 * Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * ModelingMetaDao contains methods for reading data in the database. The
 * methods in this class are only intended for use when modelling the new IT
 * base protection (ITBP).
 * 
 * This class is no ordinary verinice DAO. But it uses other DAOs and is
 * therefore called MetaDao. This class wraps accesses to the ordinary DAOs.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelingMetaDao {

    private static final String TYPE_ID = "typeId";
    private static final String UUIDS = "uuids";

    private static final String HQL_LOAD_ELEMENTS_WITH_PROPERTIES = "select element from CnATreeElement element " +
            "join fetch element.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where element.uuid in (:uuids)"; //$NON-NLS-1$

    public static final String HQL_LOAD_ELEMENTS_OF_SCOPE = "select  safeguard from CnATreeElement safeguard " +
            "join fetch safeguard.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where safeguard.objectType = :typeId " +
            "and safeguard.scopeId = :scopeId"; //$NON-NLS-1$

    private static final String HQL_LOAD_ELEMENTS_WITH_PARENT = "select distinct element from CnATreeElement element " +
            "join fetch element.parent as p1 " +
            "where element.uuid in (:uuids)"; //$NON-NLS-1$
    
    private static final String HQL_LOAD_ELEMENTS_WITH_3_PARENTS = "select distinct element from CnATreeElement element " +
            "join fetch element.parent as p1 " +
            "join fetch p1.parent as p2 " +
            "join fetch p2.parent as p3 " +
            "where element.uuid in (:uuids)"; //$NON-NLS-1$

    private static final String HQL_LOAD_LINKED_SAFEGUARDS_OF_MODULES = "select safeguard from CnATreeElement safeguard " +
            "join safeguard.linksUp as linksUp " +
            "join linksUp.dependant as requirement " +
            "join requirement.parent as module " +
            "join fetch safeguard.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where safeguard.objectType = :typeId " +
            "and module.uuid in (:uuids)"; //$NON-NLS-1$
    
    private static final String HQL_LOAD_CHILDREN_WITH_PROPERTIES = "select requirement from CnATreeElement requirement " +
            "join requirement.parent as module " +
            "join fetch requirement.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where requirement.objectType = :typeId " + 
            "and module.uuid in (:uuids)"; //$NON-NLS-1$
    
    private static final String HQL_LOAD_LINKED_ELEMENTS_WITH_PROPERTIES = "select element from CnATreeElement element "
            + "join element.linksUp as linksUp " + "join linksUp.dependant as requirement "
            + "join fetch element.entity as entity "
            + "join fetch entity.typedPropertyLists as propertyList "
            + "join fetch propertyList.properties as props "
            + "where element.objectType in (:typeIds) " 
            + "and requirement.uuid = :uuid"; //$NON-NLS-2$
    
    private IBaseDao<CnATreeElement, Serializable> dao;

    public ModelingMetaDao(IBaseDao<CnATreeElement, Serializable> dao) {
        super();
        this.dao = dao;
    }

    @SuppressWarnings("unchecked")
    public List<CnATreeElement> loadElementsFromScope(final String typeId, final Integer scopeId) {
        return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelingMetaDao.HQL_LOAD_ELEMENTS_OF_SCOPE)
                        .setParameter("scopeId", scopeId).setParameter(TYPE_ID, typeId);
                query.setReadOnly(true);
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public List<CnATreeElement> loadElementsWithProperties(final Collection<String> allUuids) {
        return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(HQL_LOAD_ELEMENTS_WITH_PROPERTIES)
                        .setParameterList(UUIDS, allUuids);
                query.setReadOnly(true);
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public List<CnATreeElement> loadElementsWithParent(final Collection<String> uuids) {
        return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelingMetaDao.HQL_LOAD_ELEMENTS_WITH_PARENT)
                        .setParameterList(UUIDS, uuids);
                query.setReadOnly(true);
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public List<CnATreeElement> loadElementsWith3Parents(final Collection<String> uuids) {
        return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelingMetaDao.HQL_LOAD_ELEMENTS_WITH_3_PARENTS)
                        .setParameterList(UUIDS, uuids);
                query.setReadOnly(true);
                return query.list();
            }
        });
    }

    public Set<CnATreeElement> loadChildrenOfElement(String uuid) {
        RetrieveInfo ri = RetrieveInfo.getChildrenInstance();
        ri.setChildrenProperties(true);
        CnATreeElement element = getDao().findByUuid(uuid, ri);
        if (element == null) {
            return Collections.emptySet();
        }
        return element.getChildren();
    }

    @SuppressWarnings("unchecked")
    public Set<CnATreeElement> loadChildrenWithProperties(final Set<String> parentUuids,
            final String typeId) {
        final List<CnATreeElement> resultList = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(HQL_LOAD_CHILDREN_WITH_PROPERTIES)
                        .setParameterList(UUIDS, parentUuids).setParameter(TYPE_ID, typeId);
                query.setReadOnly(true);
                return query.list();
            }
        });
        return new HashSet<>(resultList);
    }

    @SuppressWarnings("unchecked")
    public List<CnATreeElement> loadLinkedElementsOfParents(final Set<String> parentUuids,
            final String typeId) {
        return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session
                        .createQuery(ModelingMetaDao.HQL_LOAD_LINKED_SAFEGUARDS_OF_MODULES)
                        .setParameterList(UUIDS, parentUuids).setParameter(TYPE_ID, typeId);
                return query.list();
            }
        });
    }

    /**
     * Loads the linked elements of an element with the given uuid. The type IDs
     * of the linked elements are passed as parameter typeIds.
     * 
     * @param uuid
     *            The UUID of an element
     * @param typeIds
     *            An array of type ids.
     * @return A list with linked elements and their properties
     */
    @SuppressWarnings("unchecked")
    public List<CnATreeElement> loadLinkedElementsWithProperties(final String uuid,
            final String[] typeIds) {
        return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(HQL_LOAD_LINKED_ELEMENTS_WITH_PROPERTIES)
                        .setParameter("uuid", uuid).setParameterList("typeIds", typeIds);
                query.setReadOnly(true);
                return query.list();
            }
        });
    }

    public CnATreeElement loadElementWithPropertiesAndChildren(String uuid) {
        return getDao().findByUuid(uuid,
                RetrieveInfo.getChildrenInstance().setChildrenProperties(true));
    }

    public CnATreeElement loadElementWithPropertiesAndChildren(Integer dbid) {
        return getDao().retrieve(dbid,
                RetrieveInfo.getChildrenInstance().setChildrenProperties(true));
    }

    public CnATreeElement loadElementWithChildren(String uuid) {
        return getDao().findByUuid(uuid, RetrieveInfo.getChildrenInstance());
    }

    public CnATreeElement loadElementWithChildren(Integer dbid) {
        return getDao().retrieve(dbid, RetrieveInfo.getChildrenInstance());
    }

    public CnATreeElement loadElementWithProperties(Integer dbid) {
        return getDao().retrieve(dbid, RetrieveInfo.getPropertyInstance());
    }

    public IBaseDao<CnATreeElement, Serializable> getDao() {
        return dao;
    }

    public void setDao(IBaseDao<CnATreeElement, Serializable> dao) {
        this.dao = dao;
    }
}
