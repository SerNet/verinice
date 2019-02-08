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
import java.util.List;

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

    private static final String UUIDS = "uuids";

    private static final String JOIN_PROPERTIES = "join fetch entity.typedPropertyLists as propertyList "
            + "join fetch propertyList.properties as props ";

    private static final String HQL_LOAD_ELEMENTS_WITH_PROPERTIES = "select element from CnATreeElement element "
            + "join fetch element.entity as entity " + JOIN_PROPERTIES
            + "where element.uuid in (:uuids)"; //$NON-NLS-1$

    private IBaseDao<CnATreeElement, Serializable> dao;

    public ModelingMetaDao(IBaseDao<CnATreeElement, Serializable> dao) {
        super();
        this.dao = dao;
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

    public CnATreeElement loadElementWithProperties(Integer dbid) {
        return getDao().retrieve(dbid, RetrieveInfo.getPropertyInstance());
    }

    public IBaseDao<CnATreeElement, Serializable> getDao() {
        return dao;
    }

}
