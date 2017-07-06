/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.model;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This service provides methods to get the number of elements in the database.
 * The service is configured as a Spring bean in file: veriniceserver-common.xml.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class CountElementService implements ICountElementService {

    private static final Logger log = Logger.getLogger(CountElementService.class);

    private static final String HQL = "SELECT e.objectType as type,COUNT(e.objectType.uuid) "
            + "FROM CnATreeElement e "
            + "GROUP BY e.objectType";
    private static final Object[] PARAMS = new Object[]{};

    private Map<String, Long> idNumberMap = new Hashtable<>(0);
    private Long number;

    private Integer limit;
    private Map<String, Integer> typeLimitMap;

    private IBaseDao<CnATreeElement, Long> cnaTreeElementDao;

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.ICountElementService#getNumber()
     */
    @Override
    public long getNumber() {
        loadNumbers();
        return number;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.ICountElementService#getNumberOfAllTypes()
     */
    @Override
    public Map<String, Long> getNumberOfAllTypes() {
        loadNumbers();
        return idNumberMap;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.ICountElementService#getNumber(java.lang.String)
     */
    @Override
    public long getNumber(String typeID) {
        loadNumbers();
        return idNumberMap.get(typeID);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.ICountElementService#getLimit()
     */
    @Override
    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
        if(log.isInfoEnabled()) {
            log.info("Default limit: " + limit);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.ICountElementService#getLimit(java.lang.String)
     */
    @Override
    public Integer getLimit(String typeID) {
        Integer typeLimit = getTypeLimitMap().get(typeID);
        if(typeLimit==null) {
            typeLimit = getLimit();
        }
        return typeLimit;
    }

    @SuppressWarnings("unchecked")
    private void loadNumbers() {
        List<Object[]> hqlResult = getCnaTreeElementDao().findByQuery(HQL,PARAMS);
        processHqlResult(hqlResult);
        if(log.isInfoEnabled()) {
            logNumbers();
        }
    }

    protected void processHqlResult(List<Object[]> hqlResult) {
        idNumberMap = new HashMap<>();
        number = Long.valueOf(0);
        for (Object[] resultRow : hqlResult) {
            long numberType = (Long)resultRow[1];
            number += numberType;
            idNumberMap.put((String)resultRow[0], numberType);
        }
    }

    public Map<String, Integer> getTypeLimitMap() {
        return typeLimitMap;
    }

    public void setTypeLimitMap(Map<String, Integer> typeLimitMap) {
        this.typeLimitMap = typeLimitMap;
        if(log.isInfoEnabled()) {
            logTypeLimit();
        }
    }

    public IBaseDao<CnATreeElement, Long> getCnaTreeElementDao() {
        return cnaTreeElementDao;
    }

    public void setCnaTreeElementDao(IBaseDao<CnATreeElement, Long> cnaTreeElementDao) {
        this.cnaTreeElementDao = cnaTreeElementDao;
    }

    private void logNumbers() {
        log.info("Total number of objects: " + number);
        Set<String> typeIds = idNumberMap.keySet();
        for (String typeId : typeIds) {
            log.info(typeId + ": " + idNumberMap.get(typeId));
        }
    }

    private void logTypeLimit() {
        for (String typeId : typeLimitMap.keySet()) {
            log.info("Limit of type ID " + typeId + ": " + typeLimitMap.get(typeId));
        }
    }

}
