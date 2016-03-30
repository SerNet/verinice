/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels <bw@sernet.de>.
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
 *     Benjamin Weißenfels <bw@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.security;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.vote.RoleVoter;

import sernet.verinice.interfaces.IRightsServerHandler;


/**
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public class VeriniceActionIdVoter extends RoleVoter {

    private String rolePrefix;

    private IRightsServerHandler rightsServerHandler;


    public String getRolePrefix() {
        return rolePrefix;
    }


    public void setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;

    }

    @Override
    public boolean supports(Class clazz) {
       return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, ConfigAttributeDefinition config) {

        String name = authentication.getName();

        @SuppressWarnings("unchecked")
        Collection <ConfigAttribute>configAttributes = config.getConfigAttributes();

        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
        while(iterator.hasNext()) {
            ConfigAttribute attribute = iterator.next();
            if(getRightsServerHandler().isEnabled(name, attribute.getAttribute())){
                return ACCESS_GRANTED;
            }
        }

        return ACCESS_ABSTAIN;
    }


    public IRightsServerHandler getRightsServerHandler() {
        return rightsServerHandler;
    }


    public void setRightsServerHandler(IRightsServerHandler rightsServerHandler) {
        this.rightsServerHandler = rightsServerHandler;
    }

}
