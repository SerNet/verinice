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
import java.util.List;

import org.springframework.orm.hibernate3.HibernateCallback;

public interface IBaseDao<T, ID extends Serializable> extends IDao<T, ID> {

	public T merge(T entity, boolean fireUpdates);

	public T findByUuid(String uuid, IRetrieveInfo ri);

	public T retrieve(ID id, IRetrieveInfo ri);

	/**
	 * @param ri
	 * @return
	 */
	public List findAll(IRetrieveInfo ri);

	public List findByCallback(HibernateCallback hcb);

	public Object executeCallback(HibernateCallback hcb);

	public int updateByQuery(String hqlQuery, Object[] values);

	public void reload(T element, Serializable id);

	public void initialize(Object collection);

	public void flush();

	public Class<T> getType();
	
	public void checkRights(T entity) /*throws SecurityException*/ ;
	
	public void checkRights(T entity, String username) /*throws SecurityException*/ ;

}
