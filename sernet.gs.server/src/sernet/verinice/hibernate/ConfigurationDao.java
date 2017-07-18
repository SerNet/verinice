/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.hibernate;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ConfigurationDao extends TreeElementDao<Configuration, Serializable> {

    private IAccountService accountService;

    public ConfigurationDao() {
        super(Configuration.class);
    }

    @Override
    public Configuration merge(Configuration entity) {
        saveAccountGroups(entity.getRoles(false));
        return (Configuration) super.merge(entity);
    }

    @Override
    public void saveOrUpdate(Configuration entity) {
        saveAccountGroups(entity.getRoles(false));
        super.saveOrUpdate(entity);
    }

    private void saveAccountGroups(Set<String> accountGroupNames) {

        List<AccountGroup> accountGroups = getAccountService().listGroups();

        for (AccountGroup aGroup : accountGroups) {
            if (accountGroupNames.contains(aGroup.getName())) {
                accountGroupNames.remove(aGroup.getName());
            }
        }

        accountService.saveAccountGroups(accountGroupNames);
    }

    public IAccountService getAccountService() {
        if (accountService == null) {
            accountService = createAccountService();
        }
        return accountService;
    }
    
    public static IAccountService createAccountService() {
        return (IAccountService) VeriniceContext.get(VeriniceContext.ACCOUNT_SERVICE);
    }
}
