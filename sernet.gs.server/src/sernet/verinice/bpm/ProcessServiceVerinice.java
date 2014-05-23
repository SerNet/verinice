/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.bpm;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.bpm.IControlExecutionProcess;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Extends {@link ProcessServiceGeneric} with generic methods which have dependencies
 * to verinice classes but have no relation to a specific process.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessServiceVerinice extends ProcessServiceGeneric {

    private static final Logger LOG = Logger.getLogger(ProcessServiceVerinice.class);
    
    private IDao<ChangeLogEntry, Integer> changeLogEntryDao;
    private IBaseDao<CnATreeElement,Integer> elementDao;

    /**
     * Returns the owner (creator) of a {@link CnATreeElement}
     * by querying table changelogentry.
     * 
     * If no owner is found, default owner is returned:
     * IControlExecutionProcess.DEFAULT_OWNER_NAME
     * 
     * @param element A CnATreeElement
     * @return The owner (creator) of the element
     */
    protected String getOwnerName(CnATreeElement element) {
        String owner = IControlExecutionProcess.DEFAULT_OWNER_NAME;
        DetachedCriteria crit = DetachedCriteria.forClass(ChangeLogEntry.class);
        crit.add(Restrictions.eq("elementId", element.getDbId()));
        crit.add(Restrictions.eq("change", ChangeLogEntry.TYPE_INSERT));
        crit.addOrder(Order.asc("changetime"));
        List<ChangeLogEntry> result = getChangeLogEntryDao().findByCriteria(crit);
        if(result!=null && !result.isEmpty() && result.get(0).getUsername()!=null) {
            owner = result.get(0).getUsername();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Owner of control: " + element.getUuid() + " is: " + owner);
            }
        } else {
            LOG.warn("Can not find owner of control: " + element.getUuid() + ", using default owner name: " + owner);
        }
        return owner;
    }

    public IDao<ChangeLogEntry, Integer> getChangeLogEntryDao() {
        return changeLogEntryDao;
    }

    public void setChangeLogEntryDao(IDao<ChangeLogEntry, Integer> changeLogEntryDao) {
        this.changeLogEntryDao = changeLogEntryDao;
    }

    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }
    
    /**
     * True: This is a real implementation.
     * 
     * @see sernet.verinice.interfaces.bpm.IProcessServiceIsa#isActive()
     */
    @Override
    public boolean isActive() {
        return true;
    }

}
