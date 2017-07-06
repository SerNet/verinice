/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.account.AccountSearchParameter;

/**
 * This command loads all accounts (Configurations)
 * which are visible for the user which executes this command.
 * 
 * A account is visible for a user if the user
 * has the right to read the related person object. 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class LoadVisibleAccounts extends GenericCommand implements IAuthAwareCommand {
    
    String hql = "select p.cnaTreeElement.dbId from Permission p where"
            + " p.cnaTreeElement.objectType in "
            + "('person','person-iso') and p.role in (:roles)";
   
    private transient IAuthService authService;
 
    private List<Configuration> accountList;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() { 
        if (isAdmin()) {
            accountList = getAllAccounts();
        } else {
            accountList = getVisibleAccounts();
        }
    }

    private List<Configuration> getVisibleAccounts() {
        List<String> roles = Arrays.asList(
                getConfigurationService().getRoles(
                        getAuthService().getUsername()));
        List visibleElementIdList = getDao().findByQuery(
                hql, new String[]{"roles"}, new Object[]{roles});
        List<Configuration> allAccounts = getAllAccounts();
        List<Configuration> visibleAccounts = new LinkedList<>();
        for (Configuration account : allAccounts) {
            if (visibleElementIdList.contains(account.getPerson().getDbId())) {
                visibleAccounts.add(account); 
            }
        }
        return visibleAccounts;
    }

    private List<Configuration> getAllAccounts() {
        return getAccountService().findAccounts(AccountSearchParameter.newInstance());
    }
    
    private boolean isAdmin() {
        return containsAdminRole(getAuthService().getRoles());
    }
    
    private boolean containsAdminRole(String[] roles) {
        if (roles != null) {
            for (String r : roles) {
                if (ApplicationRoles.ROLE_ADMIN.equals(r)) {
                    return true;
                }
            }   
        }
        return false;
    }
 
    public List<Configuration> getAccountList() {
        return accountList;
    }

    protected IBaseDao<Permission, Serializable> getDao() {
        return getDaoFactory().getDAO(Permission.class);
    }
    
    protected IAccountService getAccountService() {
        return (IAccountService) VeriniceContext.get(VeriniceContext.ACCOUNT_SERVICE);
    }
    
    protected IConfigurationService getConfigurationService() {
        return (IConfigurationService) VeriniceContext.get(VeriniceContext.CONFIGURATION_SERVICE);
    }
    
    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService service) {
        this.authService = service;
    }
}
