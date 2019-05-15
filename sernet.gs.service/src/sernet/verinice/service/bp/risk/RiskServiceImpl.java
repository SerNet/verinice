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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.risk.configuration.RiskConfiguration;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateContext;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateResult;

/**
 * Service implementation to run and configure an IT base protection (ITBP) risk
 * analysis.
 * 
 * The changes made to elements in this class are not saved directly, they are
 * saved indirectly by Hibernate. To ensure that the changes are really saved,
 * this class must be used in a JDBC transaction. The JDBC transaction
 * management is configured by Spring.
 * 
 * This service is managed by the Spring framework. It is configured in file
 * veriniceserver-risk-analysis.xml (On the server / verinice.PRO) or
 * veriniceserver-risk-analysis-standalone.xml (verinice standalone)
 * 
 * This service is configured as a singleton (see:
 * https://en.wikipedia.org/wiki/Singleton_pattern).
 */
public class RiskServiceImpl implements RiskService {

    private RiskServiceMetaDao metaDao;
    private IBaseDao<PropertyList, Integer> propertyListDao;

    private Map<Integer, RiskConfiguration> riskConfigurationCache = Collections
            .synchronizedMap(new LinkedHashMap<Integer, RiskConfiguration>() {

                private static final long serialVersionUID = -5796349016191394354L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<Integer, RiskConfiguration> eldest) {
                    return size() > 500;
                }
            });

    /**
     * Update the IT network with the new risk configuration. Delete frequencies
     * and impacts that have been used but should be removed.
     * 
     * @see sernet.verinice.service.bp.risk.RiskService#updateRiskConfiguration(sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateContext)
     */
    @Override
    public RiskConfigurationUpdateResult updateRiskConfiguration(
            RiskConfigurationUpdateContext updateContext) {
        ServerInitializer.inheritVeriniceContextState();
        ItNetwork itNetwork = getMetaDao().loadItNetwork(updateContext.getUuidItNetwork());
        Integer itNetworkDBId = itNetwork.getDbId();

        updateItNetwork(itNetwork, updateContext);
        return updateRiskValuesInThreats(itNetworkDBId, updateContext);
    }

    private void updateItNetwork(ItNetwork itNetwork,
            RiskConfigurationUpdateContext updateContext) {
        itNetwork.setRiskConfiguration(updateContext.getRiskConfiguration());
        getMetaDao().updateItNetwork(itNetwork);
        Integer itNetworkId = itNetwork.getDbId();
        if (riskConfigurationCache.containsKey(itNetworkId)) {
            riskConfigurationCache.put(itNetworkId, updateContext.getRiskConfiguration());
        }
    }

    private RiskConfigurationUpdateResult updateRiskValuesInThreats(Integer scopeId,
            RiskConfigurationUpdateContext updateContext) {
        Set<BpThreat> threatsFromScope = getMetaDao().loadThreatsFromScope(scopeId);
        RiskValueInThreatUpdater riskValueUpdater = new RiskValueInThreatUpdater(updateContext,
                threatsFromScope);
        riskValueUpdater.setPropertyListDao(propertyListDao);
        riskValueUpdater.execute();
        return riskValueUpdater.getRiskConfigurationUpdateResult();
    }

    @Override
    public RiskConfiguration findRiskConfiguration(Integer itNetworkID) {
        RiskConfiguration riskConfiguration = riskConfigurationCache.get(itNetworkID);
        if (riskConfiguration == null
                // null is a valid value for a RiskConfiguration and therefore
                // does not imply "not found".
                && !riskConfigurationCache.containsKey(itNetworkID)) {
            riskConfiguration = getMetaDao().getItNetworkDao().findById(itNetworkID)
                    .getRiskConfiguration();
            riskConfigurationCache.put(itNetworkID, riskConfiguration);
        }
        return riskConfiguration;
    }

    public RiskServiceMetaDao getMetaDao() {
        return metaDao;
    }

    public void setMetaDao(RiskServiceMetaDao metaDao) {
        this.metaDao = metaDao;
    }

    public IBaseDao<PropertyList, Integer> getPropertyListDao() {
        return propertyListDao;
    }

    public void setpropertyListDao(IBaseDao<PropertyList, Integer> propertyListDao) {
        this.propertyListDao = propertyListDao;
    }

}
