/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.connect;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;


public interface IBaseDao<T, ID extends Serializable> {

		 public void saveOrUpdate(T entity);
		 
		 public T merge(T entity);

		 public T merge(T entity, boolean fireUpdates);

		 public void delete(T entity);

		 public T findById(ID id);

		 public List<T> findAll();
		 
		 public List findByQuery(String hqlQuery, Object[] params);
		 
		 public int updateByQuery(String hqlQuery, Object[] values);
		 
		 public void refresh(T element);
		 
		 public void reload(T element, Serializable id);

		 public void initialize(Object collection);
		 
		 public void flush();

		 public Class<T> getType();
		 
}
