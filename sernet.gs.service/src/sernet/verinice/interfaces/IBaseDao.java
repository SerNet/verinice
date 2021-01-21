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
package sernet.verinice.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.orm.hibernate3.HibernateCallback;

public interface IBaseDao<T, ID extends Serializable> extends IDao<T, ID> {

    default T merge(T entity, boolean fireUpdates) {
        return merge(entity, fireUpdates, true);
    }

    T merge(T entity, boolean fireUpdates, boolean updateIndex);

    T findByUuid(String uuid, IRetrieveInfo ri);

    T retrieve(ID id, IRetrieveInfo ri);

    /**
     * @param ri
     * @return
     */
    List findAll(IRetrieveInfo ri);

    List findByCallback(HibernateCallback hcb);

    Object executeCallback(HibernateCallback hcb);

    int updateByQuery(String hqlQuery, Object[] values);

    void reload(T element, Serializable id);

    void initialize(Object collection);

    void flush();

    Class<T> getType();

    /**
     * Checks if the user calling the function has write permissions for the
     * element with the given ID and scopeId.
     * 
     * Throws a sernet.gs.service.SecurityException if no write permissions are
     * granted.
     */
    default void checkRights(ID id, ID scopeId) {
        checkRights(Collections.singletonMap(id, scopeId));
    }
    
    /**
     * Checks if the user calling the function has write permissions for the
     * elements with the given IDs and scopeIds.
     * 
     * Throws a sernet.gs.service.SecurityException if no write permissions are
     * granted.
     */ 
    default void checkRights(Map<ID, ID> idToScopeId) {
    }

    /**
     * Checks if the user calling the function has write permissions for the
     * given entity.
     * 
     * Throws a sernet.gs.service.SecurityException if no write permissions are
     * granted.
     */
    default void checkRights(T entity)/* throws SecurityException */ {
        checkRights(Collections.singleton(entity));
    }

    /**
     * Checks if the user calling the function has write permissions for the
     * given entities.
     * 
     * Throws a sernet.gs.service.SecurityException if no write permissions are
     * granted.
     */
    void checkRights(Collection<T> elements);

    /**
     * Checks if the user with the given user name has write permissions for the
     * given entity.
     * 
     * Throws a sernet.gs.service.SecurityException if no write permissions are
     * granted.
     */

    default void checkRights(T entity,
            String username)/* throws SecurityException */ {
        checkRights(Collections.singleton(entity), username);
    }

    /**
     * Checks if the user with the given user name has write permissions for the
     * given entities.
     * 
     * Throws a sernet.gs.service.SecurityException if no write permissions are
     * granted.
     */

    void checkRights(Collection<T> entities,
            String username) /* throws SecurityException */ ;

    void clear();

    boolean contains(T entity);

}
