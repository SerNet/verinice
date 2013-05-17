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
package sernet.verinice.graph;

import java.util.List;

import sernet.verinice.hibernate.TreeElementDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * IGraphElementLoader loads elements for {@link GraphService}.
 * You can add one or more loader to GraphService.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IGraphElementLoader {

    /**
     * If this parameter is set only elements of one one scope / organization
     * are loaded.
     * 
     * @param scopeId Scope-id / organization db-id
     */
    void setScopeId(Integer scopeId);
    
    /**
     * If this parameter is set only elements with specified type-id
     * are loaded.
     * 
     * @param typeIds Type-ids such as Asset.TYPE_ID or Control.TYPE_ID
     */
    void setTypeIds(String[] typeIds);
    
    /**
     * If this parameter is set. Elements are filtered 
     * by an IElementFilter
     * 
     * @param elementFilter An element filter
     */
    void setElementFilter(IElementFilter elementFilter);
    
    void setCnaTreeElementDao(TreeElementDao<CnATreeElement, Long> cnaTreeElementDao); 
    
    /**
     * Loads and returns the elements specified by parameters
     * and element filter.
     * 
     * @return A list of elements
     */
    List<CnATreeElement> loadElements();
}
