/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/

package sernet.verinice.hibernate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.licensemanagement.ILicenseManagementEntryDao;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementEntryDao extends HibernateDao<LicenseManagementEntry, Serializable> implements ILicenseManagementEntryDao {

    /**
     * @param type
     */
    public LicenseManagementEntryDao() {
        super(LicenseManagementEntry.class);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementEntryDao#findById(int)
     */
    @Override
    public LicenseManagementEntry findById(int dbId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(LicenseManagementEntry.class);
        criteria.add(Restrictions.eq("dbId", dbId));
        List<LicenseManagementEntry> list = findByCriteria(criteria);
        if (list.size() == 1){
            return list.get(0);
        } 
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementEntryDao#findByContentIdentifier(java.lang.String)
     */
    @Override
    public Set<LicenseManagementEntry> findByContentIdentifier(String contentIdentifier) {
        Set<LicenseManagementEntry> set = new HashSet<LicenseManagementEntry>();
        DetachedCriteria criteria = DetachedCriteria.forClass(LicenseManagementEntry.class);
        criteria.add(Restrictions.eq("contentIdentifier", contentIdentifier));
        List<LicenseManagementEntry> list = findByCriteria(criteria);
        if(list != null && list.size() > 0){
            set.addAll(list);
        }
        return set;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementEntryDao#findByLicenseId(java.lang.String)
     */
    @Override
    public LicenseManagementEntry findByLicenseId(String licenseId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(LicenseManagementEntry.class);
        criteria.add(Restrictions.eq("licenseID", licenseId));
        List<LicenseManagementEntry> list = findByCriteria(criteria);
        if (list.size() == 1){
            return list.get(0);
        } 
        return null;
    }

}
