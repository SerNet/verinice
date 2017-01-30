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
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
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
    
    private String[] hibernateTypeIds;
    
    private IElementFilter elementFilter;
    
    private transient IBaseDao<CnATreeElement, ? extends Serializable> cnaTreeElementDao;

    /* (non-Javadoc)
     * @see sernet.verinice.graph.IGraphElementLoadeTr#loadElements()
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
        return (hibernateTypeIds != null) ? hibernateTypeIds.clone() : null;
    }

    /**
     * Set the type ids for this loader.
     * The type ids from SNCA.xml are replaced by the hibernate type ids from
     * *.hbm.xml mapping file if hibernate id is nor the same.
     * 
     * @param typeIds Type ids from SNCA.xml
     * @see sernet.verinice.interfaces.graph.IGraphElementLoader#setTypeIds(java.lang.String[])
     */
    @Override
    public void setTypeIds(String[] typeIds) {
        List<String> hibernateTypeIdList = new LinkedList<>();
        if(typeIds!=null) {
            for (int i = 0; i < typeIds.length; i++) {
                hibernateTypeIdList.add(HibernateTypeIdManager.getHibernateTypeId(typeIds[i]));
                // There are 2 different Hibernate ids for MassnahmenUmsetzung.TYPE_ID: 
                // MassnahmenUmsetzung.HIBERNATE_TYPE_ID and
                // RisikoMassnahmenUmsetzung.HIBERNATE_TYPE_ID
                if(MassnahmenUmsetzung.TYPE_ID.equals(typeIds[i])) {
                    hibernateTypeIdList.add(RisikoMassnahmenUmsetzung.HIBERNATE_TYPE_ID);
                }
            } 
        } 
        this.hibernateTypeIds = hibernateTypeIdList.toArray(new String[hibernateTypeIdList.size()]);
    }
    
    public IElementFilter getElementFilter() {
        return elementFilter;
    }

    @Override
    public void setElementFilter(IElementFilter elementFilter) {
        this.elementFilter = elementFilter;
    }
    
    public IBaseDao<CnATreeElement, ? extends Serializable> getCnaTreeElementDao() {
        return cnaTreeElementDao;
    }

    @Override
    public void setCnaTreeElementDao(IBaseDao<CnATreeElement, ? extends Serializable> cnaTreeElementDao) {
        this.cnaTreeElementDao = cnaTreeElementDao;
    }

}
