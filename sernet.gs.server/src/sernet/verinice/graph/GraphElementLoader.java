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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.hibernate.TreeElementDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GraphElementLoader implements IGraphElementLoader {
 
    private static final Logger LOG = Logger.getLogger(GraphElementLoader.class);
    
    private Integer scopeId;
    
    private String[] typeIds;
    
    private IElementFilter elementFilter;
    
    private TreeElementDao<CnATreeElement, Long> cnaTreeElementDao;

    /* (non-Javadoc)
     * @see sernet.verinice.graph.IGraphElementLoader#loadElements()
     */
    @Override
    public List<CnATreeElement> loadElements() {
        DetachedCriteria crit = createDefaultCriteria();
        if(getScopeId()!=null) {
            crit.add(Restrictions.eq("scopeId", getScopeId()));
        }
        if(getTypeIds()!=null) {
            crit.add(Restrictions.in("objectType", getTypeIds()));
        }
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
        List<CnATreeElement> filteredList = new ArrayList<CnATreeElement>();
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
    
    public Integer getScopeId() {
        return scopeId;
    }

    @Override
    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
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
    
    public TreeElementDao<CnATreeElement, Long> getCnaTreeElementDao() {
        return cnaTreeElementDao;
    }

    public void setCnaTreeElementDao(TreeElementDao<CnATreeElement, Long> cnaTreeElementDao) {
        this.cnaTreeElementDao = cnaTreeElementDao;
    }

}
