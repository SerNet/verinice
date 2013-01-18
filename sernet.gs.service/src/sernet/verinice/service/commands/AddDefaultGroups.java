/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.INoAccessControl;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class AddDefaultGroups extends GenericCommand implements INoAccessControl {

    private transient Logger log = Logger.getLogger(AddDefaultGroups.class);
    
    IBaseDao<Configuration, Serializable> configurationDao;
    
    IBaseDao<Property, Serializable> propertyDao;
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(AddDefaultGroups.class);
        }
        return log;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        boolean addUserGroup = false;
        boolean addAdminGroup = false;
        try {
            checkGroupName(IRightsService.USERDEFAULTGROUPNAME);
            addUserGroup = true;
        } catch(GroupExistsException e) {
            getLog().warn(e.getMessage());
        }
        try {
            checkGroupName(IRightsService.ADMINDEFAULTGROUPNAME);
            addAdminGroup = true;
        } catch(GroupExistsException e) {
            getLog().warn(e.getMessage());
        }
        List<Configuration> configurationList = getConfigurationDao().findAll(RetrieveInfo.getPropertyInstance());
        for (Configuration conf : configurationList) {
            if(conf.isAdminUser()) {
                if(addAdminGroup) {
                    conf.getEntity().createNewProperty(getRolePropertyType(), IRightsService.ADMINDEFAULTGROUPNAME);
                }
            } else if(addUserGroup) {
                conf.getEntity().createNewProperty(getRolePropertyType(), IRightsService.USERDEFAULTGROUPNAME);
            }
        }
    }

    /**
     * @return
     */
    private PropertyType getRolePropertyType() {
        return getHuiTypeFactory().getPropertyType(Configuration.TYPE_ID, Configuration.PROP_ROLES);
    }

    /**
     * 
     */
    private void checkGroupName(String name) {
        DetachedCriteria criteria = DetachedCriteria.forClass(Property.class);
        criteria.add(Restrictions.eq("propertyType", Configuration.PROP_ROLES));
        criteria.add(Restrictions.like("propertyValue", name));
        List<Property> result = getPropertyDao().findByCriteria(criteria);
        if(result!=null && !result.isEmpty()) {
            throw new GroupExistsException("Default user group name already exists: " + name);
        }
    }
    
    private HUITypeFactory getHuiTypeFactory() {
        return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
    }

    /**
     * @return the configurationDao
     */
    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        if(configurationDao==null) {
            configurationDao = getDaoFactory().getDAO(Configuration.class);
        }
        return configurationDao;
    }

    /**
     * @return the propertyDao
     */
    public IBaseDao<Property, Serializable> getPropertyDao() {
        if(propertyDao==null) {
            propertyDao = getDaoFactory().getDAO(Property.class);
        }
        return propertyDao;
    }

    class GroupExistsException extends RuntimeException {

        /**
         * @param message
         */
        public GroupExistsException(String message) {
            super(message);
        }
        
    }

}
