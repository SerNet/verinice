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
import org.springframework.security.intercept.method.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.vote.AccessDecisionVoter;

import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IRightsServerHandler;
import sernet.verinice.service.RightsServerHandler;

/**
 * Handles access to verinice services, which are secured by action ids (@link
 * {@link ActionRightIDs}).
 * 
 * <p>
 * It uses the user name which is provided by spring security and checks with
 * the help of {@link RightsServerHandler} if the use is allowed to use a
 * specific mehtod of an object.</p>
 * 
 * @see MethodSecurityInterceptor
 * @see RightsServerHandler
 * @see Authentication
 * 
 * @author Benjamin Weißenfels <bw@sernet.de>
 *
 */
public class VeriniceActionIdVoter implements AccessDecisionVoter {

    private String actionIdPrefix = "ACTION_ID_";

    private IRightsServerHandler rightsServerHandler;

    @Override
    public int vote(Authentication authentication, Object object, ConfigAttributeDefinition config) {

        String name = authentication.getName();

        // authorize internal calls to do everything they want to do.
        if(DummyAuthentication.INTERNAL_USER.equals(name)) {
            return ACCESS_GRANTED;
        }

        @SuppressWarnings("unchecked")
        Collection<ConfigAttribute> configAttributes = config.getConfigAttributes();

        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
        while (iterator.hasNext()) {

            ConfigAttribute attribute = iterator.next();

            if(!supports(attribute)){
                continue;
            }

            String actionId = extractActionId(attribute.getAttribute());

            if (getRightsServerHandler().isEnabled(name, actionId)) {
                return ACCESS_GRANTED;
            }
        }

        return ACCESS_ABSTAIN;
    }

    private String extractActionId(String attribute) {
        String lowerCase = attribute.toLowerCase();
        return lowerCase.substring(getActionIdPrefix().length());
    }

    public IRightsServerHandler getRightsServerHandler() {
        return rightsServerHandler;
    }

    public void setRightsServerHandler(IRightsServerHandler rightsServerHandler) {
        this.rightsServerHandler = rightsServerHandler;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return (attribute.getAttribute() != null) && attribute.getAttribute().startsWith(getActionIdPrefix());
    }

    public String getActionIdPrefix() {
        return actionIdPrefix;
    }

    public void setActionIdPrefix(String actionIdPrefix) {
        this.actionIdPrefix = actionIdPrefix;
    }

    @Override
    public boolean supports(Class clazz) {
        return true;
    }

}
