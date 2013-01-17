/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.configuration.Configuration;

/**
 *
 */
public class CheckUserName extends GenericCommand implements IAuthAwareCommand {

    private String userName;
    
    private transient IAuthService authService;
    
    private transient Logger LOG = Logger.getLogger(CheckUserName.class);
    
    private boolean result = false;
    
    private transient IBaseDao<Configuration, Serializable> dao;
    
    public CheckUserName(String username){
        this.userName = username;
    }
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        checkUsername(userName);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthAwareCommand#setAuthService(sernet.verinice.interfaces.IAuthService)
     */
    @Override
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthAwareCommand#getAuthService()
     */
    @Override
    public IAuthService getAuthService() {
        return this.authService;
    }

    /**
     * Checks if the username in a {@link Configuration} is unique in the database.
     * Throws {@link UsernameExistsRuntimeException} if username is not available.
     * If username is not set or null no exception is thrown
     * 
     * @param element a {@link Configuration}
     * @throws UsernameExistsRuntimeException if username is not available
     */
    private void checkUsername(String username) throws UsernameExistsRuntimeException {
        if(getAuthService().getAdminUsername().equals(username)) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("Username is admin name: " + username);
            }
            result = true;
            return;
        }

        DetachedCriteria criteria = DetachedCriteria.forClass(Property.class);
        criteria.add(Restrictions.eq("propertyType", Configuration.PROP_USERNAME));
        criteria.add(Restrictions.like("propertyValue", username));

        List resultList = getDao().findByCriteria(criteria);
        if(resultList!=null && !resultList.isEmpty()) {
            // save only if this is really the same user object:
            boolean doubleUsername = false;


            checkDoubles: for (Object t : resultList) {
                Property foundProperty = (Property) t;


                if ( foundProperty.getPropertyValue().equals(username) ) { // current dbId doesn't match found username, is double
                    doubleUsername = true;
                    break checkDoubles;
                }
            }
            if (doubleUsername) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Username exists: " + username);
                }
                result = true;
            }
        }
    }
    
    public boolean getResult(){
        return result;
    }
    
    private Logger getLog(){
        if(LOG == null){
            LOG = Logger.getLogger(CheckUserName.class);
        } 
        return LOG;
    }
    
    public IBaseDao<Configuration, Serializable> getDao() {
        if (dao == null) {
            dao = createDao();
        }
        return dao;
    }

    private IBaseDao<Configuration, Serializable> createDao() {
        return (IBaseDao<Configuration, Serializable>) getDaoFactory().getDAO(Configuration.TYPE_ID);
    }
    
    
}
