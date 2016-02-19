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
package sernet.verinice.interfaces.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * GraphElementLoader loads elements for {@link GraphService} or {@link GraphCommand}s.
 * You can add one or more loader to GraphService.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GraphElementLoader implements IGraphElementLoader, Serializable {
 
    private static final long serialVersionUID = -6099406083025720444L;

    private static final Logger LOG = Logger.getLogger(GraphElementLoader.class);
    
    private Integer[] scopeIds;
    
    private String[] typeIds;
    
    private IElementFilter elementFilter;
    
    private transient IBaseDao<CnATreeElement, Long> cnaTreeElementDao;

    /* (non-Javadoc)
     * @see sernet.verinice.graph.IGraphElementLoader#loadElements()
     */
    @Override
    public List<CnATreeElement> loadElements() {
        DetachedCriteria crit = createDefaultCriteria();
        if (getScopeIds() != null && getScopeIds().length > 0) {
            crit.add(Restrictions.in("scopeId", getScopeIds()));
        }
        if (getTypeIds() != null && getTypeIds().length > 0) {
            crit.add(Restrictions.in("objectType", getTypeIds()));
        }
        @SuppressWarnings("unchecked") // daos does not use generics
        List<CnATreeElement> elementList = getCnaTreeElementDao().findByCriteria(crit);
        elementList = filterElements(elementList);
        if (LOG.isInfoEnabled()) {
            LOG.info(elementList.size() + " relevant objects found");
        }
        return elementList;
    }
    
    private List<CnATreeElement> filterElements(List<CnATreeElement> elementList) {
        if(getElementFilter()==null) {
            return elementList;
        }
        List<CnATreeElement> filteredList = new ArrayList<>();
        for (CnATreeElement element : elementList) {
            if(getElementFilter().check(element)) {
                filteredList.add(element);
            }
        }
        return filteredList;
    }
    
    private DetachedCriteria createDefaultCriteria() {
        DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class);
        crit.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
        crit.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return crit;
    }
    
    public Integer[] getScopeIds() {
        return scopeIds;
    }

    @Override
    public void setScopeIds(Integer... scopeIds) {
        this.scopeIds = scopeIds;
    }
    
    @Override
    public void setScopeId(Integer scopeId) {
        this.scopeIds = new Integer[] { scopeId };
    }
    
    public String[] getTypeIds() {
        return (typeIds != null) ? typeIds.clone() : null;
    }

    @Override
    public void setTypeIds(String[] typeIds) {
        this.typeIds = (typeIds != null) ? typeIds.clone() : null;
    }
    
    public IElementFilter getElementFilter() {
        return elementFilter;
    }

    @Override
    public void setElementFilter(IElementFilter elementFilter) {
        this.elementFilter = elementFilter;
    }
    
    public IBaseDao<CnATreeElement, Long> getCnaTreeElementDao() {
        return cnaTreeElementDao;
    }

    @Override
    public void setCnaTreeElementDao(IBaseDao<CnATreeElement, Long> cnaTreeElementDao) {
        this.cnaTreeElementDao = cnaTreeElementDao;
    }

}
