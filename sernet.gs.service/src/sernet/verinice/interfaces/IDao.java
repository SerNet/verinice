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
package sernet.verinice.interfaces;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 * @param <T>
 * @param <ID>
 */
public interface IDao<T, ID extends Serializable> {

    public abstract void saveOrUpdate(T entity);

    public abstract T merge(T entity);

    public abstract void delete(T entity);

    public abstract T findById(ID id);

    public abstract List<T> findAll();

    public abstract List findByQuery(String hqlQuery, Object[] params);

    public abstract List findByCriteria(DetachedCriteria criteria);
    
    public abstract <E> E initializeAndUnproxy(E entity);

}