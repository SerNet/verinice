/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
 ******************************************************************************/
package sernet.verinice.service.bp.risk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;

/**
 * RiskServiceMetaDao contains methods for reading data in the database. The
 * methods in this class are only intended for use when executing a risk
 * analysis in IT base protection (ITBP).
 * 
 * This class is no ordinary verinice DAO. But it uses other DAOs and is
 * therefore called MetaDao. This class wraps accesses to the ordinary DAOs.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class RiskServiceMetaDao {

    private static final String SCOPE_ID = "scopeId";
    private static final String TYPE_ID = "typeId";

    // @formatter:off
    private static final String JOIN_PROPERTIES = "join fetch entity.typedPropertyLists as propertyList "
            + "join fetch propertyList.properties as props ";

    private static final String HQL_LOAD_BY_TYPE_AND_SCOPE = "select element from CnATreeElement element "
            + "join fetch element.entity as entity " 
            + JOIN_PROPERTIES
            + "where element.objectType = :" + TYPE_ID + " " 
            + "and element.scopeId = :" + SCOPE_ID; // $NON-NLS-2$
    // @formatter:on

    private IBaseDao<CnATreeElement, Integer> elementDao;
    private IBaseDao<ItNetwork, Integer> itNetworkDao;

    public ItNetwork loadItNetwork(String uuid) {
        return getItNetworkDao().findByUuid(uuid, RetrieveInfo.getPropertyInstance());
    }

    public ItNetwork updateItNetwork(ItNetwork itNetwork) {
        return getItNetworkDao().merge(itNetwork);
    }

    @SuppressWarnings("unchecked")
    public Set<CnATreeElement> loadRequirementsFromScope(Integer scopeId) {
        return (Set<CnATreeElement>) loadElementsFromScope(scopeId, BpRequirement.TYPE_ID);
    }

    @SuppressWarnings("unchecked")
    public Set<CnATreeElement> loadSafeguardsFromScope(Integer scopeId) {
        return (Set<CnATreeElement>) loadElementsFromScope(scopeId, Safeguard.TYPE_ID);
    }

    @SuppressWarnings("unchecked")
    public Set<BpThreat> loadThreatsFromScope(Integer scopeId) {
        return (Set<BpThreat>) loadElementsFromScope(scopeId, BpThreat.TYPE_ID);
    }

    @SuppressWarnings("unchecked")
    public Set<? extends CnATreeElement> loadElementsFromScope(Integer scopeId, String typeId) {
        List<BpThreat> resultList = getElementDao().findByCallback(session -> {
            Query query = session.createQuery(HQL_LOAD_BY_TYPE_AND_SCOPE)
                    .setParameter(TYPE_ID, typeId).setParameter(SCOPE_ID, scopeId);
            query.setReadOnly(true);
            return query.list();
        });
        return new HashSet<>(resultList);
    }

    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }

    public IBaseDao<ItNetwork, Integer> getItNetworkDao() {
        return itNetworkDao;
    }

    public void setItNetworkDao(IBaseDao<ItNetwork, Integer> itNetworkDao) {
        this.itNetworkDao = itNetworkDao;
    }



}
