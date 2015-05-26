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
package sernet.verinice.rcp.accountgroup;

import java.util.Set;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
interface IAccountGroupViewDataService {

    String[] getAccountGroups();

    String[] getAccountNamesForGroup(String accountGroupName);

    String[] getAllAccounts();

    void addAccountGroup(String accountGroupName);

    void loadAccountGroupData();

    String[] saveAccountGroupData(String groupName, String[] accountNames);

    String[] deleteAccountGroupData(String groupName, String[] accountNames);

    void editAccountGroupName(String newName, String oldName);

    Set<String> deleteAccountGroup(String groupName);
    
    String getPrettyPrintAccountName(String account);

}
