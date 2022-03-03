/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade.
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
package sernet.verinice.service.ldap;

import java.io.Serializable;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ldap.ILdapCommand;
import sernet.verinice.interfaces.ldap.ILdapService;

public class UsePasswordFromClient extends GenericCommand implements ILdapCommand, Serializable {

    private static final long serialVersionUID = -1347543117308349796L;

    private transient ILdapService ldapService;

    boolean result;

    public UsePasswordFromClient() {
        super();
    }

    @Override
    public void execute() {
        result = ldapService.isUsePasswordFromClient();
    }

    public boolean isUsePasswordFromClient() {
        return result;
    }

    @Override
    public ILdapService getLdapService() {
        return ldapService;
    }

    @Override
    public void setLdapService(ILdapService ldapService) {
        this.ldapService = ldapService;
    }

}
